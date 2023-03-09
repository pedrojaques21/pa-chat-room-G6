    package org.example;

    import java.io.DataOutputStream;
    import java.io.DataInputStream;
    import java.io.IOException;
    import java.net.Socket;


    import org.junit.jupiter.api.*;

    import static org.junit.jupiter.api.Assertions.*;

    class ServerThreadTest {

        private ServerThread serverThread;
        private int serverPort;


        @BeforeEach
        @DisplayName("Creating a server for testing before each test")
        public void startServer() {
            serverPort = 8888;
            serverThread = new ServerThread(serverPort);
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
        void checkServerNull(){
            assertNotNull(serverThread.getName());

        }

    }