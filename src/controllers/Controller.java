package controllers;

import parsing.HTTPResponse;

public interface Controller {
    public HTTPResponse GET(String path);

    public HTTPResponse POST(String path, byte[] content);

    public HTTPResponse PUT(String path, byte[] content);

    public HTTPResponse DELETE(String path);

    default HTTPResponse HEAD(String path) {
        HTTPResponse response = GET(path);
        response.setBody(new byte[] {});
        return response;
    }
}
