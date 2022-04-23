package com.picromedia.controllers;

import com.google.gson.Gson;
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
    private final ReentrantLock reentrantLock;
    public UserController () {
        gson = new Gson();
        reentrantLock = new ReentrantLock();
    }


    @Override
    public void lock() {
        reentrantLock.lock();
    }

    @Override
    public void unlock() {
        reentrantLock.unlock();
    }

    /*
    * By default, returns a list of all users
    * Options (In order of precedence):
    * id: If present, returns a user with the given id
    */
    @Override
    public HTTPResponse GET(HashMap<String, String> options, Connection conn) {
        HTTPResponse response = new HTTPResponse();

        try {
            if (options.containsKey("id")) {
                User user = getById(Long.parseLong(options.get("id")), conn);
                response.setBody(gson.toJson(new SendingUser(user)).getBytes(StandardCharsets.UTF_8));
            } else {
                List<User> users = getAllUsers(conn);
                response.setBody(gson.toJson(users.stream().map(SendingUser::new).toList()).getBytes(StandardCharsets.UTF_8));
            }
            response.set200();
            response.putHeader("Content-Type", "application/json");
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

    /*
    * Json Format:
    * username: string - The username of the new user
    * password: string - The password of the new user
    * email: string - The email of the new user
    */
    @Override
    public HTTPResponse POST(HashMap<String, String> options, byte[] content, Connection conn) {
        HTTPResponse response = new HTTPResponse();
        String json = new String(content, StandardCharsets.UTF_8);
        ReceivingUser user = gson.fromJson(json, ReceivingUser.class);

        try (Statement stmt = conn.createStatement()) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(user.getPassword().getBytes(StandardCharsets.UTF_8));
            stmt.executeUpdate(String.format("INSERT INTO %s (Username, PasswordHash, Email) VALUES ('%s', 0x%s, '%s');",
                    UserTable, user.getUsername(), Hex.encodeHexString(hash), user.getEmail()));

            ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
            if (!rs.next()) {
                response.set500();
                return response;
            }
            long id = rs.getLong("LAST_INSERT_ID()");
            SendingUser responseUser = new SendingUser(getById(id, conn));
            response.set201();
            response.setBody(gson.toJson(responseUser).getBytes(StandardCharsets.UTF_8));
            response.putHeader("Content-Type", "application/json");
            return response;
        } catch (SQLException | NoSuchAlgorithmException | NotFoundException e) {
            response.set500();
            return response;
        }
    }

    @Override
    public HTTPResponse PUT(HashMap<String, String> options, byte[] content, Connection conn) {
        return POST(options, content, conn);
    }

    @Override
    public HTTPResponse DELETE(HashMap<String, String> options, Connection conn) {
        HTTPResponse response = new HTTPResponse();
        long id;
        try {
            id = Long.parseLong(options.get("id"));
        } catch (NumberFormatException e) {
            response.set400();
            return response;
        }
        try (Statement stmt = conn.createStatement()) {
            int rowsAffected = stmt.executeUpdate(String.format("DELETE FROM %s WHERE Id=%d", UserTable, id));
            if (rowsAffected == 0) {
                response.set404();
                return response;
            }
            response.set204();
            return response;
        } catch (SQLException e) {
            response.set500();
            return response;
        }
    }

    private User getById(long id, Connection conn) throws SQLException, NotFoundException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM %s WHERE Id=%d", UserTable, id));
            if (!rs.next()) {
                throw new NotFoundException(String.format("No user with the id %d was found", id));
            }
            return new User(rs.getLong("Id"), rs.getString("Username"), rs.getString("PasswordHash"), rs.getString("Email"));
        }
    }

    private List<User> getAllUsers(Connection conn) throws SQLException {
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
