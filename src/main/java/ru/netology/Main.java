package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {

        final List<String> validPaths = List.of("/index.html", "/spring.svg",
                "/spring.png", "/resources.html", "/styles.css", "/app.js",
                "/links.html", "/forms.html", "/classic.html",
                "/events.html", "/events.js");
        final List<String> validMethods = List.of("GET", "POST", "PUT", "DELETE");

        final var connections = Executors.newFixedThreadPool(64);
        final var clients = new ConcurrentHashMap<Integer, Socket>();
        final var notFound404 =
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";

        final var server = new Server();
        server.addHandler("GET", "/messages", (request, responseStream) -> {
            // code
        });
        server.addHandler("POST", "/message", (request, responseStream) -> {
            // code
        });
        server.start(9999);
        System.out.println("Server is up.");

        while (true) {
            connections.execute(() -> {
                final var client = server.connect();
                new Thread(() -> {
                    clients.put(client.getPort(), client);
                    while (true) {
                        try (var in = new BufferedReader(
                                new InputStreamReader(
                                        client.getInputStream()));
                             var out = new BufferedOutputStream(
                                     client.getOutputStream())) {

                            List<String> lines = new ArrayList<>();
                            String line;
                            while ((line = in.readLine()) != null) {
                                lines.add(line);
                            }

                            final var requestLine = lines.getFirst();
                            final var parts = requestLine.split(" ");

                            if (parts.length != 3) {
                                out.write(notFound404.getBytes());
                                out.flush();
                                continue;
                            }

                            String method = parts[0];
                            if (!validMethods.contains(method)) {
                                out.write(notFound404.getBytes());
                                out.flush();
                                continue;
                            }

                            String path = parts[1];
                            if (!validPaths.contains(path)) {
                                out.write(notFound404.getBytes());
                                out.flush();
                                continue;
                            }

                            List<String> headers = new ArrayList<>();
                            for (int i = 2; i < lines.size() &&
                                    !lines.get(i).isEmpty(); i++) {
                                headers.add(lines.get(i));
                            }

                            StringBuilder bodyBuilder = new StringBuilder();
                            if (lines.get(headers.size()).isEmpty()) {
                                for (int i = headers.size() + 1; i < lines.size(); i++) {
                                    bodyBuilder.append(lines.get(i));
                                }
                            }

                            String body = bodyBuilder.toString();

                            Request request = new Request(method, path, headers, body);

                        } catch (IOException e) {
                            System.err.println(e);
                        }
                    }
                }).start();
            });
        }
    }
}