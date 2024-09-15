package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
public class Server {

    private ServerSocket server = null;
    private Socket client = null;
    private final ConcurrentHashMap<String,
            ConcurrentHashMap<String, Handler>> handlers =
            new ConcurrentHashMap<>();

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

    public void addHandler(String method, String path, Handler handler) {

//        ConcurrentHashMap<String, Handler> paths = handlers.computeIfAbsent(
//                method, k -> new ConcurrentHashMap<>());
//        paths.put(path, handler);


        if (!handlers.containsKey(method)) {
            var paths = new ConcurrentHashMap<String, Handler>();
            paths.put(path, handler);
            handlers.put(method, paths);
        } else {
            var paths = handlers.get(method);
            paths.put(path, handler);
        }
    }
}
