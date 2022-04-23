package com.picromedia;

import com.google.gson.Gson;
import com.picromedia.controllers.MessageController;
import com.picromedia.controllers.PicrossPuzzleController;
import com.picromedia.models.PicrossPuzzle;
import com.picromedia.parsing.ApiURL;
import com.picromedia.parsing.HTTPResponse;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
        ApiURL.init();
        System.out.println(">> Server starting!");
        ExecutorService threadPool = Executors.newFixedThreadPool(4);
        try (ServerSocket socket = new ServerSocket(4747)) {
            while (true) {
                Socket clientSocket = socket.accept();
                threadPool.execute(new ConnectionHandler(clientSocket));
            }
        }
    }
}
