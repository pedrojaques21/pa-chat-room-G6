package org.example;
import java.io.*;
import java.net.ServerSocket;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

/**
 * ServerThread class represents the Server that will serve the Clients
 */
public class ServerThread extends Thread {
    private int port;
    private static DataInputStream in;
    private static PrintWriter out;
    private static ServerSocket server;
    private static Socket socket;

    private static int maxClients;
    static int clientCount = 0;

    private static ArrayList<ClientThread> clients = new ArrayList<>(maxClients);

    private static ArrayList<Socket> clientsConnected = new ArrayList<>();



    public static int getClientMax() {
        return maxClients;
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

    public static void sendtMessage(Socket client, String message) throws IOException {
        in = new DataInputStream ( client.getInputStream ( ) );
        out = new PrintWriter ( client.getOutputStream ( ) , true );
        out.println(message);
    }

    public static void brodcastMessage(String message) {
        for(Socket cli: clientsConnected){
            out.println(message);
        }
    }


    public static void checkServerSize(Socket client) throws IOException {
        int size = clientsConnected.size();
        if(clientsConnected.size()<5){
            clientsConnected.add(client);
            for(Socket clientC: clientsConnected) {
                System.out.print("size:" + clientsConnected.size());
                System.out.print(" Client: " + clientC);
            }
        }else{
            sendtMessage(client,"Server is Full!\n");
        }
    }




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
                        checkServerSize(socket);
                        break;
                    case "2":
                        System.out.println("SEND A MESSAGE");
                        brodcastMessage(command);
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

