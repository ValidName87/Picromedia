package com.picromedia.parsing;

import java.util.HashMap;

import com.picromedia.controllers.Controller;

public class ApiURL {
    private Controller controller;
    private HashMap<String, String> options;

    public ApiURL(String url) throws IllegalArgumentException {
        String[] splitURL = url.split("/");
        if (splitURL.length < 3) {
            throw new IllegalArgumentException();
        }

    }
}
