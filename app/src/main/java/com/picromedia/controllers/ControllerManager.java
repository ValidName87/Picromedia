package com.picromedia.controllers;

import java.util.HashMap;

import com.picromedia.parsing.HTTPResponse;

public class ControllerManager {
    private final HashMap<String, Controller> controllers;

    public ControllerManager() {
        controllers = new HashMap<>();
    }

    public HTTPResponse GET(String path) {
        Controller controller = controllers.get(path.split("/")[1]);
        if (controller == null) {
            HTTPResponse response = new HTTPResponse();
            response.set400();
            return response;
        }
        controller.lock();
        try {
            return controller.GET(path);
        } finally {
            controller.unlock();
        }
    }

    public HTTPResponse POST(String path, byte[] content) {
        Controller controller = controllers.get(path.split("/")[1]);
        if (controller == null) {
            HTTPResponse response = new HTTPResponse();
            response.set400();
            return response;
        }
        controller.lock();
        try {
            return controller.POST(path, content);
        } finally {
            controller.unlock();
        }
    }

    public HTTPResponse PUT(String path, byte[] content) {
        Controller controller = controllers.get(path.split("/")[1]);
        if (controller == null) {
            HTTPResponse response = new HTTPResponse();
            response.set400();
            return response;
        }
        controller.lock();
        try {
            return controller.PUT(path, content);
        } finally {
            controller.unlock();
        }
    }

    public HTTPResponse DELETE(String path) {
        Controller controller = controllers.get(path.split("/")[1]);
        if (controller == null) {
            HTTPResponse response = new HTTPResponse();
            response.set400();
            return response;
        }
        controller.lock();
        try {
            return controller.DELETE(path);
        } finally {
            controller.unlock();
        }
    }

    public HTTPResponse HEAD(String path) {
        Controller controller = controllers.get(path.split("/")[1]);
        if (controller == null) {
            HTTPResponse response = new HTTPResponse();
            response.set400();
            return response;
        }
        controller.lock();
        try {
            return controller.HEAD(path);
        } finally {
            controller.unlock();
        }
    }
}
