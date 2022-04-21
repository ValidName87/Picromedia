package com.picromedia.parsing;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class HTTPResponse {
    private String version;
    private String code;
    private final HashMap<String, String> headers;
    private byte[] body;

    public HTTPResponse() {
        version = "HTTP/1.1";
        code = "";
        headers = new HashMap<>();
        body = new byte[] {};
        ZonedDateTime date = ZonedDateTime.now(ZoneId.of("GMT"));
        headers.put("Date", date.format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }

    public String toStringBodyless() {
        StringBuilder s = new StringBuilder();
        s.append(version).append(" ").append(code).append("\r\n");
        headers.forEach((String key, String value) -> s.append(key).append(": ").append(value).append("\r\n"));
        s.append("\r\n");
        return s.toString();
    }

    public String getVersion() {
        return version;
    }

    public String getStatusCode() {
        return code;
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

    public void setVersion(String version) {
        this.version = version;
    }

    public void setStatusCode(String code) {
        this.code = code;
    }

    public void putHeader(String header, String value) {
        headers.put(header, value);
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void set200() {
        this.code = "200 OK";
    }

    public void set204() {
        this.code = "204 No Content";
    }

    public void set400() {
        this.code = "400 Bad Request";
    }

    public void set401() {
        this.code = "401 Unauthorized";
    }

    public void set403() {
        this.code = "403 Forbidden";
    }

    public void set404() {
        this.code = "404 Not Found";
    }

    public void set500() {
        this.code = "500 Internal Server Error";
    }

    public void set503() {
        this.code = "503 Service Unavailable";
    }
}
