package com.picromedia.controllers;

import com.google.gson.Gson;
import com.picromedia.SecretsManager;
import com.picromedia.models.ReceivingUser;
import com.picromedia.models.User;
import com.picromedia.models.SendingUser;
import com.picromedia.parsing.HTTPResponse;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


public class UserController implements Controller {
    private static final String UserTable = "User";
    private final Gson gson;
    private final Connection conn;
    private final ReentrantLock reentrantLock;
    public UserController () {
        gson = new Gson();
        reentrantLock = new ReentrantLock();
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/picromedia",
                    SecretsManager.getSecret("SqlUser"), SecretsManager.getSecret("SqlPass"));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


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
        HTTPResponse response = new HTTPResponse();
        if (options.containsKey("id")) {
            try {
                User user = getById(Long.parseLong(options.get("id")));
                response.set200();
                response.setBody(gson.toJson(new SendingUser(user)).getBytes(StandardCharsets.UTF_8));
                return response;
            } catch (NumberFormatException e) {
                response.set400();
                return response;
            } catch (SQLException e) {
                response.set500();
                return response;
            } catch (NotFoundException e) {
                response.set404();
                return response;
            }
        }
        List<User> users;
        try {
            users = getAllUsers();
        } catch (SQLException e) {
            response.set500();
            return response;
        }
        response.set200();
        response.setBody(gson.toJson(users.stream().map(SendingUser::new).toList()).getBytes(StandardCharsets.UTF_8));
        return response;
    }

    @Override
    public HTTPResponse POST(HashMap<String, String> options, byte[] content) {
        HTTPResponse response = new HTTPResponse();
        String json = new String(content, StandardCharsets.UTF_8);
        ReceivingUser user = gson.fromJson(json, ReceivingUser.class);

        try (Statement stmt = conn.createStatement()) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(user.getPassword().getBytes(StandardCharsets.UTF_8));
            stmt.executeUpdate("INSERT INTO " + UserTable + " (Username, PasswordHash, Email) VALUES (\"" +
                    user.getUsername() + "\", 0x" + Hex.encodeHexString(hash) + ", \"" + user.getEmail() + "\");");

            ResultSet rs = stmt.executeQuery("SELECT * FROM " + UserTable + " WHERE Email=\"" + user.getEmail() + "\"");
            rs.next();
            SendingUser responseUser = new SendingUser(rs.getLong("Id"), rs.getString("Username"), rs.getString("Email"));
            response.setBody(gson.toJson(responseUser).getBytes(StandardCharsets.UTF_8));
            response.setStatusCode("201 Created");
            return response;
        } catch (SQLException | NoSuchAlgorithmException e) {
            response.set500();
            return response;
        }
    }

    @Override
    public HTTPResponse PUT(HashMap<String, String> options, byte[] content) {
        return POST(options, content);
    }

    @Override
    public HTTPResponse DELETE(HashMap<String, String> options) {
        HTTPResponse response = new HTTPResponse();
        if (!options.containsKey("id")) {
            response.set400();
            return response;
        }
        long id;
        try {
            id = Long.parseLong(options.get("id"));
        } catch (NumberFormatException e) {
            response.set400();
            return response;
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(String.format("DELETE FROM " + UserTable + " WHERE Id=%d", id));
            response.set204();
            return response;
        } catch (SQLException e) {
            response.set500();
            return response;
        }
    }

    private User getById(long id) throws SQLException, NotFoundException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM " + UserTable + " WHERE Id=%d", id));
            if (!rs.next()) {
                throw new NotFoundException(String.format("No user with the id %d was found", id));
            }
            return new User(rs.getLong("Id"), rs.getString("Username"), rs.getString("PasswordHash"), rs.getString("Email"));
        }
    }

    private List<User> getAllUsers() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            List<User> userList = new ArrayList<>();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + UserTable);
            while (rs.next()) {
                userList.add(new User(rs.getLong("Id"), rs.getString("Username"), rs.getString("PasswordHash"), rs.getString("Email")));
            }
            return userList;
        }
    }
}
