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

    private String word;
    private String messageToParse;

    String filterPath = "server/filter.txt";
    private HashSet<String> filterWords = new HashSet<String>();    // to store filter words

    // Constructor to implement a filter update.
    public MessageFilter(String word) {
        this.word = word;
    }


    /** // Constructor to filter client messages
    public MessageFilter(String messageToParse) {
        this.messageToParse = messageToParse;
    }*/

    @Override
    public void run() {

        readFilterFile(filterPath);
        // If the container doesn't includes this word, it's added, otherwise it's removed.
        if (!filterWords.contains(word)) {
            filterWords.add(word);
        } else {
            filterWords.remove(word);
        }
        System.out.println("Updated word set: " + filterWords);
        writeFilterFile(filterPath);

        System.out.println("\nFilter update done!\n");

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

    /**
     * Method to save the updated set of words to the file filter.txt
     */
    private void writeFilterFile(String updatedFile) {
        try {
            FileWriter writer = new FileWriter(filterPath);
            for (String word : filterWords) {
                writer.write(word + "\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
