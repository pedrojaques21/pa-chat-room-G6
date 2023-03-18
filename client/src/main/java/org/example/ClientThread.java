package org.example;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;
import java.util.Queue;
import java.io.*;


/**
 * ClientThread class represents each client that will interact with the server
 */
public class ClientThread extends Thread {
    private final int port;
    private final int id;
    /**private final int freq;*/
    private DataOutputStream out;
    private BufferedReader in;
    private Socket socket;

    /**private final Semaphore sem;*/



    /**
     * Each Client is constructed using 3 parameters, port, id and freq.
     *
     * @param port is the port where the client should connect to the server;
     * @param id is the unique identifier of the client;
     *
     */
    public ClientThread ( int port , int id) {
        this.port = port;
        this.id = id;
        /**this.freq = freq;
         * this.sem = sem;*/

    }



    /**
     * Creating Client
     */
    public void createClient() {
        try {
            socket = new Socket("localhost", 8080);
            out = new DataOutputStream ( socket.getOutputStream ( ) );
            String request = "CREATE_CLIENT";
            out.writeUTF ( request + " " +this.id);
            out.flush ( );
            socket.close();
            System.out.println("Sent message to server: " + request);
        }catch ( IOException e ) {
            e.printStackTrace ( );
        }

    }

    public void sendMessage(int id, String message){
        try {
            socket = new Socket("localhost", 8080);
            out = new DataOutputStream ( socket.getOutputStream ( ) );
            String request = "CREATE_CLIENT";
            out.writeUTF ( request + " " +this.id);
            out.flush ( );
            socket.close();
            System.out.println("Sent message to server: " + request);
        }catch ( IOException e ) {
            e.printStackTrace ( );
        }
    }

    @Override
    public void run ( ) {

        //try {
        int i = 0;
        while ( true ) {
            System.out.println ( "Sending Data" );
            try {
                // if(sem.tryAcquire(1, TimeUnit.SECONDS)) {
                socket = new Socket ( "localhost" , 8080);
                out = new DataOutputStream ( socket.getOutputStream ( ) );
                in = new BufferedReader ( new InputStreamReader ( socket.getInputStream ( ) ) );
                out.writeUTF ( "My message number " + i + " to the server " + "I'm " + id );
                String response;
                response = in.readLine ( );
                System.out.println ( "From Server " + response );
                out.flush ( );
                socket.close ( );
                sleep ( 1000 );
                i++;
            } catch ( IOException | InterruptedException e ) {
                e.printStackTrace ( );
            }
        }
    }
}
