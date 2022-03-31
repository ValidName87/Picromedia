package controllers;

import parsing.HTTPResponse;

public interface Controller {
    public HTTPResponse GET(String path);

    public HTTPResponse POST(String path, byte[] content);

    public HTTPResponse PUT(String path, byte[] content);

    public HTTPResponse DELETE(String path);

    public HTTPResponse HEAD(String path);

    public void lock();

    public void unlock();
}
