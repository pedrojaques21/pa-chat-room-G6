package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * LogWriterThread class represents the thread that wil write all the logs on the server.log file
 */
public class LoggerThread extends Thread{

    private final String logFilePath;
    private boolean active;
    private final Lock loggerLock;
    private String action;
    private int clientId;
    private String message;

    public void setAction(String action) {
        this.action = action;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * LoggerThread constructor creates a thread that logs events and as the log file path as
     * parameter
     *
     * @param logFilePath is the path to the log file
     */
    public LoggerThread(String logFilePath) {

        this.logFilePath = logFilePath;
        this.loggerLock = new ReentrantLock();
        this.active = true;
    }

    public void logMessage(String message) {
        try {
            FileWriter writer = new FileWriter(logFilePath, true);

            // Get current time
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String timestamp = formatter.format(now);
            String[] messageComponents = message.split("\\s+");
            // Extract the message components
            String action = messageComponents[0];
            String id = messageComponents[1];
            String messageSent = message.substring(message.indexOf(messageComponents[2]));

            switch (action) {
                case "CREATE", "REMOVE" -> {
                    String logCreate = timestamp + " - Action: " + messageSent + " - Client" + id + "\n";
                    writer.write(logCreate);
                }
                case "MESSAGE" -> {
                    String logMessage = timestamp + " - Action: " + action + " - Client" + id + " - " + "\"" + messageSent + "\"" + "\n";
                    writer.write(logMessage);
                }
                case "CHANGE" -> {
                    String logIdChange = timestamp + " - Action: " + action + " - Changed Client id to " + id + "\n";
                    writer.write(logIdChange);
                }
                case "EXISTINGID" -> {
                    String existingId = timestamp + " - Action: " + "EXISTING ID" + " - A Client with id "+ id + " Already Exists!\n";
                    writer.write(existingId);
                }
                case "EXISTINGIDWAITING" ->{
                    String existingWaitingId = timestamp + " - Action: " + "EXISTING ID" + " - A Client Waiting To connect with id "+ id + " Already Exists!\n";
                    writer.write(existingWaitingId);
                }
                case "CHANGEWAITING" -> {
                    String logIdChange = timestamp + " - Action: " + "CHANGE" + " - Client Waiting to Connect Changed id to " + id + "\n";
                    writer.write(logIdChange);
                }
                default -> {
                }
            }

            // Write log to file
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (active) {

        }
    }

    /**
     * This method stops the execution of the logger
     */
    public void stopLogging() {
        active = false;
    }

    /**
     * @return returns the current action that is being executed in the server
     */
    private String getCurrentAction() {
        // Add code here to get the current action on the server
        return "Current action";
    }

}
