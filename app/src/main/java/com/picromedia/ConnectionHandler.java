package com.picromedia;

import java.io.*;
import java.net.Socket;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.picromedia.parsing.HTTPRequest;
import com.picromedia.parsing.HTTPResponse;
import com.picromedia.parsing.Router;

public class ConnectionHandler implements Runnable {
    private final Socket socket;
    private final Connection sqlConnection;

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
        try {
            sqlConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/picromedia",
                    SecretsManager.getSecret("SqlUser"), SecretsManager.getSecret("SqlPass"));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream out = socket.getOutputStream();

            StringBuilder sBuild = new StringBuilder();
            do {
                char character = (char)in.read();
                sBuild.append(character);
            } while (in.ready());
            HTTPRequest request = new HTTPRequest(Arrays.stream(sBuild.toString().split("\r\n")).toList());
            System.out.println(">> Read request:");
            System.out.println(request);

            HTTPResponse response = Router.getResponse(request, sqlConnection);

            System.out.println(request);
            System.out.println(response.toStringBodyless());

            out.write(response.toStringBodyless().getBytes(StandardCharsets.UTF_8));
            out.write(response.getBody());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("The socket couldn't close");
            }
        }
    }

}
