package org.example;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    public static void main ( String[] args ) {

        ReentrantLock ClientLock = new ReentrantLock();
        Scanner scanner = new Scanner(System.in);
        String logFilePath = "server/server.log";
        LoggerThread loggerThread = new LoggerThread(logFilePath);

        System.out.println("                                                         ");
        System.out.println("******************* Chat Room PA G6 *********************");
        System.out.println("** To leave -> /quit                                   **");
        System.out.println("*********************************************************");
        System.out.println("                                                         ");

        System.out.print("Insert your id: ");
        int id = scanner.nextInt();
        ClientThread clientCreate1 = new ClientThread(id,8080,ClientLock,loggerThread);
        clientCreate1.start();

    }

}