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

    private final Queue<Socket> queue;

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
        for (int key : connectedClients.keySet()) {
            int idClient = key;
            Socket client = connectedClients.get(key);
            if (!client.isClosed()) {
                PrintWriter sendMessage = new PrintWriter(client.getOutputStream(), true);
                if (action == 1) {
                    if (idClient != id) {
                        sendMessage.println("Client " + id + ": " + message + ", sent to the client " + idClient);
                    }
                } else if (action == 2) {
                    sendMessage.println(message + idClient);
                }
            }
        }
    }

    public void sendMessage(String message, Socket client, int idRecebido, int action) throws IOException {
        if (action == 1) {
            PrintWriter sendMessage = new PrintWriter(client.getOutputStream(), true);
            sendMessage.println("Server: " + message + ", sento to client " + idRecebido);
        } else if (action == 2) {
            PrintWriter sendMessage = new PrintWriter(client.getOutputStream(), true);
            sendMessage.println("Server: " + message);

        }
    }

    @Override
    public void run() {
        processRequest();
        processReplies();
    }

    public void processRequest() {
        try {
            while (true) {

                while (!queue.isEmpty() && connectedClients.size() < maxClients) {
                    System.out.println("Entrou na queue");
                    Socket client = queue.poll();
                    ClientHandler handler = new ClientHandler(client);
                    connections.add(handler);
                    executor.submit(handler);
                }

                System.out.println("Novo Request");
                Socket clientSocket = server.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                connections.add(handler);
                executor.submit(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processReplies() {
        while (true) {
            lockQueueReplies.lock();
            while (!queue.isEmpty()) {
                System.out.println("Entrou na queue");
                Socket client = queue.poll();
                ClientHandler handler = new ClientHandler(client);
                connections.add(handler);
                executor.submit(handler);
            }
            lockQueueReplies.unlock();
        }
    }


    class ClientHandler implements Runnable {

        private Socket client;
        DataInputStream in;
        PrintWriter out;

        private boolean running;

        public ClientHandler(Socket client) {
            this.client = client;
            this.running = true;
        }

        @Override
        public void run() {
            try {
                in = new DataInputStream(client.getInputStream());
                out = new PrintWriter(client.getOutputStream(), true);
                //   while (running) {
                String message;
                while (running) {
                    message = in.readUTF();
                    System.out.println("***** " + message + " *****");
                    String[] messageComponents = message.split("\\s+");
                    // Extract the message components
                    String action = messageComponents[0];
                    String id = messageComponents[1];
                    String msgReceived = message.substring(message.indexOf(messageComponents[2]));
                    System.out.println("Received this action: " + action);
                    System.out.println("SEM: " + maxClientsSem.availablePermits());
                    switch (action) {
                        case "CREATE":
                            if (maxClientsSem.tryAcquire()) {
                                connectClient(Integer.parseInt(id), client);
                                System.out.println("VOLTEI DPS DO CREATE");
                                stopThread();
                            } else {

                                queue.add(client);
                                for (Socket cli : queue) {
                                    System.out.println("EM ESPERA: " + cli);
                                }
                                sendMessage("Server is full, wait for someone to quit!", client, Integer.parseInt(id), 1);
                                stopThread();
                            }
                            break;
                        case "MESSAGE":
                            if (checkId(Integer.parseInt(id))) {
                                broadcastMessage(1, Integer.parseInt(id), msgReceived);
                                stopThread();
                            } else {
                                broadcastMessage(2, Integer.parseInt(id), "Server: There is no client with id " + Integer.parseInt(id) + ", message sent to client ");
                                stopThread();
                            }
                            break;
                        case "REMOVE":
                            if (checkId(Integer.parseInt(id))) {
                                removeClient(Integer.parseInt(id), client);
                                maxClientsSem.release();
                                stopThread();
                            } else {
                                broadcastMessage(2, Integer.parseInt(id), "Server: There is no client with id " + Integer.parseInt(id) + ", message sent to client ");
                                stopThread();
                            }
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void connectClient(int id, Socket client) throws IOException {
            connectedClients.put(id, client);
            broadcastMessage(2, id, "Server: A client with id " + id + " connected to the server! Message to client ");
            for (Map.Entry<Integer, Socket> cli : connectedClients.entrySet()) {
                System.out.println("ID: " + cli.getKey() + " Socket: " + cli.getValue());
            }

        }

        public boolean checkId(int id) {
            boolean idFound = false;
            for (Map.Entry<Integer, Socket> cli : connectedClients.entrySet()) {
                if (cli.getKey() == id) {
                    idFound = true;
                }
            }
            if (idFound) {
                return true;
            } else {
                return false;
            }

        }

        public void removeClient(int id, Socket client) throws IOException {
            boolean encontrou = false;
            for (Map.Entry<Integer, Socket> cli : connectedClients.entrySet()) {
                if (cli.getKey() == id) {
                    encontrou = true;
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
                } else {
                    encontrou = false;
                }
            }
            if (!encontrou) {
                System.out.println("MANDA MSG");
                sendMessage("There is no client with id " + id, client, id, 2);
            }
        }

        public void stopThread() {
            System.out.println("STOPPING");
            running = false;
        }

    }


}

