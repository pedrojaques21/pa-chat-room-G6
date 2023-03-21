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
            server = new ServerSocket ( 8080 );
        } catch ( IOException e ) {
            e.printStackTrace ( );
        }
    }

    public static void broadcastMessage(String message) throws IOException {

        for (int key : connectedClients.keySet()) {
            Socket client = connectedClients.get(key);
            OutputStream cliOut = client.getOutputStream();
            OutputStreamWriter cliOutWrit = new OutputStreamWriter(cliOut);
            BufferedWriter bw = new BufferedWriter(cliOutWrit);
            bw.write(message);
            bw.flush();
        }
    }

    @Override
    public void run ( ) {
        processRequests();
    }
    private void processRequests ( ) {
        Thread t = new Thread ( ( ) -> {
            while ( true ) {
                try {
                    // Reads the request
                    Socket client = server.accept ( );
                    ClientHandler clientHandler = new ClientHandler(client);
                    executor.submit(clientHandler);
                } catch ( IOException e ) {
                    throw new RuntimeException ( e );
                }
            }
        } );
        t.start ( );
    }



    class ClientHandler implements Runnable{

        private Socket client;
        private DataInputStream  in;
        private PrintWriter out;
        public ClientHandler(Socket client){
            this.client = client;
        }
        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(),true);
                in = new DataInputStream(client.getInputStream());

                while (true) {
                    String messageRecieved = in.readUTF();
                    String[] parts = messageRecieved.split(" ");
                    String action = parts[0];
                    String id = parts[1];
                    if(Objects.equals(action, "CREATE")){
                        if (checkServerSize()) {
                            connectClient(Integer.parseInt(id), client);
                        } else {
                            sendMessageToClient("Server is Full!\n");
                        }
                    } else if (Objects.equals(action, "MESSAGE")) {
                        System.out.println("Vou enviar mensagem!\n");
                        broadcastMessage(messageRecieved);
                    }
/**
                    switch (action) {
                        case "CREATE":
                            if (checkServerSize()) {
                                connectClient(Integer.parseInt(id), client);
                            } else {
                                sendMessageToClient("Server is Full!\n");
                            }
                            break;
                        case "MESSAGE":
                            System.out.println("Vou enviar mensagem!\n");
                            broadcastMessage(messageRecieved);
                            break;
                        default:
                            break;
                    }
 */
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendMessageToClient(String message){
            out.println(message);
        }

        public void connectClient(int id, Socket client){
            connectedClients.put(id,client);
            for(Map.Entry<Integer, Socket> cli : connectedClients.entrySet()){
                System.out.println("ID: " + cli.getKey() + " Socket: " + cli.getValue());
            }
        }


        public boolean checkServerSize() throws IOException {
            if(connectedClients.size() < maxClients){
                System.out.println("Entrou no check");
                return true;
            }else{
                return false;
            }
        }
    }


}

