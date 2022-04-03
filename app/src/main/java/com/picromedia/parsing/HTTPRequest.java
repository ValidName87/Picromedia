package com.picromedia.parsing;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class HTTPRequest {
    private String verb;
    private String path;
    private String version;
    private final HashMap<String, String> headers;
    private byte[] body;

    public HTTPRequest(List<String> request) {
        try {
            String[] firstLine = request.get(0).split(" ");
            verb = firstLine[0].toUpperCase();
            path = firstLine[1];
            version = firstLine[2];
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            verb = "";
            path = "";
            version = "HTTP/1.1";
        }

        if (path.startsWith("http://")) {
            path = path.substring(7);
        } else if (path.startsWith("https://")) {
            path = path.substring(8);
        }

        headers = new HashMap<>();
        String currentLine = request.size() > 1 ? request.get(1) : "";
        int i = 2;
        while (!currentLine.isEmpty()) {
            String[] kv = currentLine.split(": ");
            headers.put(kv[0], kv[1]);
            currentLine = request.size() > i ? request.get(i) : "";
            i++;
        }

        body = (request.size() > i + 1 ? request.get(i + 1) : "").getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(verb).append(" ").append(path).append(" ").append(version).append("\r\n");
        headers.forEach((String key, String value) -> s.append(key).append(": ").append(value).append("\r\n"));
        s.append("\r\n");
        s.append(new String(body, StandardCharsets.UTF_8));
        return s.toString();
    }

    public String getVerb() {
        return verb;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public String getHeaderValue(String header) {
        return headers.get(header);
    }

    public byte[] getBody() {
        return body;
    }
}
