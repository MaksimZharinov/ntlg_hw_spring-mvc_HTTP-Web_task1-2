package ru.netology;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class Main {
  public static void main(String[] args) {
    final var validPaths = List.of("/index.html", "/spring.svg",
            "/spring.png", "/resources.html", "/styles.css", "/app.js",
            "/links.html", "/forms.html", "/classic.html",
            "/events.html", "/events.js");
    final var connections = Executors.newFixedThreadPool(64);
    final var clients = new ConcurrentHashMap<Integer, Socket>();

    final var server = new Server(9999);
    server.start();
    System.out.println("Server is up.");

      while (true) {
        connections.execute(() -> {
          final var client = server.connect();
          new Thread(() -> {
            clients.put(client.getPort(), client);
            try {
              while (true) {
                  var in = new BufferedReader(
                          new InputStreamReader(
                          client.getInputStream()));
                  var out = new BufferedOutputStream(
                          client.getOutputStream());
                  final var requestLine = in.readLine();
                  final var parts = requestLine.split(" ");

                  if (parts.length != 3) {
                      continue;
                  }

                  final var path = parts[1];
                  if (!validPaths.contains(path)) {
                      out.write((
                              "HTTP/1.1 404 Not Found\r\n" +
                                      "Content-Length: 0\r\n" +
                                      "Connection: close\r\n" +
                                      "\r\n"
                      ).getBytes());
                      out.flush();
                      continue;
                  }

                  final var filePath = Path.of(".", "public", path);
                  final var mimeType = Files.probeContentType(filePath);

                  if (path.equals("/classic.html")) {
                      final var template = Files.readString(filePath);
                      final var content = template.replace(
                              "{time}",
                              LocalDateTime.now().toString()
                      ).getBytes();
                      out.write((
                              "HTTP/1.1 200 OK\r\n" +
                                      "Content-Type: " + mimeType + "\r\n" +
                                      "Content-Length: " + content.length + "\r\n" +
                                      "Connection: close\r\n" +
                                      "\r\n"
                      ).getBytes());
                      out.write(content);
                      out.flush();
                      continue;
                  }

                  final var length = Files.size(filePath);
                  out.write((
                          "HTTP/1.1 200 OK\r\n" +
                                  "Content-Type: " + mimeType + "\r\n" +
                                  "Content-Length: " + length + "\r\n" +
                                  "Connection: close\r\n" +
                                  "\r\n"
                  ).getBytes());
                  Files.copy(filePath, out);
                  out.flush();
              }
            } catch (IOException e) {
              System.err.println(e);
            }
          }).start();
        });
      }
  }
}