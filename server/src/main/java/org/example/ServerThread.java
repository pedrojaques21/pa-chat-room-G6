package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ServerThread class represents the Server that will serve multiple Clients.
 * <p>
 * This class will use two methods: <b>Master-Slave</b> and <b>PoolThread</b>.
 * <p>
 * The Master ({@link ServerThread}) after receiving requests from the Client, will create slaves({@link ClientHandler}) to attend the request.</p>
 * These Worker threads will be stored in a threadPool used to handle the client connections.
 */
public class ServerThread extends Thread {
    private int port;
    private final ExecutorService executor;
    private Semaphore maxClientsSem;
    private ServerSocket server;
    private boolean finish;
    private static int maxClients;
    private static ArrayList<ClientHandler> connections;
    private final Queue<ClientHandler> queue;
    private static Map<Integer, Socket> connectedClients;
    private final ReentrantLock lockQueueReplies;


    /**
     * Getter of the maximum number of clients that the server can handle.
     *
     * @return the maximum number of clients that the server can handle.
     */
    public static int getClientMax() {

        return maxClients;
    }

    /**
     * Each Server is constructed using the port number where it will be connected, the
     * maximum number of clients it can handle and the number of workers to attend the requests.
     *
     * @param port       is the port number where the server will be listening for incoming connections.
     * @param maxClients is the maximum number of clients that the server can handle.
     * @param nWorkers   is the number of worker threads in the thread pool that will be used to handle the client connections.
     */
    public ServerThread(int port, int maxClients, int nWorkers) {
        this.port = port;
        this.maxClients = maxClients;
        connections = new ArrayList<>();
        connectedClients = new HashMap<>(maxClients);
        this.lockQueueReplies = new ReentrantLock();
        this.executor = Executors.newFixedThreadPool(nWorkers);
        this.queue = new LinkedList<>();
        finish = false;
        try {
            server = new ServerSocket(this.port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        maxClientsSem = new Semaphore(maxClients);
    }

    /**
     * Method used to <b>broadcast the message to all connected clients, except the client that sent the message</b>.
     *
     * @param action  is the type of message to be sent (1 - Client message, 2 - Server message).
     * @param id      is the ID of the client who sent the message.
     * @param message is the message to be sent.
     * @throws IOException if an I/O error occurs while sending the message.
     */
    public void broadcastMessage(int action, int id, String message) throws IOException {
        if (connectedClients.containsKey(id)) {
            for (int key : connectedClients.keySet()) {
                int idClient = key;
                Socket client = connectedClients.get(key);
                if (!client.isClosed()) {
                    PrintWriter sendMessage = new PrintWriter(client.getOutputStream(), true);
                    if (action == 1) {
                        if (idClient != id) {
                            sendMessage.println("Client " + id + ": " + message);
                        }
                    } else if (action == 2) {
                        sendMessage.println(message);
                    }
                }
            }
        }
    }

    /**
     * Method used to Send a message to <b>only a specific client</b>.
     *
     * @param message is the message to be sent.
     * @param client  the socket/connection of the client to send the message.
     * @throws IOException if an I/O error occurs while sending the message.
     */
    public void sendMessage(String message, Socket client) throws IOException {
        PrintWriter sendMessage = new PrintWriter(client.getOutputStream(), true);
        sendMessage.println("Server: " + message);
    }

    /**
     * Run method of {@link ServerThread}
     * <p>
     * It stays in a infinite loop checking for requests from the clients.
     * Once the server is closed, the loop ends.
     */
    @Override
    public void run() {
        while (!server.isClosed()) {
            processRequest();
        }
    }

    /**
     * Active thread that accepts the requests from the client and sends the task to a worker({@link ClientHandler}).
     * It also checks the size of the server, if it is <b>NOT</b> full and there <b>are clients waiting</b>, it should attend those clients {@link ServerThread#processReplies()}.
     *
     * @throws IOException if an I/O error occurs while sending the message.
     */
    public void processRequest() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    //check if there are clients waiting and if there is space for them on the server
                    if (!queue.isEmpty() && connectedClients.size() < maxClients) {
                        processReplies();
                    }
                    //accepts the connection from the client
                    Socket clientSocket = server.accept();
                    //creates a worker to deal with the connection
                    ClientHandler handler = new ClientHandler(clientSocket, 0);
                    //add the connection received to the ArrayList
                    connections.add(handler);
                    //execute the worker asynchronously by a thread in the thread pool
                    executor.submit(handler);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    /**
     * Active thread that processes the clients waiting to connect to the server.
     * If the connection trying to connect is not null, a worker is created {@link ClientHandler}
     *
     * @throws IOException          if an I/O error occurs while sending the message.
     * @throws InterruptedException if the connection is interrupted unexpectedly.
     */
    private void processReplies() throws IOException {
        Thread t = new Thread(() -> {
            while (true) {
                lockQueueReplies.lock();
                try {
                    //removes the first connection from the waiting queue and assigns it to a ClientHandler
                    ClientHandler client = queue.peek();
                    if (client != null) {
                        //creates a Worker with that connection
                        ClientHandler handler = new ClientHandler(client.client, client.id);
                        // remove the client from the queue after assigning it to a thread
                        queue.poll();
                        //increments the semaphore
                        maxClientsSem.acquire();
                        //connects to the server
                        handler.connectClient(client.id, client.client);
                        //adding connection
                        connections.add(handler);
                        //execute the worker asynchronously by a thread in the thread pool
                        executor.submit(handler);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                lockQueueReplies.unlock();
            }
        });
        t.start();
    }

    /**
     * ClientThread class represents the thread that handles the communication with the Client, the Worker or Slave
     */
    class ClientHandler implements Runnable {

        private Socket client;
        DataInputStream in;
        PrintWriter out;

        private int id;

        private boolean running;

        private HashSet<String> filterWords = new HashSet<>();    // to store filter words

        /**
         * Each ClientHandler is constructed using the connection socket of the client trying to connect and its id.
         * It also has a boolean to establish if the thread is still running.
         * Finally it initiates the DataInputStream to receive client messages and PrintWriter to send messages
         *
         * @param client represents the socket connection with the client
         * @param id     represents the id of the client connecting
         * @throws IOException if an I/O error occurs.
         */
        public ClientHandler(Socket client, int id) throws IOException {
            this.client = client;
            this.running = true;
            this.in = new DataInputStream(client.getInputStream());
            this.out = new PrintWriter(client.getOutputStream(), true);
            this.id = id;
        }

        /**
         * Run method of {@link ClientHandler}
         * <p>
         * It stays in an infinite loop checking for messages from the clients.
         * Once the client leaves, the loop ends.
         * It receives the message from the client, breaks it down into action, id and message, then processes it accordingly.
         */
        @Override
        public void run() {
            try {
                String message;
                while (running) {
                    //incoming message from client
                    message = in.readUTF();
                    System.out.println("\n***** " + message + " *****");
                    //splits the message by white spaces
                    String[] messageComponents = message.split("\\s+");
                    // Extract the message components
                    //action -> indicates what the client is trying to do
                    String action = messageComponents[0];
                    //id -> id of the client that sent the message
                    String id = messageComponents[1];
                    //msgReceived -> message written by the client
                    String msgReceived = message.substring(message.indexOf(messageComponents[2]));
                    //Filtering messages; replacing forbidden words by "***"
                    readFilterFile("server/filter.txt");
                    for (String str : filterWords) {    // iteration through the HashSet filterWords
                        if (msgReceived.contains(str)) {
                            msgReceived = msgReceived.replace(str, "*****");  // and word replacements
                        }
                    }
                    switch (action) {
                        case "CREATE"://connects the client to the server
                            //if the id given does not exist already
                            if (!checkId(Integer.parseInt(id))) {
                                //acquire the semaphore
                                if (maxClientsSem.tryAcquire()) {
                                    //assigns the id and connects the client
                                    this.id = Integer.parseInt(id);
                                    connectClient(Integer.parseInt(id), client);
                                } else {//if it cant acquire the semaphore it goes to the waiting queue
                                    //check if the given id does not exist on the queue
                                    if (!checkIdQueue(Integer.parseInt(id))) {
                                        //assigns the id and adds the client to the waiting queue
                                        this.id = Integer.parseInt(id);
                                        queue.add(this);
                                        System.out.println("TESTANDO: " + this);
                                        for (ClientHandler cli : queue) {
                                            System.out.println("EM ESPERA: " + cli.id + " SOCK: " + cli.client);
                                        }
                                        //lets the client know that the server is full and to wait.
                                        sendMessage("Server is full, wait for someone to quit!", client);
                                    } else {
                                        //alerts the client that the given id already exists.
                                        sendMessage("Already exists a client waiting with that id! Choose another one.", client);
                                    }
                                }
                            } else {
                                sendMessage("Already exists a client with that id! Choose another one.", client);
                            }
                            break;
                        case "MESSAGE"://broadcasts a message
                            broadcastMessage(1, Integer.parseInt(id), msgReceived);
                            break;
                        case "REMOVE"://a client just quit
                            //release the semaphore to allow other clients to connect
                            maxClientsSem.release();
                            removeClient(Integer.parseInt(id), client);
                            stopThread();
                            break;
                        case "CHANGE"://changing the id when tried to connect and the id already existed
                            changeConnection(this, Integer.parseInt(id));
                            break;
                        case "CHANGEWAITING"://changing the id when tried to connect to the waiting queue and the id already existed
                            if (!checkIdQueue(Integer.parseInt(id))) {
                                for (ClientHandler cli : queue) {
                                    this.id = Integer.parseInt(id);
                                    queue.add(this);
                                    sendMessage("Server is full, wait for someone to quit!", client);
                                }
                            } else {
                                sendMessage("Already exists a client waiting with that id! Choose another one.", client);
                            }
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Connects a client to the server
         *
         * @param id is the id of the client being connected
         * @param client is the client being connected
         * @throws IOException
         */
        public void connectClient(int id, Socket client) throws IOException {
            //places the clients and its id on the hashmap
            connectedClients.put(id, client);
            //Alerts the client that it connected successfully to the server
            broadcastMessage(2, id, "Server: A client with id " + id + " connected to the server!");
            for (Map.Entry<Integer, Socket> cli : connectedClients.entrySet()) {
                System.out.println("ID: " + cli.getKey() + " Socket: " + cli.getValue());
            }
        }

        /**
         * Method used to check id a given id already exists among the connected clients
         * @param id - given id
         * @return <code>true</code> if it exists and <code>false</code> if it does not.
         */
        public boolean checkId(int id) {
            return connectedClients.containsKey(id);
        }


        /**
         * Method used to check id a given id already exists among the clients waiting on the queue
         * @param id - given id
         * @return <code>true</code> if it exists and <code>false</code> if it does not.
         */
        public boolean checkIdQueue(int id) {
            for (ClientHandler cli : queue) {
                if (cli.id == id) {
                    return true;
                }
            }
            return false;
        }


        /**
         * Method used to change the id of client that entered an id that already existed.
         * @param clientToChange - represents the client that will have its id changed
         * @param idToChangeTo - represents the new id
         * @throws IOException
         */
        public void changeConnection(ClientHandler clientToChange, int idToChangeTo) throws IOException {
            for (ClientHandler client : connections) {
                //searches for the client socket connection of the client that needs to change the id
                if (client == clientToChange) {
                    //checks again if the id already exists
                    if (!checkId(idToChangeTo)) {
                        //Assigns the new id and connects the client
                        client.id = idToChangeTo;
                        connectClient(idToChangeTo, clientToChange.client);
                    } else {
                        //alerts the client if it exists
                        sendMessage("Already exists a client with that id! Choose another one.", clientToChange.client);
                    }
                }

            }
        }

        /**
         * Method when a client quits the chat.
         * @param id - represents the id of the client that is leaving.
         * @param client - represents the socket connection of the client that is leaving
         * @throws IOException
         */
        public void removeClient(int id, Socket client) throws IOException {
            //loops through all connected clients until it finds a match
            for (Map.Entry<Integer, Socket> cli : connectedClients.entrySet()) {
                if (cli.getKey() == id) {
                    //alerts all the clients that someone left the chat
                    broadcastMessage(2, cli.getKey(), "Server: Client " + cli.getKey() + " left the chat");
                    //Removes the client from the connected clients
                    connectedClients.remove(cli.getKey());
                    try {
                        //terminates its connections
                        in.close();
                        out.close();
                        if (!client.isClosed()) {
                            client.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * Method used to stop the running thread, assigning its running value to <code>false</code>
         */
        public void stopThread() {
            running = false;
        }

        /**
         * Method used to scan for forbidden words on the file and add them to the hashSet, soo that the server knows which words it needs to remove
         * @param filterPath - Path to the file that contains the forbidden words
         * @throws Exception
         */
        private void readFilterFile(String filterPath) {
            File original = new File(filterPath);
            try {
                //reads the file of the forbidden words
                Scanner reader = new Scanner(original);
                while (reader.hasNextLine()) {
                    String word = reader.nextLine();
                    filterWords.add(word);  // adding filter words to the HashSet filterWords
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}





