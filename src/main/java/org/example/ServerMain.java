package org.example;

public class ServerMain {
    public static void main ( String[] args ) {
        ServerThread server = new ServerThread ( 8888 );
        server.start ( );
    }
}
