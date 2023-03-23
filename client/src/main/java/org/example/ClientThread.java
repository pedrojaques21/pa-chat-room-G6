package org.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
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
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
            reentrantLock.lock();
            sendMessage(1,client.id,"Foi criado um cliente!");
        } finally {
            reentrantLock.unlock();
        }

    }

    public void sendMessage(int action,int id,String message){
        try {
            if(action==1){
                out.writeUTF("CREATE" + " " + id + " " + message);
                out.flush();
            } else if (action == 2) {
                out.writeUTF("MESSAGE" + " " + id + " " + message);
                out.flush();
            } else if (action == 3) {
                out.writeUTF("REMOVE" + " " + id + " " + message);
                out.flush();
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void removeClient(int id) {
        boolean encontrou = false;
        for (ClientThread client : clients) {
            if (client.id == id) {
                clients.remove(client);
                for (ClientThread cl : clients) {
                    System.out.println("Connected Clients: " + cl.id);
                    encontrou=true;
                }
                //sendMessage(3,client.id,"REMOVE" );
                /**
                 try {
                 client.socket.close();
                 } catch (IOException e) {
                 e.printStackTrace();
                 }*/

            }
        }
        if (!encontrou) {
            sendMessage(3, id, "Does not existe a client with that ID!");
        }
    }

    @Override
    public void run ( ) {
        createClient(this);

        while (true) {
            try {
                totalClients++;
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;

                while ((message = in.readLine()) != null) {
                    System.out.println(message);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
