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

    public ApiURL(String url) throws IllegalArgumentException {
        String[] splitURL = url.split("/");
        if (splitURL.length < 3) {
            throw new IllegalArgumentException();
        }
        String[] moreSplitURL = splitURL[2].split("\\?");
        controller = controllers.get(moreSplitURL[0]);
        for (int i = 1; i < moreSplitURL.length; i++) {
            String[] option = moreSplitURL[i].split("=");
            if (option.length < 2) {
                throw new IllegalArgumentException();
            }
            options.put(option[0], String.join("=",
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
                String name = controllerClass.getName().substring(0, "Controller".length());
                controllers.put(name, instance);
                System.out.println(name);
                System.out.println(instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
