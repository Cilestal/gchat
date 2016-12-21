package org.ml.gchat.engine;

import com.google.gson.Gson;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Date: 10.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public class Client implements Runnable {
    private static final int MESSAGE_MAX_LENGTH = 1024;
    private static final int BUFFER_SIZE = 2048;
    private static final int MAX_BUFFER_SIZE = BUFFER_SIZE * 10;

    private final ByteBuffer clientBuffer;

    private String userName;
    private String password;

    private final String host;
    private final int port;

    private SocketChannel clientChannel;
    private Selector selector;

    private int status;
    private static final int RUNNABLE = 3;
    private static final int AUTH = 2;
    private static final int CONNECTED = 1;
    private static final int NEW = 0;
    private static final int TERMINATED = -1;

    private ConcurrentLinkedQueue<Message> messages;
    private final Gson gson;

    private static final Logger LOG = Logger.getLogger(Client.class);

    public Client(String host, int port) {
        this(host, port, "");
    }

    public Client(String host, int port, String userName) {
        this(host, port, userName, "");
    }

    public Client(String host, int port, String userName, String password) {
        this.host = host;
        this.port = port;
        this.clientBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.userName = userName;
        this.password = password;
        this.messages = new ConcurrentLinkedQueue<>();
        this.status = NEW;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        if (!isConnected()) {
            connect();
        }

        try {
            this.status = RUNNABLE;
            while (isRunning()) {
                selector.select();

                if (!isRunning()) return;

                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key : selectionKeys) {
                    selectionKeys.remove(key);

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isReadable()) {
                        read(key);
                    }
                }
            }
        } catch (IOException e) {
            LOG.fatal("Ошибка при работе клиента!", e);
            saveFatalError(ErrorMessage.RUNTIME_ERROR);
        }
    }

    /**
     * Выполняет присоеденение к серверу по заданному адресу
     * Изменяет статус клиента на {@see CONNECTED}
     */
    public void connect() {
        if (isConnected()) {
            LOG.error("Клиент уже подсоеденен к серверу!");
            return;
        }

        try {
            this.selector = Selector.open();
            InetSocketAddress hostAddress = new InetSocketAddress(host, port);
            this.clientChannel = SocketChannel.open(hostAddress);
            this.clientChannel.configureBlocking(false);
            this.clientChannel.register(selector, SelectionKey.OP_READ);
            LOG.debug("Подключение к серверу прошло успешно!");
            this.status = CONNECTED;
        } catch (IOException e) {
            LOG.fatal("Ошибка при попытке присоеденения к серверу!", e);
            saveFatalError(ErrorMessage.CONNECTION_ERROR);
        }
    }

    /**
     * Отсылает запрос на авторизацию пользователя.
     *
     * @throws Exception Возникает, если клиент не подключен к серверу.
     */
    public void auth() throws Exception {
        if (!isConnected()) throw new Exception("Сначала присоеденитесь к серверу!");

        sendCommand(Command.USER_AUTH, password);
        LOG.debug("Отправка пакетов для авторизации пользователя.");
        this.status = AUTH;
    }

    /**
     * Выполняет регистрацию пользователя.
     *
     * @throws Exception Возникает, если клиент не подключен к серверу.
     */
    public void register() throws Exception {
        if (!isConnected()) throw new Exception("Сначала присоеденитесь к серверу!");

        sendCommand(Command.REGISTER, password);
        LOG.debug("Отправка регистрационных данных.");
    }

    public void close() {
        LOG.debug("Клиент останавливается.");

        this.status = TERMINATED;

        try {
            if (selector.isOpen()) {
                selector.wakeup();
                selector.close();
            }
        } catch (Exception e) {
            LOG.error("Ошибка при закрытии селектора!");
        }

        try {
            if (clientChannel != null && clientChannel.isOpen()) {
                clientChannel.close();
            }
        } catch (Exception e) {
            LOG.error("Ошибка при закрытии канала!!");
        }
    }

    /*
    * Методы чтения сообщений
    * */

    private ByteBuffer prepareBuffer(SelectionKey key) {
        ByteBuffer buffer;
        if (key.attachment() == null) {
            buffer = ByteBuffer.allocate(BUFFER_SIZE);
            key.attach(buffer);
        } else {
            buffer = (ByteBuffer) key.attachment();
            buffer.clear();
        }

        return buffer;
    }

    private void read(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = prepareBuffer(key);

        int mesLen;
        StringBuilder sb = new StringBuilder();

        try {
            do {
                mesLen = client.read(buffer);

                if (mesLen == -1) {
                    break;
                }

                // TODO: 18.11.2016 ByteBuffer -> CharBuffer
                String str = new String(buffer.array(), 0, mesLen, "UTF-8").trim();
                sb.append(str);
            } while (mesLen > 0 && sb.length() < MAX_BUFFER_SIZE);

            if(sb.length() > 0) {
                readMessages(sb.toString());
            }

            if (mesLen == -1) {
                throw new Exception("The server rejected the connection.");
            }
        } catch (IOException e) {
            LOG.error("Reading problem, closing connection");
            saveFatalError(ErrorMessage.READING_PROBLEM);
            key.cancel();
            close();
        } catch (Exception e) {
            LOG.error(e.getMessage());
            key.cancel();

            System.err.println(sb.toString());
            close();
        }
    }

    private void readMessages(String messages) {
        String[] mesArray = messages.split("(?<=[}])");

        for (String mes : mesArray) {
            Message message = gson.fromJson(mes, Message.class);
            readMessage(message);
        }
    }

    private void readMessage(Message message) {
        if (message != null) {
            messages.add(message);

            Command command = message.getCommand();

            switch (command) {
                case ERROR:
                    LOG.error(message.getMessage());
                    close();
                    break;
            }
        } else {
            LOG.warn("Пользователь получил битый пакет!");
        }
    }

    private void saveFatalError(ErrorMessage error) {
        this.status = TERMINATED;
        Message errorMessage = new Message(Command.ERROR, "", new Date(), error.toString());
        messages.add(errorMessage);
    }

    /*
    * Методы отправки сообщений
    * */

    private void sendCommand(Command command, String mes) {
        if (mes.length() > MESSAGE_MAX_LENGTH) {
            mes = mes.substring(0, MESSAGE_MAX_LENGTH);
        }

        Message msg = new Message(command, userName, new Date(), mes);
        sendMessage(msg);
    }

    public void sendPublicMassage(String mes) {
        sendCommand(Command.PUBLIC_MESSAGE, mes);
    }

    public synchronized void sendPrivateMessage(String recipient, String message) {
        if (message.length() > MESSAGE_MAX_LENGTH) {
            message = message.substring(0, MESSAGE_MAX_LENGTH);
        }

        Message msg = new Message(Command.PRIVATE_MESSAGE, recipient, new Date(), message);
        sendMessage(msg);
    }

    private void sendMessage(Message message) {
        try {
            this.clientBuffer.clear();
            String text = gson.toJson(message);
            this.clientBuffer.put(text.getBytes("UTF-8")).flip();
            this.clientChannel.write(clientBuffer);
        } catch (IOException e) {
            LOG.error("Ошибка при отправке сообщения!");
        }
    }

    /*
    * Client status methods
    * */

    public boolean isConnected() {
        return status >= CONNECTED;
    }

    public boolean isRunning() {
        return status == RUNNABLE;
    }

    public boolean isAuth() {
        return status >= AUTH;
    }

    public boolean isTerminated() {
        return status == TERMINATED;
    }

    public int getStatus() {
        return status;
    }

    /*
    * Getters and setters
    * */

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        if (!isAuth()) {
            this.userName = userName;
        } else {
            LOG.error("Нельзя изменять имя пользователя во время выполнения!");
        }
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ConcurrentLinkedQueue<Message> getMessages() {
        return messages;
    }

    @Override
    public String toString() {
        return "Client{" +
                "userName='" + userName + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", clientChannel=" + clientChannel +
                ", selector=" + selector +
                ", status=" + status +
                '}';
    }
}