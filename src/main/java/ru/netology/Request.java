package ru.netology;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Request {

    private List<String> headers = new ArrayList<>();
    private String body;
    private BufferedReader in;
    private String request;
    private String path;

    public Request(String request, String path, Socket client) {
        this.request = request;
        this.path = path;
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public Object request() {
        boolean check = false;
        for (String header : headers) {
            if (request.equals(header)) {
                check = true;
            }
        }
        if (check) {
            return body;
        }
        return in;
    }
}
