package ru.netology;

import java.util.List;

public class Request {

    private final String method;
    private final String path;
    private final List<String> headers;
    private final String body;

    public Request(String method, String path, List<String> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

//    public void parsingRequestLine(String requestLine) throws IOException {
//
//        final var filePath = Path.of(".", "public", path);
//        final var mimeType = Files.probeContentType(filePath);
//
//
//
//        if (path.equals("/classic.html")) {
//            final var template = Files.readString(filePath);
//            final var content = template.replace(
//                    "{time}",
//                    LocalDateTime.now().toString()
//            ).getBytes();
//            out.write((
//                    "HTTP/1.1 200 OK\r\n" +
//                            "Content-Type: " + mimeType + "\r\n" +
//                            "Content-Length: " + content.length + "\r\n" +
//                            "Connection: close\r\n" +
//                            "\r\n"
//            ).getBytes());
//            out.write(content);
//            out.flush();
//        }
//
//        final var length = Files.size(filePath);
//        out.write((
//                "HTTP/1.1 200 OK\r\n" +
//                        "Content-Type: " + mimeType + "\r\n" +
//                        "Content-Length: " + length + "\r\n" +
//                        "Connection: close\r\n" +
//                        "\r\n"
//        ).getBytes());
//        Files.copy(filePath, out);
//        out.flush();
//    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getBody() {
        return body;
    }

    public List<String> getHeaders() {
        return headers;
    }
}