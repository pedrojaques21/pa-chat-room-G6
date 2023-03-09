package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerThreadTest {

    @Test
    @DisplayName("Check if server is not null")
    void checkServerNull(){
        var server = new ServerThread(8888);
        assertNotNull(server.getName());

    }

}