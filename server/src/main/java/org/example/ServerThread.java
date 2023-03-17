package org.example;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * ServerThread class represents the Server that will serve the Clients
 */
public class ServerThread extends Thread {
    private final int port;
    private DataInputStream in;
    private PrintWriter out;
    private ServerSocket server;
    private Socket socket;

    private static int clientMax = 0;

    private static ArrayList<Thread> clients = new ArrayList<>(clientMax);


    public static int getClientMax() {
        return clientMax;
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
    public ServerThread ( int port ) {
        this.port = port;
        try {
            server = new ServerSocket ( this.port );
        } catch ( IOException e ) {
            e.printStackTrace ( );
        }
    }

    @Override
    public void run ( ) {

        while ( true ) {
            try {
                System.out.println ( "Accepting Data" );
                socket = server.accept ( );
                in = new DataInputStream ( socket.getInputStream ( ) );
                out = new PrintWriter ( socket.getOutputStream ( ) , true );
                String message = in.readUTF ( );
                System.out.println ( "***** " + message + " *****" );
                out.println ( message.toUpperCase ( ) );
            } catch ( IOException e ) {
                e.printStackTrace ( );
            }
        }

    }
}
