package org.example;

public class Main {

    public static void main ( String[] args ) {
        ServerThread server = new ServerThread ( 8888 , 10 );
        server.start ( );
    }
}