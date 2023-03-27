package org.example;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;
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
     * Constructor of the ClientThread. It is a thread that represents the client.
     * @param id - id that identifies the client.
     * @param port - port that the client will connect and listen to.
     * @param reentrantLock - Lock used to control access to critical areas.
     * @param logger - thread used to write on the log file
     */
    public ClientThread(int id, int port, ReentrantLock reentrantLock, LoggerThread logger) {
        this.port = port;
        this.id = id;
        this.reentrantLock = reentrantLock;
        //represents if the client is still active
        this.running = true;
        this.logger = logger;
        //represents if the client is changing its id -> soo that it wont receive messages from server
        this.changeID = false;
        try {
            socket = new Socket("localhost", port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Getter of the socket of the client
     * @return its socket
     */
    public Socket getSocket() {

        return socket;
    }


    /**
     * Getter of the id of the client
     * @return its id
     */
    public long getId() {

        return this.id;
    }

    /**
     * Method used to create a client and send the request to the server
     */
    public void createClient(ClientThread client) {
        sendMessage(1, client.id, "Foi criado um cliente!");
    }

    /**
     * Method responsible to interact with the server, depending on the action received.
     * @param action - action received.
     * @param id - id of the client that is sending the message.
     * @param message - message to be sent
     */
    public void sendMessage(int action, int id, String message) {
        try {
            if (action == 1) {//A client connected
                reentrantLock.lock();
                out.writeUTF("CREATE" + " " + id + " " + message);
                out.flush();
                logger.logMessage("CREATE" + " " + id + " " + "CONNECTED");
                reentrantLock.unlock();
            } else if (action == 2) {//A client sent a message
                reentrantLock.lock();
                out.writeUTF("MESSAGE" + " " + id + " " + message);
                out.flush();
                logger.logMessage("MESSAGE" + " " + id + " " + message);
                reentrantLock.unlock();
            } else if (action == 3) {//A client quit
                reentrantLock.lock();
                out.writeUTF("REMOVE" + " " + id + " " + message);
                out.flush();
                logger.logMessage("REMOVE" + " " + id + " " + "DISCONNECTED");
                this.running = false;
                shutdown();
                reentrantLock.unlock();
            } else if (action == 4) {//A client changed its id
                reentrantLock.lock();
                out.writeUTF("CHANGE" + " " + id + " " + message);
                out.flush();
                logger.logMessage("CHANGE" + " " + id + " " + "CHANGE ID");
                this.changeID = false;
                reentrantLock.unlock();
            } else if (action == 5) {//A client on the waiting queue changed its id
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

    /**
     * Run method that represents an infinite loop waiting for messages from the server.
     * Before entering the loop it creates the client.
     * Also creates a thread to a class {@link clientInput} that handles the input of the client.
     */
    @Override
    public void run() {
        try {
            createClient(this);
            String messageReceived;
            while (running) {
                //creating the thread that will handle the clients input
                clientInput msgInput = new clientInput();
                Thread t = new Thread(msgInput);
                t.start();
                if (socket.isClosed()) {
                    break;
                }
                //checks if the server sent a message
                while ((messageReceived = in.readLine()) != null) {
                    System.out.println(messageReceived);
                    //if the message says that the given id already exists
                    if(messageReceived.equals("Server: Already exists a client with that id! Choose another one.")){
                        reentrantLock.lock();
                        this.changeID = true;
                        //writes on the log
                        logger.logMessage("EXISTINGID" + " " + id + " " + "CHANGE ID");
                        reentrantLock.unlock();
                        //changes the id
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

    /**
     * Method responsible to change the id of client that entered a id that already existed.
     * @param action - Represents where the client is when changing the id (1 -> Going to connect; 2 -> Waiting on the queue to connect)
     */
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

    /**
     * Method responsible to remove the client, terminating all its tasks.
     */
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

    /**
     * Class that represents an active thread waiting for the client input.
     * Then processes it and calls {@link ClientThread#sendMessage(int, int, String)}
     */
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
