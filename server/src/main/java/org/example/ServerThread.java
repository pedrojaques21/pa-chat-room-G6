package org.example;
import java.io.*;
import java.net.ServerSocket;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * ServerThread class represents the Server that will serve the Clients
 */
public class ServerThread extends Thread {
    private final int port;
    private static DataInputStream in;
    private static PrintWriter out;
    private static ServerSocket server;
    private static Socket socket;

    private static int maxClients;
    static int clientCount = 0;

    private static ArrayList<Integer> clients = new ArrayList<Integer>(maxClients);

    private static ArrayList<Integer> clientsOnPort = new ArrayList<Integer>();


    public static int getClientMax() {
        return maxClients;
    }



    public static void readServerConfig(){
        try {
            File file = new File("server.config");
            Scanner scanner = new Scanner(file);
            int clientMax = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("CLIENT_MAX")) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        clientMax = Integer.parseInt(parts[1].trim());
                        break;
                    }
                }
            }
            scanner.close();
            System.out.println("CLIENT_MAX: " + clientMax);
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }
    }

    /**
     * Each Server is constructed using the port number where it will be connected.
     *
     * @param port is the port where the server is connected.
     */
    public ServerThread ( int port, int maxClients ) {
        this.port = port;
        this.maxClients = maxClients;
        try {
            server = new ServerSocket ( this.port );
        } catch ( IOException e ) {
            e.printStackTrace ( );
        }
    }


    public static void checkServerSize(int id) throws IOException {
        int size = clients.size();
        if(size<=maxClients){
            clients.add(id);
            System.out.println("size:" + clients.size());
        }else{

        }

    }

    /**
    public static void getClientsOnPort(int port) {
        System.out.println("HELLOOOO");

        for (Integer client : clients) {
            System.out.println("HEELO 2");
            if (client.getSocket().getPort() == port) {
                System.out.println("HEELLO 3");
                clientsOnPort.add(client);
            }
        }

    }*/



    @Override
    public void run ( ) {

        while ( true ) {
            try {
                //System.out.println ( "Accepting Data" );
                socket = server.accept ( );
                in = new DataInputStream ( socket.getInputStream ( ) );
                out = new PrintWriter ( socket.getOutputStream ( ) , true );
                String messageRecieved = in.readUTF ( );
                //System.out.println (messageRecieved);
                out.println ( messageRecieved.toUpperCase ( ) );
                String command = messageRecieved.substring(0, messageRecieved.indexOf(' '));
                String numberString = messageRecieved.substring(messageRecieved.indexOf(' ') + 1);

                switch (command){
                    case "1":
                        System.out.println("CREATING A CLIENT");
                        checkServerSize(Integer.parseInt(numberString));

                        break;
                    case "SEND_MESSAGE":
                        System.out.println("SEND A MESSAGE");
                        break;
                    default:
                        System.out.println("NONE");
                        break;
                }


            } catch ( IOException e ) {
                e.printStackTrace ( );
            }
        }

    }
}
