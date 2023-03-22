package org.example;

import javax.swing.*;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    private static String serverConfigPath = "./server/server.config";
    private static Scanner scanner = new Scanner(System.in);
    // Reads configuration file and gets correct server configuration
    private static ServerConfig configFile;

    static {
        try {
            configFile = new ServerConfig(serverConfigPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main ( String[] args ) throws IOException {

        String serverPortStr = configFile.getProperty("PORT");
        String maxClientsStr = configFile.getProperty("MAX_CLIENTS");

        int serverPort = Integer.parseInt(serverPortStr);
        int maxClients = Integer.parseInt(maxClientsStr);

        ServerThread server = new ServerThread ( serverPort , maxClients,4 );//Colocar os número de workers no file config?

        int choice = 0;

        do {
            System.out.println("Server Menu:");
            System.out.println("1. Start Server"); // This option will start the server, initializing server thread
            System.out.println("2. Update Server Configurations"); // This option can be used while the server is running to refresh server configurations from server.config file
            System.out.println("3. Update Message Filters"); // This option can be used while the server is running to update message filters from filter.txt
            System.out.println("4. Stop Server"); // This option will stop the server and close all connections
            System.out.print("Choose an option: ");
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // consume the newline character
            } catch (Exception e) {
                System.out.println("Invalid input.");
                scanner.nextLine(); // consume the invalid input
                continue;
            }
            switch (choice) {
                case 1:
                    // Start the Server
                    System.out.println("\nStarting the Server...\n");
                    server.start ( );
                    break;
                case 2:
                    // Update Server configurations
                    System.out.println("\nUpdating Server configurations...\n");
                    updateServerConfigs();
                    break;
                case 3:
                    // Update Message Filters
                    System.out.println("\nExecuting option 3...\n");
                    break;
                case 4:
                    // Stop the Server
                    System.out.println("Stopping the Server...");
                    server.interrupt();
                    break;
                default:
                    // Invalid option
                    System.out.println("Invalid option.");
            }
        } while (choice != 4);
        scanner.close();
    }

    private static void updateServerConfigs ( ) {

        String key;
        String value;

        System.out.println("The available configurations are -> " + configFile.getKeys());

        System.out.println("\nWhich configuration would you like to change: ");
        key = scanner.nextLine();

        System.out.println("Which value do you want to set it: ");
        value = scanner.nextLine();

        configFile.setProperty( key, value );
    }
}