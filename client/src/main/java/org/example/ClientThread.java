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
            clients.add(client);
            out.writeUTF ( "CREATE" + " " + client.id);
            out.flush ( );
        }catch ( IOException e ) {
            e.printStackTrace ( );
        }

    }

    public void sendMessage(int id,String message){
            try {
                if(clients.isEmpty()){
                    System.out.println("There are no clients on the chat!\n");
                }else{
                    reentrantLock.lock();
                    for(ClientThread cli: clients) {
                        if(cli.id == id) {
                            DataOutputStream cliOut = new DataOutputStream(cli.socket.getOutputStream());
                            cliOut.writeUTF("MESSAGE" + " " +  id + " " + message);
                            cliOut.flush();
                        }
                    }
                    reentrantLock.unlock();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
    }

    public void broadcastMessage(String message) {
      //  for (ClientThread client : clients) {
            //if (client.id != id) {
                try {
                    out = new DataOutputStream(socket.getOutputStream());
                    out.writeUTF("MESSAGE" + " " + message);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            //}
        //}
    }

    public void removeClient(){
        clients.remove(this);
        broadcastMessage(id + " left the chat!");
    }

    @Override
    public void run ( ) {

            try {
                totalClients++;
                socket = new Socket ( "localhost" , 8080 );
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new DataOutputStream(socket.getOutputStream());
                System.out.println("Creating a client with id: " + this.id);
                out.writeUTF("Hello, " + this.id );
                out.flush();

                while (true) {
                    reentrantLock.lock();
                    createClient(this);
                    reentrantLock.unlock();
                    //out.writeUTF("Hello, " + this.id );
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

    }

}
