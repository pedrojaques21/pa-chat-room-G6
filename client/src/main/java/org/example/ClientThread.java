package org.example;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;


/**
 * ClientThread class represents each client that will interact with the server
 */
public class ClientThread extends Thread {

    private final ReentrantLock reentrantLock;

    private final LoggerThread logger;
    private boolean running;
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
    public ClientThread (int id,int port, ReentrantLock reentrantLock,LoggerThread logger) {
            this.port = port;
            this.id = id;
            this.reentrantLock = reentrantLock;
            this.running = true;
            this.logger = logger;
        try {
            socket = new Socket("localhost",port);
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
                reentrantLock.lock();
                out.writeUTF("CREATE" + " " + id + " " + message);
                out.flush();
                logger.logMessage("CREATE" + " " + id + " " + "CONNECTED");
                reentrantLock.unlock();
            } else if (action == 2) {
                reentrantLock.lock();
                out.writeUTF("MESSAGE" + " " + id + " " + message);
                out.flush();
                logger.logMessage("MESSAGE" + " " + id + " " + message);
                reentrantLock.unlock();
            } else if (action == 3) {
                reentrantLock.lock();
                out.writeUTF("REMOVE" + " " + id + " " + message);
                out.flush();
                logger.logMessage("REMOVE" + " " + id + " " + "DISCONNECTED");
                reentrantLock.unlock();
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        createClient(this);
        try {
            totalClients++;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message;
            while (running) {
                if (socket.isClosed()) {
                    break;
                }
                message = in.readLine();
                if (message != null) {
                    System.out.println(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        shutdown();
    }

    public void shutdown() {
        running = false;
        try {
            in.close();
            out.close();
            if (!socket.isClosed()){
                socket.close();
            }
        } catch (IOException e) {
            //Cant Handle
        }
    }

}
