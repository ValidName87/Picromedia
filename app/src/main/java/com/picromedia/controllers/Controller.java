package com.picromedia.controllers;

import java.util.HashMap;

import com.picromedia.parsing.HTTPResponse;

public interface Controller {
    public HTTPResponse GET(HashMap<String, String> options);

    public HTTPResponse POST(HashMap<String, String> options, byte[] content);

    public HTTPResponse PUT(HashMap<String, String> options, byte[] content);

    public HTTPResponse DELETE(HashMap<String, String> options);

    public HTTPResponse HEAD(HashMap<String, String> options);

    public void lock();

    public void unlock();
}
