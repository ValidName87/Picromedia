package com.picromedia.controllers;

import java.sql.Connection;
import java.util.HashMap;

import com.picromedia.parsing.HTTPResponse;

public interface Controller {
    HTTPResponse GET(HashMap<String, String> options, Connection conn);

    HTTPResponse POST(HashMap<String, String> options, byte[] content, Connection conn);

    HTTPResponse PUT(HashMap<String, String> options, byte[] content, Connection conn);

    HTTPResponse DELETE(HashMap<String, String> options, Connection conn);

    default HTTPResponse HEAD(HashMap<String, String> options, Connection conn) {
        HTTPResponse response = GET(options, conn);
        if (response != null) {
            response.putHeader("Content-Length", String.valueOf(response.getBody().length));
            response.setBody(new byte[]{});
        }
        return response;
    }

    void lock();

    void unlock();
}
