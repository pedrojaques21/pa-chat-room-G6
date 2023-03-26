package org.example;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main ( String[] args ) throws IOException {

        ReentrantLock ClientLock = new ReentrantLock();
        int totalClients = 0;
        Scanner scanner = new Scanner(System.in);
        Scanner option = new Scanner(System.in);
        Scanner res = new Scanner(System.in);
        String logFilePath = "server/server.log";
        LoggerThread loggerThread = new LoggerThread(logFilePath);

        ClientThread clientCreate1 = new ClientThread(totalClients,8080,ClientLock,loggerThread);
        totalClients = totalClients + 1;
        clientCreate1.start();
        ClientThread clientCreate2 = new ClientThread(totalClients,8080,ClientLock,loggerThread);
        totalClients = totalClients + 1;
        clientCreate2.start();

        System.out.println("                                                         ");
        System.out.println("******************* Chat Room PA G6 *********************");
        System.out.println("** To create a client -> /create                       **");
        System.out.println("** To send a message -> /message                       **");
        System.out.println("** To remove a client -> /remove                       **");
        System.out.println("** To leave -> /quit                                   **");
        System.out.println("*********************************************************");

        while(true) {
            String input = scanner.nextLine(); // read user input
            if (input.startsWith("/create")) {
                // Create a new client
                ClientThread clientCreate = new ClientThread(totalClients,8080,ClientLock,loggerThread);
                totalClients = totalClients + 1;
                clientCreate.start();
            } else if (input.startsWith("/message")) {
                // Send a message to a client
                System.out.println("Who is trying to send a message?");
                System.out.print("Insert id: \n");
                int id = option.nextInt();
                System.out.print("Insert Message: ");
                String message = res.nextLine();
                ClientThread clientThread = new ClientThread(totalClients,8080,ClientLock,loggerThread);
                clientThread.sendMessage(2,id, message);

            } else if (input.startsWith("/remove")) {
                System.out.print("Insert id of the client you wish to remove: \n");
                int id = option.nextInt();
                ClientThread clientToRemove = new ClientThread(totalClients,8080,ClientLock,loggerThread);
                clientToRemove.sendMessage(3,id,"REMOVE" );
            }else if(input.startsWith("/quit")){
                break;
            }
        }
        scanner.close();
        option.close();
        res.close();
    }

}