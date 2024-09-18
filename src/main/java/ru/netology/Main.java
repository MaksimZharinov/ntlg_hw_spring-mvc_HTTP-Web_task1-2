package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class Main {

    final static List<String> validPaths = List.of("/index.html",
            "/spring.svg", "/spring.png", "/resources.html", "/styles.css",
            "/app.js", "/links.html", "/forms.html", "/classic.html",
            "/events.html", "/events.js");
    final static List<String> validMethods = List.of("GET", "POST",
            "PUT", "DELETE");
    final static String notFound404 =
            "HTTP/1.1 404 Not Found\r\n" +
                    "Content-Length: 0\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

    private static boolean checkRequest(Request request,
                                        BufferedOutputStream responseStream) {
        try {
            if (!validMethods.contains(request.getMethod()) ||
                    !validPaths.contains(request.getPath())) {
                responseStream.write(notFound404.getBytes());
                responseStream.flush();
                return false;
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        return true;
    }

    private static void err(BufferedOutputStream out) throws IOException {
        out.write(notFound404.getBytes());
        out.flush();
    }

    public static void main(String[] args) {

        final var connections = Executors.newFixedThreadPool(64);
        final var clients = new ConcurrentHashMap<Integer, Socket>();

        final var server = new Server();

        server.addHandler("GET", "/messages", (request, responseStream) -> {

            try {

                final var filePath = Path.of(".", "public", request.getPath());
                final var mimeType = Files.probeContentType(filePath);

                if (request.getPath().equals("/classic.html")) {
                    final var template = Files.readString(filePath);
                    final var content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();
                    responseStream.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + content.length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    responseStream.write(content);
                    responseStream.flush();
                }

                final var length = Files.size(filePath);
                responseStream.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                Files.copy(filePath, responseStream);
                responseStream.flush();
            } catch (Exception e) {
                System.err.println(e);
            }
        });

        server.addHandler("POST", "/messages", (request, responseStream) -> {

            try {

                if (!checkRequest(request, responseStream)) return;

                final var filePath = Path.of(".", "public", request.getPath());
                final var mimeType = Files.probeContentType(filePath);


            } catch (Exception e) {
                System.err.println(e);
            }
        });

        server.start(9999);
        System.out.println("Server is up.");

        while (true) {
            connections.execute(() -> {
                final var client = server.connect();
                new Thread(() -> {
                    clients.put(client.getPort(), client);
                    try (var in = new BufferedReader(
                            new InputStreamReader(
                                    client.getInputStream()));
                         var out = new BufferedOutputStream(
                                 client.getOutputStream())) {

                        while (true) {

                            List<String> lines = new ArrayList<>();
                            String line;
                            while ((line = in.readLine()) != null) {
                                lines.add(line);
                            }

                            final var requestLine = lines.getFirst();
                            final var parts = requestLine.split(" ");

                            if (parts.length != 3) {
                                err(out);
                                continue;
                            }

                            String method = parts[0];
                            if (!validMethods.contains(method)) {
                                err(out);
                                continue;
                            }

                            String path = parts[1];
                            if (!validPaths.contains(path)) {
                                err(out);
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
                            checkRequest(request, out);

                            if (!server.doHandler(request, out)) {
                                err(out);
                            }

                        }
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                }).start();
            });
        }
    }
}