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
    public ClientThread (Socket socket, int port) {
            this.socket = socket;
            this.port = port;
            this.id = clients.size();
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
           //socket = new Socket("localhost", 8080);
          // out = new DataOutputStream ( socket.getOutputStream ( ) );
            clients.add(this);
            //broadcastMessage(id + " joined the chat!");
            out.writeUTF ( "CREATE" + " " + this.id);
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
                    for(ClientThread cli: clients) {
                        if(cli.id == id) {
                            if (cli.out == null) {
                                cli.out = new DataOutputStream(cli.socket.getOutputStream());
                            }
                            cli.out.writeUTF("MESSAGE" + " " +  id + " " + message);
                            cli.out.flush();
                        }
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
    }

    public void broadcastMessage(String message) {
        for (ClientThread client : clients) {
            if (client.id != id) {
                try {
                    client.out.writeUTF("MESSAGE" + " " + message);
                    client.out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void removeClient(){
        clients.remove(this);
        broadcastMessage(id + " left the chat!");
    }

    @Override
    public void run ( ) {
        while (true) {
            try {
                // get the input stream of the socket
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new DataOutputStream(socket.getOutputStream());
                clients.add(this);
                //broadcastMessage(id + " joined the chat!");
                out.writeUTF ( "CREATE" + " " + this.id);
                out.flush ( );
                // continuously read messages from the server
                String message;
               // message = in.readLine();
               // System.out.println("MESG RECEBIDA: " + message);
               // broadcastMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
