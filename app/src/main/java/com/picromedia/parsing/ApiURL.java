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
        System.out.println(Arrays.toString(splitURL));
        if (splitURL.length < 3) {
            throw new IllegalArgumentException();
        }
        controller = controllers.get(splitURL[2].toLowerCase());
        if (splitURL.length == 3) {
            return;
        }
        String[] moreSplitURL = splitURL[3].split("&");
        for (String s : moreSplitURL) {
            String[] option = s.split("=");
            if (option.length < 2) {
                throw new IllegalArgumentException();
            }
            String key = option[0].toLowerCase();
            String value = String.join("=",
                    Arrays.stream(option, 1, option.length).toArray(String[]::new)).toLowerCase();
            options.put(key, value);
        }
    }

    public Controller getController() {
        return controller;
    }

    public HashMap<String, String> getOptions() {
        return options;
    }

    public static void init() {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
