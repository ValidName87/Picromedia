package com.picromedia.controllers;

import java.util.HashMap;

import com.picromedia.parsing.HTTPResponse;

public interface Controller {
    HTTPResponse GET(HashMap<String, String> options);

    HTTPResponse POST(HashMap<String, String> options, byte[] content);

    HTTPResponse PUT(HashMap<String, String> options, byte[] content);

    HTTPResponse DELETE(HashMap<String, String> options);

    default HTTPResponse HEAD(HashMap<String, String> options) {
        HTTPResponse response = GET(options);
        if (response != null) {
            response.setBody(new byte[]{});
        }
        return response;
    }

    void lock();

    void unlock();
}
