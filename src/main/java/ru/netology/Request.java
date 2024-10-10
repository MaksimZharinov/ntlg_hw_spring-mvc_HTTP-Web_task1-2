package ru.netology;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class Request {

    private final String method;
    private final String path;
    private final List<String> headers;
    private final String body;
    private final List<NameValuePair> queryParams;
    private final List<NameValuePair> postParams;
    private final List<FileItem> parts;

    public Request(String method, String path,
                   List<String> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
        queryParams = URLEncodedUtils.parse(path, StandardCharsets.UTF_8);
        var contentType = headers.stream()
                .filter(h -> h.contains("Content-Type"))
                .collect(Collectors.joining());
        if (contentType.contains("application/x-www-form-urlencoded")) {
            postParams = URLEncodedUtils.parse(body, StandardCharsets.UTF_8);
        } else {
            postParams = null;
        }
        if (contentType.contains("multipart/form-data")) {
            var upload = new ServletFileUpload();
            try {
                parts = upload.parseRequest(new ByteArrayInputStream(
                                body.getBytes(StandardCharsets.UTF_8)),
                        headers);
            } catch (FileUploadException ex) {
                System.err.println(ex);
            }
        } else {
            parts = null;
        }
    }

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

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    public List<NameValuePair> getQueryParam(String name) {
        return queryParams.stream()
                .filter(p -> p.getName().equals(name))
                .collect(Collectors.toList());
    }

    public List<NameValuePair> getPostParams() {
        return postParams;
    }

    public List<NameValuePair> getPostParam(String name) {
        return postParams.stream()
                .filter(p -> p.getName().equals(name))
                .collect(Collectors.toList());
    }

    public List<FileItem> getParts() {
        return parts;
    }
}