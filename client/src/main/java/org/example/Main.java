package org.example;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Main {
    public static void main ( String[] args ) throws IOException {

        Semaphore   sem = new Semaphore(1);

        Socket socket = new Socket("localhost", 8080);

        ClientThread cli = new ClientThread(socket,8080);



        Scanner option = new Scanner(System.in);
        int choice = 0;
        do {
            System.out.println("What do you want to do:");
            System.out.println("1. Create a Client");
            System.out.println("2. Choose a Client and send a Message");
            System.out.println("3. Display all Users");
            System.out.println("4. Quit");
            System.out.print("Choose your option: ");

            choice = option.nextInt();

            switch (choice) {
                case 1:
                    System.out.println("Option 1 selected.");
                    ClientThread client = new ClientThread ( socket,8080);
                    client.start();
                    //client.createClient();

                    break;
                case 2:
                    System.out.println("Who is trying to send a message?");
                    System.out.print("Insert id: \n");
                    int id = option.nextInt();
                    Scanner res = new Scanner(System.in);
                    System.out.print("Insert Message: ");
                    String message = res.nextLine();
                    System.out.println("Id inserted: " + id);
                    System.out.println("Message to be sent: " + message);
                    //ClientThread clientMessage = new ClientThread(socket,8080);
                    cli.sendMessage(id,message);
                    break;
                case 3:
                    System.out.println("Option 3 selected.");
                    break;
                case 4:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid option.");
                    break;
            }

        } while (choice != 4);

        option.close();


    }
}