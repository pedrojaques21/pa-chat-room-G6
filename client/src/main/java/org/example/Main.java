package org.example;
import java.util.Scanner;

public class Main {

    public static void main ( String[] args ) {




        Scanner option = new Scanner(System.in);
        int choice = 0;
        do {
            System.out.println("What do you want to do:");
            System.out.println("1. Create a Client");
            System.out.println("2. Choose a Client and send a Message");
            System.out.println("3. Display all Users");
            System.out.println("4. Quit");

            choice = option.nextInt();

            switch (choice) {
                case 1:
                    System.out.println("Option 1 selected.");
                    ClientThread client = new ClientThread ( 8888 , 1 , 2000 );
                    client.start ( );

                    ClientThread client2 = new ClientThread ( 8888 , 2 , 1000 );
                    client2.start ( );

                    ClientThread client3 = new ClientThread ( 8888 , 3 , 2000 );
                    client3.start ( );

                    ClientThread client4 = new ClientThread ( 8888 , 4 , 1000 );
                    client4.start ( );
                    break;
                case 2:
                    System.out.println("Option 2 selected.");
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