package ru.netology;

import org.apache.commons.fileupload.FileUploadException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class Main {

    final static List<String> validPaths = List.of("/index.html",
            "/spring.svg", "/spring.png", "/resources.html", "/styles.css",
            "/app.js", "/links.html", "/forms.html", "/classic.html",
            "/events.html", "/events.js");
    public static final String GET = "GET";
    public static final String POST = "POST";
    final static String notFound404 =
            "HTTP/1.1 404 Not Found\r\n" +
                    "Content-Length: 0\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

    public static void main(String[] args) {

        final var validMethods = List.of(GET, POST);
        final var connections = Executors.newFixedThreadPool(64);
        final var clients = new ConcurrentHashMap<Integer, Socket>();

        final var server = new Server();

        server.addHandler("GET", "/messages",
                (request, responseStream) -> {

                    try {

                        final var filePath =
                                Path.of(".", "public", request.getPath());
                        final var mimeType =
                                Files.probeContentType(filePath);

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

        server.addHandler("POST", "/messages",
                (request, responseStream) -> {

                    try {

                        final var filePath =
                                Path.of(".", "public", request.getPath());
                        final var mimeType =
                                Files.probeContentType(filePath);


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
                    try (var in = new BufferedInputStream(
                            client.getInputStream());
                         var out = new BufferedOutputStream(
                                 client.getOutputStream())) {

                        while (true) {

                            final var limit = 4096;

                            in.mark(limit);
                            final var buffer = new byte[limit];
                            final var read = in.read(buffer);

                            final var requestLineDelimiter = new byte[]{'\r', '\n'};
                            final var requestLineEnd =
                                    indexOf(buffer, requestLineDelimiter, 0, read);
                            if (requestLineEnd == -1) {
                                err(out);
                                continue;
                            }

                            final var requestLine =
                                    new String(Arrays.copyOf(buffer, requestLineEnd))
                                            .split(" ");
                            if (requestLine.length != 3) {
                                err(out);
                                continue;
                            }

                            final var method = requestLine[0];
                            if (!validMethods.contains(method)) {
                                err(out);
                                continue;
                            }

                            final var path = requestLine[1];
                            if (!path.startsWith("/")) {
                                err(out);
                                continue;
                            }

                            final var headersDelimiter =
                                    new byte[]{'\r', '\n', '\r', '\n'};
                            final var headersStart =
                                    requestLineEnd + requestLineDelimiter.length;
                            final var headersEnd =
                                    indexOf(buffer, headersDelimiter,
                                            headersStart, read);
                            if (headersEnd == -1) {
                                err(out);
                                continue;
                            }

                            in.reset();
                            in.skip(headersStart);

                            final var headersBytes =
                                    in.readNBytes(headersEnd - headersStart);
                            final var headers = Arrays.asList(new String(headersBytes)
                                    .split("\r\n"));

                            String body = null;

                            if (!method.equals(GET)) {
                                in.skip(headersDelimiter.length);
                                final var contentLength =
                                        extractHeader(headers, "Content-Length");
                                if (contentLength.isPresent()) {
                                    final var length =
                                            Integer.parseInt(contentLength.get());
                                    final var bodyBytes =
                                            in.readNBytes(length);

                                    body = new String(bodyBytes);
                                }
                            }

                            final var request = new Request(method, path,
                                    headers, body);

//                            out.write((
//                                    "HTTP/1.1 200 OK\r\n" +
//                                            "Content-Length: 0\r\n" +
//                                            "Connection: close\r\n" +
//                                            "\r\n"
//                            ).getBytes());
//                            out.flush();

                        }
                    } catch (IOException | FileUploadException e) {
                        System.err.println(e);
                    }
                }).start();
            });
        }
    }

    private static void err(BufferedOutputStream out) throws IOException {
        out.write(notFound404.getBytes());
        out.flush();
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }
}