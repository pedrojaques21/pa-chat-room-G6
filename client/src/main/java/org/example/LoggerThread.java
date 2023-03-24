package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LogWriterThread class represents the thread that wil write all the logs on the server.log file
 */
public class LoggerThread extends Thread{

    private final String logFilePath;
    private boolean active = true;

    /**
     * LoggerThread constructor creates a thread that logs events and as the log file path as
     * parameter
     *
     * @param logFilePath is the path to the log file
     */
    public LoggerThread(String logFilePath) {

        this.logFilePath = logFilePath;
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

            switch (action){
                case "CREATE", "REMOVE":
                    String logCreate = timestamp + " - Action: " + messageSent + " - Client" + id +"\n";
                    writer.write(logCreate);
                    break;
                case "MESSAGE":
                    String logMessage = timestamp + " - Action: " + action + " - Client" + id + " - " + "\"" + messageSent + "\"" + "\n";
                    writer.write(logMessage);
                    break;
                default:
                    break;
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
