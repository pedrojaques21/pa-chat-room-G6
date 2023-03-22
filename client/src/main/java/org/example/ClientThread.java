package org.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;


/**
 * ClientThread class represents each client that will interact with the server
 */
public class ClientThread extends Thread {

    private final ReentrantLock reentrantLock;
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
    public ClientThread (int id,Socket socket, int port, ReentrantLock reentrantLock) {
            this.socket = socket;
            this.port = port;
            this.id = id;
            this.reentrantLock = reentrantLock;
        try {
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Socket getSocket() {
        return socket;
    }

    @Override
    public long getId() {
        return this.id;
    }

    /**
     * Creating Client
     */
    public void createClient(ClientThread client) {
        try {
            out = new DataOutputStream(socket.getOutputStream());
            clients.add(client);
            out.writeUTF ( "CREATE" + " " + client.id + " " + "Foi criado um cliente!");
            out.flush ( );
        }catch ( IOException e ) {
            e.printStackTrace ( );
        }

    }

    public void sendMessage(int id,String message){
        try {
            for(ClientThread cli: clients) {
                if(cli.id == id) {
                    out = new DataOutputStream(socket.getOutputStream());
                    out.writeUTF("MESSAGE" + " " + cli.id + " " + message);
                    out.flush( );
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    public void removeClient(){
        clients.remove(this);
        sendMessage(id , " left the chat!");
    }

    @Override
    public void run ( ) {

            try {
                totalClients++;
                socket = new Socket ( "localhost" , 8080 );
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new DataOutputStream(socket.getOutputStream());
                System.out.println("Creating a client with id: " + this.id);
                //out.writeUTF("Hello, " + this.id );
                //out.flush();
                //reentrantLock.lock();
                createClient(this);
                //reentrantLock.unlock();

            } catch (IOException e) {
                e.printStackTrace();
            }

    }

}
