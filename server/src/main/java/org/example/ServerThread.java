package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ServerThread class represents the Server that will serve the Clients
 */
public class ServerThread extends Thread {
    private int port;

    private final ExecutorService executor;

    private Semaphore maxClientsSem;

    private ServerSocket server;

    private Socket socket;

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
     * Each Server is constructed using the port number where it will be connected.
     *
     * @param port is the port where the server is connected.
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
        if(connectedClients.containsKey(id)) {
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
            sendMessage.println("Server: " + message );
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
        Thread t = new Thread(()->{while (true) {
            try {
                if(!queue.isEmpty() && connectedClients.size() < maxClients){
                    processReplies();
                }
                Socket clientSocket = server.accept();
                ClientHandler handler = new ClientHandler(clientSocket,0);
                connections.add(handler);
                executor.submit(handler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }});
        t.start();
    }

    private void processReplies() throws IOException {
        Thread t = new Thread(()->{
            while (true) {
                lockQueueReplies.lock();
                    try {
                        ClientHandler client = queue.peek();
                        if (client != null) {
                            System.out.println("CLI " + client);
                            ClientHandler handler = new ClientHandler(client.client, client.id);
                            queue.poll(); // remove the client from the queue after assigning it to a thread
                            maxClientsSem.acquire();
                            handler.connectClient(client.id,client.client);
                            connections.add(handler);
                            executor.submit(handler);

                        } else {
                            // queue is empty, handle it accordingly
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

        private Socket client;
        DataInputStream in;
        PrintWriter out;

        private int id;

        private boolean running;

        public ClientHandler(Socket client,int id) throws IOException {
            this.client = client;
            this.running = true;
            this.in = new DataInputStream(client.getInputStream());
            this.out = new PrintWriter(client.getOutputStream(),true);
            this.id = id;
        }

        @Override
        public void run() {
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
                    switch (action) {
                        case "CREATE":
                            if(!checkId(Integer.parseInt(id))) {
                                if (maxClientsSem.tryAcquire()) {
                                    this.id = Integer.parseInt(id);
                                    connectClient(Integer.parseInt(id), client);
                                } else {
                                    this.id = Integer.parseInt(id);
                                    queue.add(this);
                                    System.out.println("TESTANDO: " + this);
                                    for (ClientHandler cli : queue) {
                                        System.out.println("EM ESPERA: " + cli.id + " SOCK: " + cli.client);
                                    }
                                    sendMessage("Server is full, wait for someone to quit!", client, Integer.parseInt(id), 1);
                                }
                            }else{
                                sendMessage("Already exists a client with that id! Choose another one.", client, Integer.parseInt(id), 1);
                            }
                            break;
                        case "MESSAGE":
                            broadcastMessage(1, Integer.parseInt(id), msgReceived);
                            break;
                        case "REMOVE":
                            removeClient(Integer.parseInt(id), client);
                            maxClientsSem.release();
                            stopThread();
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void connectClient(int id, Socket client) throws IOException {
            connectedClients.put(id, client);
            broadcastMessage(2, id, "Server: A client with id " + id + " connected to the server!");
            for (Map.Entry<Integer, Socket> cli : connectedClients.entrySet()) {
                System.out.println("ID: " + cli.getKey() + " Socket: " + cli.getValue());
            }

        }

        public boolean checkId(int id) {
            return connectedClients.containsKey(id);
        }

        public void removeClient(int id, Socket client) throws IOException {
            for (Map.Entry<Integer, Socket> cli : connectedClients.entrySet()) {
                if (cli.getKey() == id) {
                    connectedClients.remove(cli.getKey());
                    broadcastMessage(2, cli.getKey(), "Server: Client " + cli.getKey() + " left the chat, message to client ");
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

        public void stopThread() {
            System.out.println("STOPPING");
            running = false;
        }

    }


}

