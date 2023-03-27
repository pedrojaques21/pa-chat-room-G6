    package org.example;

    import org.junit.jupiter.api.AfterEach;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.DisplayName;
    import org.junit.jupiter.api.Test;
    import java.io.*;
    import java.net.Socket;
    import java.util.Scanner;

    import static org.junit.jupiter.api.Assertions.*;

    class ServerThreadTest {

        private ServerThread serverThread;
        private int serverPort;


        @BeforeEach
        @DisplayName("Creating a server for testing before each test")
        public void startServer() {
            serverPort = 8888;
            serverThread = new ServerThread(serverPort,20,10);
            serverThread.start();
        }

        @AfterEach
        @DisplayName("Finish server after each test")
        public void endServer() {
            serverThread.interrupt();
        }


        @Test
        public void testClientTimeout() throws IOException {
            Socket socket = new Socket("localhost", serverPort);
            socket.setSoTimeout(1000); // Set timeout to 1 second
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("test message");
            // Wait for timeout to occur
            assertThrows(IOException.class, () -> new DataInputStream(socket.getInputStream()).readUTF());
            socket.close();
        }


        @Test
        public void testIfServerStartedCorrectly() {
            assertTrue(serverThread.isAlive());
            serverThread.interrupt();
        }

        @Test
        @DisplayName("Testing connection of the server")
        public void testingConnection() throws IOException {
            Socket socket = new Socket("localhost", 8888);
            assertTrue(socket.isConnected());
            socket.close();
        }


        @Test
        @DisplayName("Check if server is not null")
        void checkServerNull() {
            assertNotNull(serverThread.getName());

        }

        @Test
        void testValueOfTheConfigFile() {
            try {
                File file = new File("server.config");
                Scanner scanner = new Scanner(file);
                int clientMax = 0;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("CLIENT_MAX")) {
                        String[] parts = line.split("=");
                        if (parts.length == 2) {
                            clientMax = Integer.parseInt(parts[1].trim());
                            break;
                        }
                    }
                }
                scanner.close();
                System.out.println("CLIENT_MAX: " + clientMax);

                assertEquals(5,clientMax);
            } catch (
                    FileNotFoundException e) {
                System.out.println("File not found.");
            }

        }
    }