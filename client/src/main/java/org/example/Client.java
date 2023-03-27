package org.example;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class that represents the clients and contains the main method
 */
public class Client {
    public static void main ( String[] args ) {

        //lock used to control the access to the log file
        ReentrantLock ClientLock = new ReentrantLock();
        //reads client input
        Scanner scanner = new Scanner(System.in);
        //path to the log file
        String logFilePath = "server/server.log";
        //new thread to write on the file
        LoggerThread loggerThread = new LoggerThread(logFilePath);

        System.out.println("                                                         ");
        System.out.println("******************* Chat Room PA G6 *********************");
        System.out.println("** To leave -> /quit                                   **");
        System.out.println("*********************************************************");
        System.out.println("                                                         ");

        System.out.print("Insert your id: ");
        //stores the id given by the client
        int id = scanner.nextInt();
        //creates a new client thread
        ClientThread clientCreate1 = new ClientThread(id,8080,ClientLock,loggerThread);
        //starts the thread
        clientCreate1.start();
    }

}