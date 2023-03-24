package org.example;

/**
 * The class MessageFilter updates the file server/filter.txt and runs client's message filtering as well.
 */
public class MessageFilter implements Runnable {

    private String word;
    private int option;
    private String messageToParse;

    // Constructor to implement a filter update.
    public MessageFilter(int option, String word) {
        this.word = word;
        this.option = option;
        System.out.println((option == 1 ? "Adding to" : "Removing from") + " the filter the word \"" + this.word + "\".\n");
    }

    // Constructor to filter client messages
    public MessageFilter(String messageToParse) {
        this.messageToParse = messageToParse;
    }

    @Override
    public void run() {
        System.out.println("Testing Filter Thread working!");
    }


}
