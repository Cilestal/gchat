package org.ml.gchat;

import org.ml.gchat.engine.Client;
import org.ml.gchat.engine.Command;
import org.ml.gchat.engine.ErrorMessage;
import org.ml.gchat.engine.Message;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Date: 14.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public class ConsoleClient {
    public static void main(String[] args) throws Exception {

        Client client = new Client("127.0.0.1", 5454, "Michael3", "1111");

        try {
            new Thread(() -> {
                try {
                    client.connect();
                    client.register();
                    client.auth();
                    client.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            Thread.sleep(5_000);

            if (!client.isRunning()) {
                System.err.println("Connect problem");
                ConcurrentLinkedQueue<Message> messages = client.getMessages();
                for (Message message : messages) {
                    if (message.getCommand() == Command.ERROR) {
                        ErrorMessage error = ErrorMessage.valueOf(message.getMessage());
                        System.out.println(error);
                    }
                }
                return;
            }
            //client.sendPrivateMessage("Michael2", "HELLO MICHAEL!");
            client.sendPublicMassage("ПРИВЕТ WORLD!1");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!2");
            client.sendPublicMassage("HELLO WORLD!3");
            client.sendPublicMassage("HELLO WORLD!4");
            Thread.sleep(5000);

            //client.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(0);
        }

        ConcurrentLinkedQueue<Message> messages = client.getMessages();

        int size = messages.size();
        for (int i = 0; i < size; i++) {
            System.out.println(messages.poll());
        }
    }
}
