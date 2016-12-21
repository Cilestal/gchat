package org.ml.gchat;

import org.ml.gchat.engine.Client;
import org.ml.gchat.engine.Message;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Date: 23.10.2016
 *
 * @author Michael Lang
 * @version 1.0
 */
public class Test {
    public static void main(String[] args) throws InterruptedException {
        Test test = new Test();
        test.start();
    }

    public void start() throws InterruptedException {
        Client[] clients = new Client[100];

        for (int i = 0; i < clients.length; i++) {
            if(Math.random() > 0.5) {
                clients[i] = new Client("127.0.0.1", 5454, "Alb" + i, "1111");
            } else {
                clients[i] = new Client("127.0.0.1", 6868, "Alb" + i, "1111");
            }
            final Client client = clients[i];

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            final int finalI = i;
            new Thread(() -> {
                new Thread(() -> {
                    try {
                        client.connect();
                        client.register();
                        client.auth();
                        System.out.println("Auth client " + finalI);
                        client.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

                while (!client.isRunning()){
                    try {
                        Thread.sleep(1_000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                for (int j = 0; j < 10; j++) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    client.sendPublicMassage("USER [" + finalI +"]" + " = " + j);
                }

                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (finalI == 80){
                    ConcurrentLinkedQueue<Message> messages = client.getMessages();
                    messages.forEach(System.err::println);
                }

                //client.close();
            }).start();
        }
    }


}

