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

        Semaphore sem = new Semaphore(1);
        ReentrantLock ClientLock = new ReentrantLock();

        System.out.println("                                                                     ");
        System.out.println("            ******************* Chat Room PA G6 *********************");
        System.out.println("            ** To create a client -> /create                       **");
        System.out.println("            ** To send a message -> /message <client_id> <message> **");
        System.out.println("            ** To remove a client -> /remove <client_id>           **");
        System.out.println("            ** To leave -> /quit                                   **");
        System.out.println("            *********************************************************");

        int totalClients = 0;
        Socket socket = new Socket("localhost", 8080);
        Scanner scanner = new Scanner(System.in);
        Scanner option = new Scanner(System.in);
        Scanner res = new Scanner(System.in);


        while(true) {
            String input = scanner.nextLine(); // read user input
            if (input.startsWith("/create")) {
                // Create a new client
                ClientThread clientCreate = new ClientThread(totalClients,socket,8080,ClientLock);
                totalClients = totalClients + 1;
                clientCreate.start();
                //clientCreate.createClient(clientCreate);
            } else if (input.startsWith("/message")) {
                // Send a message to a client
                System.out.println("Who is trying to send a message?");
                System.out.print("Insert id: \n");
                int id = option.nextInt();
                System.out.print("Insert Message: ");
                String message = res.nextLine();
                System.out.println("Id inserted: " + id);
                System.out.println("Message to be sent: " + message);
                ClientThread clientThread = new ClientThread(totalClients,socket,8080,ClientLock);
                clientThread.sendMessage(id, message);

            } else if (input.startsWith("/remove")) {
                //clientThread.removeClient(clientThread);
            }else if(input.startsWith("/quit")){
                break;
            }
        }
        scanner.close();
        option.close();
        res.close();
    }

}