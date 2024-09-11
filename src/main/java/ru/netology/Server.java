package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Handler;

public class Server {

    private ServerSocket server = null;
    private Socket client = null;

    public void start(int port) {
        try {
            server = new ServerSocket(port);
            System.out.println("Server is up in port: " + port);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public Socket connect() {
        try {
            if (server == null) {
                throw new IOException("Server in never start!");
            }
            client = server.accept();
            System.out.println("New client is connected.");
        } catch (IOException e) {
            System.err.println(e);
        }
        return client;
    }

    public void addHandler(String request, String path, Handler handler) {

    }
}
