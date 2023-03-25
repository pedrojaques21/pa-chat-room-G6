package org.example;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Scanner;

/**
 * The class UpdateFilter updates the file server/filter.txt adding or removing forbidden words,
 * and runs client's message filtering as well.
 */
public class MessageFilter implements Runnable {

    private String messageToParse;

    String filterPath = "server/filter.txt";
    private HashSet<String> filterWords = new HashSet<String>();    // to store filter words

    // Constructor to implement a filter update.


    public MessageFilter(String messageToParse) {
        this.messageToParse = messageToParse;
    }

    public String getMessageToParse() {
        return messageToParse;
    }

    /** // Constructor to filter client messages
     public MessageFilter(String messageToParse) {
     this.messageToParse = messageToParse;
     }*/

    @Override
    public void run() {

        readFilterFile(filterPath);
        // If the container doesn't include this word, it's added, otherwise it's removed.
        String parsedMessage = "null";

        for (String str : filterWords) {
            if (messageToParse.contains(str)) {
                messageToParse = messageToParse.replace(str, "***");
            }

        }

        System.out.println("\n\nMessage parsed!");
        System.out.println("Filtered message: " + parsedMessage);
    }

    /**
     * Method to read the file filter.txt and store the words in the HashSet
     */
    private void readFilterFile(String filterPath) {
        File original = new File(filterPath);
        try {
            Scanner reader = new Scanner(original);
            while (reader.hasNextLine()) {
                String word = reader.nextLine();
                filterWords.add(word);  // adding filter words to the HashSet filterWords
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
