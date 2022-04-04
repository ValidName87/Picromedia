package com.picromedia.parsing;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.IntStream;

import com.picromedia.controllers.Controller;

import org.reflections.Reflections;

public class ApiURL {
    private Controller controller;
    private HashMap<String, String> options = new HashMap<>();
    private static HashMap<String, Controller> controllers = getControllers();

    public ApiURL(String url) throws IllegalArgumentException {
        String[] splitURL = url.split("/");
        if (splitURL.length < 3) {
            throw new IllegalArgumentException();
        }
        String[] moreSplitURL = splitURL[2].split("?");
        controller = controllers.get(moreSplitURL[0]);
        for (int i = 1; i < moreSplitURL.length; i++) {
            String[] option = moreSplitURL[i].split("=");
            if (option.length < 2) {
                throw new IllegalArgumentException();
            }
            options.put(option[0], String.join("=",
                    IntStream.range(1, option.length).mapToObj(j -> option[j]).toArray(String[]::new)));
        }
    }

    public Controller getController() {
        return controller;
    }

    public HashMap<String, String> getOptions() {
        return options;
    }

    private static HashMap<String, Controller> getControllers() {
        Reflections reflections = new Reflections("com.picromedia.controllers");
        Set<Class<? extends Controller>> controllerClasses = reflections.getSubTypesOf(Controller.class);
        HashMap<String, Controller> controllers = new HashMap<>();
        controllerClasses.forEach((Class<? extends Controller> controllerClass) -> {
            Controller instance;
            try {
                instance = controllerClass.getConstructor().newInstance();
                String name = controllerClass.getName().substring(0, "Controller".length());
                controllers.put(name, instance);
            } catch (Exception e) {
            }
        });
        return controllers;
    }
}
