package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Class that represents the Server and contains the main method
 * Responsible for running the server, updating server configurations, updating filter words and stopping the server.
 */
public class Server {

    //path to the server config file
    private static String serverConfigPath = "./server/server.config";
    private static Scanner scanner = new Scanner(System.in);
    // Reads configuration file and gets correct server configuration
    private static ServerConfig configFile;
    private static UpdateFilter messageFilter;

    static {
        try {
            configFile = new ServerConfig(serverConfigPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Main method that receives user input and processes its actions
     * @param args
     * @throws IOException
     */
    public static void main ( String[] args ) throws IOException {

        String serverPortStr = configFile.getProperty("PORT");
        String maxClientsStr = configFile.getProperty("MAX_CLIENTS");

        int serverPort = Integer.parseInt(serverPortStr);
        int maxClients = Integer.parseInt(maxClientsStr);

        //creates a new server Thread, with 7 workers
        ServerThread server = new ServerThread ( serverPort , maxClients,7 );

        int choice = 0;

        do {
            System.out.println("Server Menu:");
            System.out.println("1. Start Server"); // This option will start the server, initializing server thread
            System.out.println("2. Update Server Configurations"); // This option can be used while the server is running to refresh server configurations from server.config file
            System.out.println("3. Update Message Filters"); // This option can be used while the server is running to update message filters from filter.txt
            System.out.println("4. Stop Server"); // This option will stop the server and close all connections
            System.out.println("5. Flush server logs"); // This option will stop the server and close all connections
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

                    serverPortStr = configFile.getProperty("PORT");
                    maxClientsStr = configFile.getProperty("MAX_CLIENTS");

                    serverPort = Integer.parseInt(serverPortStr);
                    maxClients = Integer.parseInt(maxClientsStr);

                    String key;
                    String value;

                    System.out.println("The available configurations are -> " + configFile.getKeys());

                    System.out.println("\nWhich configuration would you like to change: ");
                    key = scanner.nextLine();

                    System.out.println("Which value do you want to set it: ");
                    value = scanner.nextLine();

                    configFile.setProperty( key, value );

                    if (key == "PORT") {
                        server.setPort(Integer.parseInt(value));
                    }

                    if (key == "MAX_CLIENTS"){
                        server.setMaxClients(Integer.parseInt(value));
                    }

                    break;
                case 3:
                    // Update Message Filters
                    System.out.println("\nUpdating words filter set...\n");
                    updateMessageFilter();
                    break;
                case 4:
                    // Stop the Server
                    System.out.println("Stopping the Server...");
                    server.interrupt();
                    System.exit(0);
                    break;
                case 5:
                    try {
                        FileWriter fileWriter = new FileWriter("./server/server.log", false);
                        fileWriter.write(""); // write empty string to file
                        fileWriter.close();
                        System.out.println("Successfully flushed logs.");
                    } catch (IOException e) {
                        System.out.println("An error occurred.");
                        e.printStackTrace();
                    }
                    break;
                default:
                    // Invalid option
                    System.out.println("Invalid option.");
            }
        } while (choice != 4);
        scanner.close();
    }

    /**
     * Method to update, add or remove, words in the server/filter.txt
     * That purpose is accomplished with the instance of class UpdateFilter,
     * which implements the interface Runnable  */
    private static void updateMessageFilter ( ) {

        String word = null;

        System.out.println("To add or to remove, what's the word? ");
        word = scanner.next();

        // Creating and starting a thread for that job,
        messageFilter = new UpdateFilter(word);
        Thread threadWords = new Thread(messageFilter);
        threadWords.start();

    }

}