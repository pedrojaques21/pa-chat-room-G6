package org.example;
import java.io.*;
import java.net.ServerSocket;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ServerThread class represents the Server that will serve the Clients
 */
public class ServerThread extends Thread {
    private int port;

    private final ExecutorService executor;

    private ServerSocket server;

    private static int maxClients;

    private static ArrayList<ClientHandler> connections;

    private static Map<Integer,Socket> connectedClients;

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
        this.executor = Executors.newFixedThreadPool ( nWorkers );
        try {
            server = new ServerSocket (port);
        } catch ( IOException e ) {
            e.printStackTrace ( );
        }
    }

    public static void broadcastMessage(String message) throws IOException {
        for (int key : connectedClients.keySet()) {
            Socket client = connectedClients.get(key);
            if(client.isConnected()) {
                System.out.println("MESSAGE: "+ message);
                //DataOutputStream out = new DataOutputStream(client.getOutputStream());
                PrintWriter sendMessage = new PrintWriter(client.getOutputStream(),true);
                sendMessage.println(message);
            }
        }
    }

    @Override
    public void run ( ) {
        while (true) {
            processRequests();
        }
    }

    private void processRequests ( ) {
        Thread t = new Thread ( ( ) -> {
            while ( true ) {
                try {
                    // Reads the request
                    Socket client = server.accept ( );
                    ClientHandler clientHandler = new ClientHandler(client);
                    connections.add(clientHandler);
                    executor.submit(clientHandler);
                } catch ( IOException e ) {
                    throw new RuntimeException ( e );
                }
            }
        } );
        t.start ( );
    }



    class ClientHandler implements Runnable{

        private Socket clientHandel;
        //private BufferedReader in;
        private DataInputStream in;

        private PrintWriter out;
        public ClientHandler(Socket client){
            this.clientHandel = client;
        }
        @Override
        public void run() {
        //    while (true) {
                try {
                    //out = new PrintWriter(clientHandel.getOutputStream(), true);
                    //in = new BufferedReader(new InputStreamReader(clientHandel.getInputStream()));
                    in     = new DataInputStream(clientHandel.getInputStream());
                    out    = new PrintWriter(clientHandel.getOutputStream(),true);
                    String message= in.readUTF();
                    System.out.println("***** "+message+" *****");
                    out.println(message.toUpperCase());

                    //String messageRecieved = in.readLine();
                    String[] parts = message.split(" ");
                    String action = parts[0];
                    String id = parts[1];
                    String msgReceived = parts[2];
                    System.out.println("ENTROU AQUI?");
                    System.out.println("Received this action: " + action);
                    out.println("ACTION: " + action);
                    switch (action) {
                        case "CREATE":
                            if (checkServerSize()) {
                                connectClient(Integer.parseInt(id), clientHandel);
                            } else {
                                sendMessageToClient("Server is Full!\n");
                            }
                            break;
                        case "MESSAGE":
                            broadcastMessage(msgReceived);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

          //  }
        }

        public void sendMessageToClient(String message){
            out.println(message);
        }

        public void connectClient(int id, Socket client) throws IOException {
            connectedClients.put(id,client);
            for(Map.Entry<Integer, Socket> cli : connectedClients.entrySet()){
                System.out.println("ID: " + cli.getKey() + " Socket: " + cli.getValue());
            }
        }


        public boolean checkServerSize() throws IOException {
            if(connectedClients.size() < maxClients){
                return true;
            }else{
                return false;
            }
        }
    }


}

