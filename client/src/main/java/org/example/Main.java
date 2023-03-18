package org.example;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Main {

    public static void main ( String[] args ) {

        Semaphore   sem = new Semaphore(1);


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
                    ClientThread client = new ClientThread ( 8080 , 1 );
                    client.createClient();
                    break;
                case 2:
                    System.out.println("Option 2 selected.");
                    break;
                case 3:
                    System.out.println("Who is trying to send a message?");
                    System.out.print("Insert id: \n");
                    int id = option.nextInt();
                    System.out.print("Insert Message: ");
                    String message = option.nextLine();

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