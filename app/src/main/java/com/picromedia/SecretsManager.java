package com.picromedia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class SecretsManager {
    private static final String secretsPath = "C:\\Users\\ctlos\\secrets\\picromedia.txt";
    private static final HashMap<String, String> secrets;
    static {
        secrets = new HashMap<>();
        try {
            String[] secretLines = Files.readString(Path.of(secretsPath), StandardCharsets.UTF_8).split("\n");
            for (String s : secretLines) {
                System.out.println("Line: " + s);
                String[] kv = s.split(": ");
                secrets.put(kv[0], kv[1]);
            }
        } catch (IOException ignored) {}
    }

    public static String getSecret(String key) {
        return secrets.get(key);
    }
}
