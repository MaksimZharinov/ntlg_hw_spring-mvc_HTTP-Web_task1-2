package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    
    private int port;
    private ServerSocket serverSocket;

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public Socket connect() {
        Socket client = null;
        try {
            client = serverSocket.accept();
        } catch (IOException e) {
            System.err.println(e);
        }
        return client;
    }
}
