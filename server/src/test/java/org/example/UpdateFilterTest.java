package org.example;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class UpdateFilterTest {


    public String path = "server/filterTest.txt";
    HashSet<String> filterWrd = new HashSet<String>();

    @BeforeEach
    public void prepareTest() {
        UpdateFilter update1 = new UpdateFilter("word1");




    }

    @Test
    @DisplayName ("")
    public void addWrd() {
        filterWrd.add("word1");

    }
/*
    @Test
    @DisplayName ("Add a word")
    public void readFilterTest(path) {
        for (String word : filterWrd) {
            writer.write(word + "\n");
    }
}
*/

    @Test
    public void addWord() {

        filterWrd.add("word1");

    }

/*
    private void writeFilterFile(String updatedFile) {

        try {
            //lock.lock();
            FileWriter writer = new FileWriter(filterPath);
            for (String word : filterWords) {
                writer.write(word + "\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
 */
/**
    @Test
    public void readFiltro(server/) {

    }

    @Test
    public void removeWord() {

    }




public class UpdateFilterTest {

    private HashSet<String> filterWords = new HashSet<String>();
    private String filePath = "server/filter.txt";

    private String word = "test";








    UpdateFilter messageFilter = new UpdateFilter(word);

    //Thread threadWords = new Thread(messageFilter);
    //threadWords.start();

    @Test
    public void testUpdateFilter() {
        // Create an instance of UpdateFilter
        UpdateFilter updateFilter = new UpdateFilter("password");

        // Run the updateFilter's run method
        updateFilter.run();

        // Check that the filterWords HashSet has been updated correctly
        assertTrue(updateFilter.filterWords.contains("password"));
        assertFalse(updateFilter.filterWords.contains("username"));

        // Check that the filter.txt file has been updated correctly
        String filterPath = "server/filter.txt";
        HashSet<String> expectedWords = new HashSet<>();
        expectedWords.add("password");
        try {
            File filterFile = new File(filterPath);
            Scanner reader = new Scanner(filterFile);
            HashSet<String> actualWords = new HashSet<>();
            while (reader.hasNextLine()) {
                String word = reader.nextLine();
                actualWords.add(word);
            }
            reader.close();
            assertEquals(expectedWords, actualWords);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}


*/

}