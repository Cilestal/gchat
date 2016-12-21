package org.ml.gchat.engine;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.ml.gchat.db.IncorrectLoginException;
import org.ml.gchat.db.UserAlreadyExistsException;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Date: 10.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public class Server implements Closeable, Runnable {
    private String serverTitle = "Server";
    private String welcomeMessageText = ", welcome you!";
    private boolean showWelcomeMessage;

    public static final int SERVER_MAX_USERS = 100;

    private static final int MAX_PORT_NUM = 5;
    private static final String HOST = "localhost";

    /** Размер буфера должен быть одинаковым на клиенте и сервере */
    private static final int BUFFER_SIZE = 2048;
    private static final int MAX_BUFFER_SIZE = BUFFER_SIZE * 4;
    private ByteBuffer serverBuffer;

    private Map<SelectionKey, String> users;
    private ConcurrentLinkedQueue<Message> messages;

    private final int[] ports;
    private Selector selector;
    private ServerSocketChannel[] channels;
    private boolean statusFlag;

    private final LoginLogic loginLogic;
    private final Gson gson;

    private static final Logger LOG = Logger.getLogger(Server.class);

    public Server(int[] ports) {
        this(ports, null);
    }

    public Server(int[] ports, LoginLogic loginLogic) {
        this.serverBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.users = new HashMap<>(32);
        this.messages = new ConcurrentLinkedQueue<>();
        this.showWelcomeMessage = true;

        if (ports.length > MAX_PORT_NUM)
            throw new RuntimeException("Максимальное количество портов = " + MAX_PORT_NUM);

        this.ports = ports;
        this.loginLogic = loginLogic;
        this.gson = new Gson();
    }

    private void init() throws IOException {
        this.selector = Selector.open();
        LOG.debug("Selector open: " + this.selector.isOpen());

        for (int i = 0; i < this.ports.length; i++) {
            LOG.debug("Register port: " + ports[i]);
            channels[i] = ServerSocketChannel.open();

            InetSocketAddress host = new InetSocketAddress(HOST, ports[i]);
            channels[i].bind(host);
            channels[i].configureBlocking(false);
            channels[i].register(this.selector, SelectionKey.OP_ACCEPT);
        }
    }

    @Override
    public void run() {
        if (ports == null || ports.length == 0)
            throw new RuntimeException("Порты отсутствуют");

        try {
            this.channels = new ServerSocketChannel[ports.length];

            try {
                init();
            } catch (Exception e) {
                close();
                LOG.fatal("Ошибка инициализации. Сервер будет закрыт.", e);
                throw e;
            }

            this.statusFlag = true;
            while (statusFlag) {
                selector.select();

                if (!statusFlag) return;

                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isReadable()) {
                        read(key);
                    }
                }
            }
        } catch (IOException e) {
            LOG.fatal("Ошибка работы сервера.", e);
        } catch (ConcurrentModificationException e) {
            LOG.fatal("ConcurrentModificationException");
        }
    }

    /*
    * Команды SelectionKey
    * */

    //подключение клиента, переходим в READ
    private void accept(SelectionKey key) {
        try {
            SocketChannel newChannel = ((ServerSocketChannel) key.channel()).accept();
            newChannel.configureBlocking(false);
            newChannel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //чтение сообщения из канала
    private void read(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = prepareBuffer(key);
        buffer.clear();

        int mesLen;
        StringBuilder sb = new StringBuilder();

        try {
            do {
                mesLen = client.read(buffer);

                if (mesLen == -1) {
                    closeClientConnection(key);
                    break;
                }

                // TODO: 18.11.2016 ByteBuffer -> CharBuffer
                String str = new String(buffer.array(), 0, mesLen, "UTF-8").trim();
                sb.append(str);
            } while (mesLen > 0 && sb.length() < MAX_BUFFER_SIZE);

            if(sb.length() > 0) {
                readMessages(key, sb.toString());
            }
        } catch (IOException e) {
            LOG.error("Reading problem, closing connection.");
            closeClientConnection(key);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    /**
     * @param key SelectionKey
     * @param messages сообщения от пользователя
     */
    private void readMessages(SelectionKey key, String messages) {
        String[] mesArray = messages.split("(?<=[}])");

        for (String mes : mesArray) {
            Message message = gson.fromJson(mes, Message.class);
            readMessage(key, message);
        }
    }

    /*
    * Методы обработки полученных сообщений
    * */
    private ByteBuffer prepareBuffer(SelectionKey key) {
        ByteBuffer attachment = (ByteBuffer) key.attachment();

        if (attachment == null) {
            attachment = ByteBuffer.allocate(BUFFER_SIZE);
            key.attach(attachment);
        }

        return attachment;
    }

    private void readMessage(SelectionKey key, Message message) {
        if (message != null) {
            switch (message.getCommand()) {
                case USER_AUTH:
                    auth(key, message);
                    break;
                case PUBLIC_MESSAGE:
                    publicMessage(key, message);
                    break;
                case USER_LOGOUT:
                    closeClientConnection(key);
                    break;
                case REGISTER:
                    register(key, message);
                    break;
                case PRIVATE_MESSAGE:
                    sendPrivateMessage(key, message);
                    break;
                default:
                    LOG.warn("Получена неизвестная команда!");
                    break;
            }
        } else {
            LOG.warn("Получен неправильный Message!");
        }
    }

    private void publicMessage(SelectionKey key, Message message) {
        if (isAuthUser(key)) {
            messages.add(message);
            LOG.info(message.getUser() + ": " + message.getMessage());
            sendBroadcastMessage(message);
        } else {
            sendServerMassage(key, Command.ERROR, ErrorMessage.AUTH_ERROR.toString());
            closeClientConnection(key);
        }
    }

    private void register(SelectionKey key, Message message) {
        try {
            loginLogic.registerLogin(message);
            LOG.info("User " + message.getUser() + " register!");
            sendServerMassage(key, Command.REGISTER_ACCEPT, "");
        } catch (UserAlreadyExistsException e) {
            sendServerMassage(key, Command.ERROR, ErrorMessage.REGISTER_USER_ALREADY_EXISTS.toString());
        } catch (IncorrectLoginException e) {
            sendServerMassage(key, Command.ERROR, ErrorMessage.REGISTER_LOGIN_INCORRECT.toString());
        } catch (Exception e) {
            sendServerMassage(key, Command.ERROR, ErrorMessage.REGISTER_PASSWORD_INCORRECT.toString());
            e.printStackTrace();
        }
    }

    private void sendPrivateMessage(SelectionKey senderKey, Message message) {
        String recipient = message.getUser();
        String sender = users.get(senderKey);
        String mes = message.getMessage();

        SelectionKey recipientKey = getUserSelectionKey(recipient);
        if (recipientKey != null) {
            Message privateMessage = new Message(Command.PRIVATE_MESSAGE, sender, new Date(), mes);
            sendMassage(recipientKey, privateMessage);
            sendMassage(senderKey, privateMessage);
        } else {
            String errorMessage = "User " + recipient + " not found!";
            sendServerMassage(senderKey, Command.PUBLIC_MESSAGE, errorMessage);
        }
    }

    /*
    * Методы авторизации пользователя и поиска пользователя
    * */

    private void auth(SelectionKey key, Message message) {
        if (!checkAuthMessage(key, message)) {
            closeClientConnection(key);
            return;
        }

        String login = message.getUser();
        sendServerMassage(key, Command.AUTH_ACCEPT, null);

        //оповещаем всех об авторизации пользователя
        sendServerBroadcastMessage(login, Command.ONLINE_USER);

        users.put(key, login);

        //отсылаем список пользователей чата новому пользователю
        users.forEach((k, name) -> sendServerMassage(key, Command.ONLINE_USER, name));

        if (showWelcomeMessage) {
            sendServerMassage(key, Command.PUBLIC_MESSAGE, login + welcomeMessageText);
        }
        SocketChannel client = (SocketChannel) key.channel();

        try {
            LOG.info(login + ": " + client.getRemoteAddress() + " connected.");
        } catch (IOException e) {
            LOG.error("Ошибка получения адреса клиента.");
        }
    }

    private boolean isAuthUser(SelectionKey key) {
        return users.containsKey(key);
    }

    private boolean checkAuthMessage(SelectionKey key, Message message) {
        String user;

        if (message == null || (user = message.getUser()) == null) {
            return false;
        }

        //проверка полученных данных
        if (loginLogic != null) {
            boolean check = loginLogic.checkLogin(message);

            if (!check) {
                LOG.debug(user + " login error!");
                sendServerMassage(key, Command.ERROR, ErrorMessage.LOGIN_OR_PASS_INCORRECT.toString());
                return false;
            }
        }

        //если пользователь уже авторизирован
        if (users.containsValue(user)) {
            sendServerMassage(key, Command.ERROR, ErrorMessage.USER_ALREADY_EXISTS.toString());
            LOG.debug("Пользователь " + user + " уже авторизирован!");
            System.out.println(users);
            return false;
        }

        return true;
    }

    private SelectionKey getUserSelectionKey(String user) {
        Set<Map.Entry<SelectionKey, String>> entries = users.entrySet();
        for (Map.Entry<SelectionKey, String> entry : entries) {
            if (entry.getValue().equals(user)) {
                return entry.getKey();
            }
        }

        return null;
    }

    /*
    * Методы отправки сообщений
    * */

    private void sendMassage(SelectionKey key, ByteBuffer buffer) {
        SocketChannel user = (SocketChannel) key.channel();

        try {
            user.write(buffer);
            Thread.sleep(0, 500);
        } catch (IOException ex) {
            LOG.error("Ошибка при отправке пакета!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendMassage(SelectionKey user, Message message) {
        ByteBuffer userBuffer = prepareBuffer(user);
        userBuffer.clear();

        String text = gson.toJson(message);
        try {
            userBuffer.put(text.getBytes("UTF-8")).flip();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        sendMassage(user, userBuffer);
    }

    private synchronized void sendServerMassage(SelectionKey user, Command command, String mes) {
        Message msg = new Message(command, serverTitle, new Date(), mes);
        this.serverBuffer.clear();

        String text = gson.toJson(msg);
        try {
            this.serverBuffer.put(text.getBytes("UTF-8")).flip();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sendMassage(user, this.serverBuffer);
    }

    public synchronized void sendServerMessage(String user, String message) {
        SelectionKey key = getUserSelectionKey(user);

        if (key != null) {
            sendServerMassage(key, Command.PUBLIC_MESSAGE, message);
        } else {
            LOG.error("User " + user + " not found!");
        }
    }

    /*
    * Send broadcast message
    * */

    private void sendBroadcastMessage(ByteBuffer buffer) {
        users.forEach((k, v) -> sendMassage(k, buffer));
    }

    private void sendBroadcastMessage(Message message) {
        users.forEach((k, v) -> sendMassage(k, message));
    }

    public synchronized void sendServerBroadcastMessage(String message, Command command) {
        users.forEach((k, v) -> sendServerMassage(k, command, message));
    }

    /*
    * Getters and Setters
    * */

    public ConcurrentLinkedQueue<Message> getMessages() {
        return messages;
    }

    public boolean isRunning() {
        return statusFlag;
    }

    public boolean isShowWelcomeMessage() {
        return showWelcomeMessage;
    }

    public void setShowWelcomeMessage(boolean showWelcomeMessage) {
        this.showWelcomeMessage = showWelcomeMessage;
    }

    public void setServerTitle(String serverTitle) {
        this.serverTitle = serverTitle;
    }

    public void setWelcomeMessageText(String welcomeMessageText) {
        this.welcomeMessageText = welcomeMessageText;
    }

    private void closeClientConnection(SelectionKey key) {
        try {
            SocketChannel user = (SocketChannel) key.channel();
            user.close();
        } catch (IOException e) {
            LOG.error("Ошибка при закрытии сервера!");
        }

        key.cancel();

        String login = users.get(key);
        users.remove(key);

        if (login != null) {
            String message = login + " logout.";

            LOG.info(message);

            sendServerBroadcastMessage(login, Command.DISCONECT_USER);
            sendServerBroadcastMessage(message, Command.PUBLIC_MESSAGE);
        }
    }

    @Override
    public void close() throws IOException {
        LOG.debug("Сервер закрывается");
        statusFlag = false;
        if (selector != null) {
            selector.wakeup();
        }

        for (ServerSocketChannel channel : channels) {
            if (channel != null && channel.isOpen()) {
                LOG.debug("Channel " + channel + " closed.");
                channel.socket().close();
                channel.close();
            }
        }

        if (selector != null && selector.isOpen()) {
            selector.close();
        }
    }

    @Override
    public String toString() {
        return "Server{" +
                "serverTitle='" + serverTitle + '\'' + "\n" +
                "welcomeMessageText='" + welcomeMessageText + '\'' + "\n" +
                "showWelcomeMessage=" + showWelcomeMessage + "\n" +
                "BUFFER_SIZE=" + BUFFER_SIZE + "\n" +
                "serverBuffer=" + serverBuffer + "\n" +
                "users=" + users + "\n" +
                "messages=" + messages + "\n" +
                "ports=" + Arrays.toString(ports) + "\n" +
                "selector=" + selector + "\n" +
                "channels=" + Arrays.toString(channels) + "\n" +
                "statusFlag=" + statusFlag + "\n" +
                "loginLogic=" + loginLogic +
                '}';
    }
}
