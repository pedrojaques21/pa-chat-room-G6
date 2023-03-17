package org.example;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * ClientThread class represents each client that will interact with the server
 */
public class ClientThread extends Thread {
    private final int port;
    private final int id;
    private final int freq;
    private DataOutputStream out;
    private BufferedReader in;
    private Socket socket;

    /**
     * Each Client is constructed using 3 parameters, port, id and freq.
     *
     * @param port   is the port where the client should connect to the server;
     * @param id     is the unique identifier of the client;
     * @param freq   is the frequency of interaction with the server;
     */
    public ClientThread (int port , int id , int freq) {
        this.port = port;
        this.id = id;
        this.freq = freq;
    }

    @Override
    public void run ( ) {
        int i = 0;
        while ( true ) {
            System.out.println ( "Sending Data" );
            try {
                // if(sem.tryAcquire(1, TimeUnit.SECONDS)) {
                socket = new Socket ( "localhost" , port );
                out = new DataOutputStream ( socket.getOutputStream ( ) );
                in = new BufferedReader ( new InputStreamReader ( socket.getInputStream ( ) ) );
                out.writeUTF ( "My message number " + i + " to the server " + "I'm " + id );
                String response;
                response = in.readLine ( );
                System.out.println ( "From Server " + response );
                out.flush ( );
                socket.close ( );
                sleep ( freq );
                i++;
            } catch ( IOException | InterruptedException e ) {
                e.printStackTrace ( );
            }
        }
    }
}
