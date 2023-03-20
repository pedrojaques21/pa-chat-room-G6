package org.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;


/**
 * ClientThread class represents each client that will interact with the server
 */
public class ClientThread extends Thread {
    private int port;
    private int id;
    private int totalClients;
    private DataOutputStream out;
    private BufferedReader in;
    private Socket socket;




 
    private static ArrayList<ClientThread> clients = new ArrayList<>();


    /**private final Semaphore sem;*/


    /**
     * Each Client is constructed using 3 parameters, port, id and freq.
     *
     * @param port is the port where the client should connect to the server;
     * id is the unique identifier of the client;
     *
     */
    public ClientThread (Socket socket, int port) throws IOException {
     //   try {
            this.socket = socket;

            this.port = port;
            clients.add(this);
            this.id = clients.size();
            sendMessage(this.id,this.id + "Entered the chat!");
       /** }catch (IOException e){
            e.printStackTrace();
        }*/
    }


    public Socket getSocket() {
        return socket;
    }

    @Override
    public long getId() {
        return totalClients;
    }

    /**
     * Creating Client
     */
    public void createClient() {
        try {
            socket = new Socket("localhost", 8080);
            out = new DataOutputStream ( socket.getOutputStream ( ) );
            clients.add(this);
            out.writeUTF ( '1' + " " + this.id);
            out.flush ( );
            socket.close();
            System.out.println(this);
            System.out.println("Sent message to server: " +"id: " + this.id);
            for (ClientThread t: clients) {
                System.out.println(t.id);
            }
        }catch ( IOException e ) {
            e.printStackTrace ( );
        }

    }

    public void sendMessage(int id, String message){
            try {
                socket = new Socket ( "localhost" , 8080);
                out = new DataOutputStream ( socket.getOutputStream ( ) );
                in = new BufferedReader ( new InputStreamReader( socket.getInputStream ( ) ) );
                out.writeUTF("2" + "ID: " + id + "Message: " +message);
                out.flush ( );
                socket.close ( );
            }catch (IOException e){
                e.printStackTrace();
            }
    }

    public void removeClient(){
        clients.remove(this);
        sendMessage(this.id,this.id + "Left the chat!");
    }

    @Override
    public void run ( ) {
        /**
           try {

                sleep ( 1000 );

            } catch ( IOException | InterruptedException e ) {
                e.printStackTrace ( );
            }*/

    }
}
