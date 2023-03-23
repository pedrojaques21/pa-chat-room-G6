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

    private static Map<Integer,Socket> connectedClients;

    private final ReentrantLock lockQueueReplies;

    public static int getClientMax() {

        return maxClients;
    }


    /**
     * Each Server is constructed using the port number where it will be connected.
     *
     * @param port is the port where the server is connected.
     */
    public ServerThread ( int port, int maxClients,int nWorkers ) {
        this.port = port;
        this.maxClients = maxClients;
        connections = new ArrayList<>();
        connectedClients = new HashMap<>(maxClients);
        this.lockQueueReplies = new ReentrantLock( );
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

    public void broadcastMessage(int action,int id,String message) throws IOException {
        for(int idReceived : connectedClients.keySet()){
            int idComparar = idReceived;
            if(idComparar == id){

            }
        }
        for (int key : connectedClients.keySet()) {
            int idClient = key;
            Socket client = connectedClients.get(key);
            if(client.isConnected()) {
                PrintWriter sendMessage = new PrintWriter(client.getOutputStream(), true);
                if(action == 1) {
                    if(idClient != id) {
                        sendMessage.println("Client " + id + ": " + message + " sent to the client " + idClient);
                    }
                } else if (action == 2) {
                    sendMessage.println(message);
                }
            }
        }
    }

    public void sendMessage(String message,Socket client) throws IOException {
        PrintWriter sendMessage = new PrintWriter(client.getOutputStream(), true);

        sendMessage.println("Server: " + message);
    }

    @Override
    public void run ( ) {
      while (true) {
          try {
                  Socket clientSocket = server.accept();
                  ClientHandler handler = new ClientHandler(clientSocket);
                  connections.add(handler);
                  executor.submit(handler);
          } catch (Exception e) {
              e.printStackTrace();
          }
      }
    }

    private void processReplies ( ) {
        Thread t = new Thread ( ( ) -> {
            while ( true ) {
                lockQueueReplies.lock ( );
                if(queue.size()<maxClients){
                    System.out.println("Estou aqui");
                    Socket client = queue.poll ( );
                    ClientHandler handler = new ClientHandler(client);
                    connections.add(handler);
                    executor.submit(handler);
                }
                lockQueueReplies.unlock ( );
            }
        } );
        t.start ( );
    }

        class ClientHandler implements Runnable{

        private Socket client;
        private boolean running;
        public ClientHandler(Socket client){
            this.client = client;
            this.running = true;
        }

        @Override
        public void run() {
            try (DataInputStream in = new DataInputStream(client.getInputStream());
                 PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {
                while (running) {
                    String message = in.readUTF();
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
                            if(maxClientsSem.tryAcquire()) {
                                connectClient(Integer.parseInt(id), client);
                            }else{

                                queue.add(client);
                                for(Socket cli: queue){
                                    System.out.println("EM ESPERA: " + cli);
                                }
                                sendMessage("Server is full, wait for someone to quit!",client);
                            }
                            break;
                        case "MESSAGE":
                            broadcastMessage(1,Integer.parseInt(id),msgReceived);
                            break;
                        case "REMOVE":
                            removeClient(Integer.parseInt(id));
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void connectClient(int id, Socket client) throws IOException {
            connectedClients.put(id,client);
            broadcastMessage(2,id,"Server: A client with id " + id + " connected to the server!\n");
            for(Map.Entry<Integer, Socket> cli : connectedClients.entrySet()){
                System.out.println("ID: " + cli.getKey() + " Socket: " + cli.getValue());
            }

        }

        public void removeClient(int id) throws IOException {
            for(Map.Entry<Integer, Socket> cli : connectedClients.entrySet()){
                if(cli.getKey() == id) {
                    System.out.println("Removing the client with id: " + cli.getKey());
                    connectedClients.remove(cli.getKey());
                    broadcastMessage(2,cli.getKey(),"Server: Client " + cli.getKey()+ " left the chat!\n");
                }else{
                    broadcastMessage(2,id,"Server: Does not existe a client with that ID!\n");
                }
            }
        }

        public void stopThread(){
            System.out.println("STOPPING");
            running = false;
            interrupt();
        }

    }


}

