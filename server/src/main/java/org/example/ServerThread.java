package org.example;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ServerThread class represents the Server that will serve the Clients
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

    public static int getClientMax() {
        return maxClients;
    }

    /**
     * Each Server is constructed using the port number where it will be connected and the
     * maximum number of clients it can handle.
     *
     * @param port is the port where the server is connected
     * @param maxClients is the maximum number of clients that the server supports
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

    public void sendMessage(String message, Socket client, int idRecebido, int action) throws IOException {
        if (action == 1) {
            PrintWriter sendMessage = new PrintWriter(client.getOutputStream(), true);
            sendMessage.println("Server: " + message);
        } else if (action == 2) {
            PrintWriter sendMessage = new PrintWriter(client.getOutputStream(), true);
            sendMessage.println("Server: " + message);

        }
    }

    @Override
    public void run() {
        while (!server.isClosed()) {
            processRequest();

        }
    }

    public void closeServerSocket() {
        try {
            if (server != null) {
                server.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void processRequest() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    if (!queue.isEmpty() && connectedClients.size() < maxClients) {
                        processReplies();
                    }
                    Socket clientSocket = server.accept();
                    ClientHandler handler = new ClientHandler(clientSocket, 0);
                    connections.add(handler);
                    executor.submit(handler);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    private void processReplies() throws IOException {
        Thread t = new Thread(()->{
            while (true) {
                lockQueueReplies.lock();
                    try {
                        ClientHandler client = queue.peek();
                        if (client != null) {
                            ClientHandler handler = new ClientHandler(client.client, client.id);
                            queue.poll(); // remove the client from the queue after assigning it to a thread
                            maxClientsSem.acquire();
                            handler.connectClient(client.id,client.client);
                            connections.add(handler);
                            executor.submit(handler);

                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                lockQueueReplies.unlock();
            }
        });
        t.start();
    }

    class ClientHandler implements Runnable {

        private static Semaphore semFilter = new Semaphore(1);
        private static ReentrantLock lockFilter = new ReentrantLock();

        private Socket client;
        DataInputStream in;
        PrintWriter out;

        private int id;

        private boolean running;

        private HashSet<String> filterWords = new HashSet<>();    // to store filter words

        public ClientHandler(Socket client,int id) throws IOException {
            this.client = client;
            this.running = true;
            this.in = new DataInputStream(client.getInputStream());
            this.out = new PrintWriter(client.getOutputStream(),true);
            this.id = id;
        }

            @Override
            public void run () {
                try {
                    String message;
                    while (running) {
                        message = in.readUTF();
                        System.out.println("\n***** " + message + " *****");
                        String[] messageComponents = message.split("\\s+");
                        // Extract the message components
                        String action = messageComponents[0];
                        String id = messageComponents[1];
                        String msgReceived = message.substring(message.indexOf(messageComponents[2]));

                        //Filtering messages; replacing forbidden words by "***"
                        readFilterFile("server/filter.txt");
                        lockFilter.lock();
                        for (String str : filterWords) {    // iteration through the HashSet filterWords
                            if (msgReceived.contains(str)) {
                                msgReceived = msgReceived.replace(str, "***");  // and word replacements
                            }
                        }
                        lockFilter.unlock();
                        switch (action) {
                            case "CREATE":
                                if (!checkId(Integer.parseInt(id))) {
                                    if (maxClientsSem.tryAcquire()) {
                                        this.id = Integer.parseInt(id);
                                        connectClient(Integer.parseInt(id), client);
                                    } else {
                                        if (!checkIdQueue(Integer.parseInt(id))) {
                                            this.id = Integer.parseInt(id);
                                            queue.add(this);
                                            System.out.println("TESTANDO: " + this);
                                            for (ClientHandler cli : queue) {
                                                System.out.println("EM ESPERA: " + cli.id + " SOCK: " + cli.client);
                                            }
                                            sendMessage("Server is full, wait for someone to quit!", client, Integer.parseInt(id), 1);
                                        } else {
                                            sendMessage("Already exists a client waiting with that id! Choose another one.", client, Integer.parseInt(id), 1);
                                        }
                                    }
                                } else {
                                    sendMessage("Already exists a client with that id! Choose another one.", client, Integer.parseInt(id), 1);
                                }
                                break;
                            case "MESSAGE":
                                broadcastMessage(1, Integer.parseInt(id), msgReceived);
                                break;
                            case "REMOVE":
                                maxClientsSem.release();
                                removeClient(Integer.parseInt(id), client);
                                stopThread();
                                break;
                            case "CHANGE":
                                changeConnection(this, Integer.parseInt(id));
                                break;
                            case "CHANGEWAITING":
                                if (!checkIdQueue(Integer.parseInt(id))) {
                                    for (ClientHandler cli : queue) {
                                        this.id = Integer.parseInt(id);
                                        queue.add(this);
                                        sendMessage("Server is full, wait for someone to quit!", client, Integer.parseInt(id), 1);
                                    }
                                } else {
                                    sendMessage("Already exists a client waiting with that id! Choose another one.", client, Integer.parseInt(id), 1);
                                }
                                break;
                        }
                    }
                } catch (
                        IOException e) {
                    e.printStackTrace();
                }
            }

            public void connectClient ( int id, Socket client) throws IOException {
                connectedClients.put(id, client);
                broadcastMessage(2, id, "Server: A client with id " + id + " connected to the server!");
                for (Map.Entry<Integer, Socket> cli : connectedClients.entrySet()) {
                    System.out.println("ID: " + cli.getKey() + " Socket: " + cli.getValue());
                }
            }

            public boolean checkId ( int id){

                return connectedClients.containsKey(id);
            }

            public boolean checkIdQueue ( int id){
                for (ClientHandler cli : queue) {
                    if (cli.id == id) {
                        return true;
                    }
                }
                return false;
            }

            public void changeConnection (ClientHandler clientToChange,int idToChangeTo) throws IOException {
                for (ClientHandler client : connections) {
                    if (client == clientToChange) {
                        if (!checkId(idToChangeTo)) {
                            client.id = idToChangeTo;
                            connectClient(idToChangeTo, clientToChange.client);
                        } else {
                            sendMessage("Already exists a client with that id! Choose another one.", clientToChange.client, idToChangeTo, 1);
                        }
                    }

                }
            }

            public void removeClient ( int id, Socket client) throws IOException {
                for (Map.Entry<Integer, Socket> cli : connectedClients.entrySet()) {
                    if (cli.getKey() == id) {
                        broadcastMessage(2, cli.getKey(), "Server: Client " + cli.getKey() + " left the chat");
                        connectedClients.remove(cli.getKey());
                        try {
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

            public void stopThread () {
                running = false;
            }

            private void readFilterFile (String filterPath) {
                File original = new File(filterPath);
                try {
                    semFilter.acquire();
                    Scanner reader = new Scanner(original);
                    while (reader.hasNextLine()) {
                        String word = reader.nextLine();
                        filterWords.add(word);  // adding filter words to the HashSet filterWords
                    }
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    semFilter.release();
                }
            }

        }

    }





