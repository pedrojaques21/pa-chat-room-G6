package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

class ClientThreadTest {

    @Test
    @DisplayName("Check if client is connected correctly")
    public void createClient(){
        File file = new File(("server.log"));
        ReentrantLock ClientLock = new ReentrantLock();
        String logFilePath = file.getAbsolutePath();
        LoggerThread loggerThread = new LoggerThread(logFilePath);
        int id = 2;
        ClientThread clientThread = new ClientThread(id,8080,ClientLock,loggerThread);
        clientThread.start();
        assertAll(
                () -> assertTrue(clientThread.isAlive()),
                () -> assertEquals(2,clientThread.getId())
        );
    }

    @Test
    @DisplayName("Checks if the message sent is written correctly on the log")
    public void createClientsWithSameId() throws IOException, InterruptedException {
        File file = new File(("server.log"));
        ReentrantLock ClientLock = new ReentrantLock();
        String logFilePath = file.getAbsolutePath();
        LoggerThread loggerThread = new LoggerThread(logFilePath);
        int id = 3;
        ClientThread clientThread1 = new ClientThread(id,8080,ClientLock,loggerThread);
        ClientThread clientThread2 = new ClientThread(id,8080,ClientLock,loggerThread);
        clientThread1.start();
        clientThread2.start();
        Thread.sleep(1000);
        Assertions.assertEquals("EXISTING ID - A Client with id 3 Already Exists!", loggerThread.getLastLine());
    }

    @Test
    @DisplayName("Checks if the message sent is written correctly on the log")
    public void testSendMessage() throws IOException {
        File file = new File(("server.log"));
        ReentrantLock ClientLock = new ReentrantLock();
        String logFilePath = file.getAbsolutePath();
        LoggerThread loggerThread = new LoggerThread(logFilePath);
        int id = 1;
        ClientThread clientThread = new ClientThread(id,8080,ClientLock,loggerThread);
        clientThread.start();
        String message = "hello";
        clientThread.sendMessage(2, 2, message);
        Assertions.assertEquals("MESSAGE - Client2 - \"hello\"", loggerThread.getLastLine());
    }

    @Test
    @DisplayName("Client quiting")
    public void testQuitClient() throws IOException, InterruptedException {
        File file = new File(("server.log"));
        ReentrantLock ClientLock = new ReentrantLock();
        String logFilePath = file.getAbsolutePath();
        LoggerThread loggerThread = new LoggerThread(logFilePath);
        int id = 4;
        ClientThread clientThread = new ClientThread(id,8080,ClientLock,loggerThread);
        clientThread.start();
        Thread.sleep(1000);
        clientThread.sendMessage(3, 4, "/quit");
        Thread.sleep(1000);
        assertAll(
                () -> assertEquals("DISCONNECTED - Client4", loggerThread.getLastLine()),
                () -> assertFalse(clientThread.isAlive())
        );
    }

    @Test
    @DisplayName("Changing Client Id after submitting a existing id")
    public void changeClientId() throws IOException, InterruptedException {
        File file = new File(("server.log"));
        ReentrantLock ClientLock = new ReentrantLock();
        String logFilePath = file.getAbsolutePath();
        LoggerThread loggerThread = new LoggerThread(logFilePath);
        int id = 4;
        ClientThread clientThread1 = new ClientThread(id,8080,ClientLock,loggerThread);
        ClientThread clientThread2 = new ClientThread(id,8080,ClientLock,loggerThread);
        clientThread1.start();
        clientThread2.start();
        Thread.sleep(1000);

        // Set the input to "3" for the next read operation
        ByteArrayInputStream in = new ByteArrayInputStream("3\n".getBytes());
        System.setIn(in);

        // Read the input from the redirected stream
        Scanner scanner = new Scanner(System.in);
        int newID = scanner.nextInt();

        clientThread2.sendMessage(4, newID, "Changing my id ");
        Thread.sleep(1000);
        assertAll(
                () -> assertEquals("CHANGE - Changed Client id to 3", loggerThread.getLastLine()),
                () -> assertTrue(clientThread1.isAlive()),
                () -> assertTrue(clientThread2.isAlive())
        );
    }

    @Test
    @DisplayName("Check that server has a limited size and the waiting clients connect in a FIFO way, considering a chat size o 4")
    public void serverLimitSizeAndConnectInAFIFOWay() throws IOException, InterruptedException {
        File file = new File(("server.log"));
        ReentrantLock ClientLock = new ReentrantLock();
        String logFilePath = file.getAbsolutePath();
        LoggerThread loggerThread = new LoggerThread(logFilePath);
        int id1 = 4;
        int id2 = 5;
        int id3 = 6;
        int id4 = 7;
        int id5 = 8;
        int id6 = 9;
        int id7 = 10;
        ClientThread clientThread1 = new ClientThread(id1,8080,ClientLock,loggerThread);
        ClientThread clientThread2 = new ClientThread(id2,8080,ClientLock,loggerThread);
        ClientThread clientThread3 = new ClientThread(id3,8080,ClientLock,loggerThread);
        ClientThread clientThread4 = new ClientThread(id4,8080,ClientLock,loggerThread);
        ClientThread clientThread5 = new ClientThread(id5,8080,ClientLock,loggerThread);
        ClientThread clientThread6 = new ClientThread(id6,8080,ClientLock,loggerThread);
        ClientThread clientThread7 = new ClientThread(id7,8080,ClientLock,loggerThread);
        clientThread1.start();
        Thread.sleep(500);
        clientThread2.start();
        Thread.sleep(500);
        clientThread3.start();
        Thread.sleep(500);
        clientThread4.start();
        Thread.sleep(500);
        clientThread5.start();
        Thread.sleep(500);
        clientThread6.start();
        Thread.sleep(500);
        clientThread7.start();
        Thread.sleep(500);
        assertNotEquals("CONNECTED - Client9", loggerThread.getLastLine());
        Thread.sleep(500);
        clientThread3.sendMessage(3, 4, "/quit");
        Thread.sleep(500);
        assertEquals("CONNECTED - Client9",loggerThread.getLastLine());
        assertNotEquals("CONNECTED - Client10",loggerThread.getLastLine());
    }
}