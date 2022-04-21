package com.picromedia.parsing;

import java.util.HashMap;
import java.util.Set;
import java.util.Arrays;

import com.picromedia.controllers.Controller;

import org.reflections.Reflections;

public class ApiURL {
    private final Controller controller;
    private final HashMap<String, String> options = new HashMap<>();
    private static final HashMap<String, Controller> controllers = new HashMap<>();

    // expects a url in the form
    // api/controller/option1=value1&option2=value2&...
    public ApiURL(String url) throws IllegalArgumentException {
        String[] splitURL = url.split("/");
        if (splitURL.length < 4) {
            throw new IllegalArgumentException();
        }
        String[] moreSplitURL = splitURL[3].split("&");
        controller = controllers.get(splitURL[2].toLowerCase());
        for (int i = 1; i < moreSplitURL.length; i++) {
            String[] option = moreSplitURL[i].split("=");
            if (option.length < 2) {
                throw new IllegalArgumentException();
            }
            options.put(option[0].toLowerCase(), String.join("=",
                    Arrays.stream(option, 1, option.length).toArray(String[]::new)));
        }
    }

    public Controller getController() {
        return controller;
    }

    public HashMap<String, String> getOptions() {
        return options;
    }

    public static void init() {
        System.out.println("Running APIUrl init");
        Reflections reflections = new Reflections("com.picromedia.controllers");
        Set<Class<? extends Controller>> controllerClasses = reflections.getSubTypesOf(Controller.class);
        controllerClasses.forEach((Class<? extends Controller> controllerClass) -> {
            Controller instance;
            try {
                instance = controllerClass.getConstructor().newInstance();
                String name = controllerClass.getSimpleName();
                name = name.substring(0, name.length()-"Controller".length()).toLowerCase();
                controllers.put(name, instance);
                System.out.println(name);
                System.out.println(instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
