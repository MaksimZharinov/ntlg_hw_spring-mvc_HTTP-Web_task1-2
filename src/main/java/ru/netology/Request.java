package ru.netology;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Request implements RequestContext {

    private final String method;
    private final String path;
    private final List<String> headers;
    private final String body;
    private final List<NameValuePair> queryParams;
    private final List<NameValuePair> postParams;
    private final List<FileItem> parts;

    public Request(String method, String path,
                   List<String> headers, String body) throws FileUploadException {
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
            var factory = new DiskFileItemFactory();
            factory.setSizeThreshold(100);
            factory.setRepository(new File("temp"));
            var upload = new ServletFileUpload(factory);
            parts = upload.parseRequest(this);

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

    public byte[] getPart(String name) {
        Iterator<FileItem> iterator = parts.iterator();
        while (iterator.hasNext()) {
            FileItem part = iterator.next();
            if (part.isFormField()) {
                if (name.equals(part.getFieldName())) {
                    return part.getString().getBytes();
                }
            } else {
                if (name.equals(part.getName())) {
                    return part.get();
                }
            }
        }
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        //  stub
        return "";
    }

    @Override
    public String getContentType() {
        //  stub
        return "";
    }

    @Override
    public int getContentLength() {
        //  stub
        return 0;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        //  stub
        return null;
    }
}