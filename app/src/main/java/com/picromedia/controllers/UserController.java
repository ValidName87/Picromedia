package com.picromedia.controllers;

import com.picromedia.DBManager;
import com.picromedia.models.User;
import com.picromedia.parsing.HTTPResponse;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class UserController implements Controller {
    private final DBManager<User> dbManager = new DBManager<>(User.class);
    private final ReentrantLock reentrantLock = new ReentrantLock();
    public UserController () {}

    @Override
    public void lock() {
        reentrantLock.lock();
    }

    @Override
    public void unlock() {
        reentrantLock.unlock();
    }

    @Override
    public HTTPResponse GET(HashMap<String, String> options) {
        return null;
    }

    @Override
    public HTTPResponse POST(HashMap<String, String> options, byte[] content) {
        return null;
    }

    @Override
    public HTTPResponse PUT(HashMap<String, String> options, byte[] content) {
        return null;
    }

    @Override
    public HTTPResponse DELETE(HashMap<String, String> options) {
        return null;
    }

    @Override
    public HTTPResponse HEAD(HashMap<String, String> options) {
        return null;
    }
}
