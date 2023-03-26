package org.example;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


/**
 * ClientThread class represents each client that will interact with the server
 */
public class ClientThread extends Thread {

    private final ReentrantLock reentrantLock;
    private final LoggerThread logger;
    private boolean running;
    private int port;
    private int id;
    private DataOutputStream out;
    private BufferedReader in;
    private Socket socket;
    private boolean changeID;

    /**
     * Each Client is constructed using 3 parameters, port, id and freq.
     *
     * @param port is the port where the client should connect to the server;
     *             id is the unique identifier of the client;
     */
    public ClientThread(int id, int port, ReentrantLock reentrantLock, LoggerThread logger) {
        this.port = port;
        this.id = id;
        this.reentrantLock = reentrantLock;
        this.running = true;
        this.logger = logger;
        this.changeID = false;
        try {
            socket = new Socket("localhost", port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public long getId() {

        return this.id;
    }

    /**
     * Creating Client
     */
    public void createClient(ClientThread client) {
        try {
            reentrantLock.lock();
            sendMessage(1, client.id, "Foi criado um cliente!");
        } finally {
            reentrantLock.unlock();
        }

    }

    public void sendMessage(int action, int id, String message) {
        try {
            if (action == 1) {
                reentrantLock.lock();
                out.writeUTF("CREATE" + " " + id + " " + message);
                out.flush();
                logger.logMessage("CREATE" + " " + id + " " + "CONNECTED");
                reentrantLock.unlock();
            } else if (action == 2) {
                reentrantLock.lock();
                out.writeUTF("MESSAGE" + " " + id + " " + message);
                out.flush();
                logger.logMessage("MESSAGE" + " " + id + " " + message);
                reentrantLock.unlock();
            } else if (action == 3) {
                reentrantLock.lock();
                out.writeUTF("REMOVE" + " " + id + " " + message);
                out.flush();
                logger.logMessage("REMOVE" + " " + id + " " + "DISCONNECTED");
                this.running = false;
                shutdown();
                reentrantLock.unlock();
            } else if (action == 4) {
                reentrantLock.lock();
                out.writeUTF("CHANGE" + " " + id + " " + message);
                out.flush();
                logger.logMessage("CHANGE" + " " + id + " " + "CHANGE ID");
                this.changeID = false;
                reentrantLock.unlock();
            } else if (action == 5) {
                reentrantLock.lock();
                out.writeUTF("CHANGEWAITING" + " " + id + " " + message);
                out.flush();
                logger.logMessage("CHANGEWAITING" + " " + id + " " + "CHANGE ID");
                this.changeID = false;
                reentrantLock.unlock();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            createClient(this);
            String messageReceived;
            while (running) {
                clientInput msgInput = new clientInput();
                Thread t = new Thread(msgInput);
                t.start();
                if (socket.isClosed()) {
                    break;
                }
                while ((messageReceived = in.readLine()) != null) {
                    System.out.println(messageReceived);
                    if(messageReceived.equals("Server: Already exists a client with that id! Choose another one.")){
                        reentrantLock.lock();
                        this.changeID = true;
                        logger.logMessage("EXISTINGID" + " " + id + " " + "CHANGE ID");
                        reentrantLock.unlock();
                        changeId(1);
                    } else if (messageReceived.equals("Server: Already exists a client waiting with that id! Choose another one.")) {
                        reentrantLock.lock();
                        this.changeID = true;
                        logger.logMessage("EXISTINGIDWAITING" + " " + id + " " + "CHANGE ID");
                        reentrantLock.unlock();
                        changeId(2);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            shutdown();
        }
    }

    public void changeId(int action) throws IOException {
        while (changeID) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Press ENTER and Insert your new id: ");
            int id = scanner.nextInt();
            scanner.nextLine();
            this.id = id;
            if(action == 1) {
                sendMessage(4, id, "Changing my id ");
            } else if (action ==2) {
                sendMessage(5, id, "Changing my id ");
            }
        }
    }
    public void shutdown() {
        running = false;
        try {
            in.close();
            out.close();
            if (!socket.isClosed()) {
                socket.close();
            }
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class clientInput implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                while (running) {
                    String messageToSend;
                    while((messageToSend = input.readLine()) != null && !changeID) {
                        if (messageToSend.equals("/quit")) {
                            sendMessage(3, id, messageToSend);
                            input.close();
                        }else {
                            sendMessage(2, id, messageToSend);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
