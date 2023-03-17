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

    @Override
    public void run( ) {
        try {
            FileWriter writer = new FileWriter(logFilePath, true);
            while (active) {
                // Get current time
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                String timestamp = formatter.format(now);

                // Write log to file
                String log = timestamp + " - ACTION : " + this.action + " - CLIENT" + this.clientId + (message != null ? " - " + message : "") ;
                loggerLock.lock();
                try {
                    writer.write(log);
                    writer.flush();
                } finally {
                    loggerLock.unlock();
                }

                // Sleep for 1 second
                Thread.sleep(1000);
            }
            writer.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
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
