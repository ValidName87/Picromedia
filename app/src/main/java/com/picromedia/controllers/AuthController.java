package com.picromedia.controllers;

import com.google.gson.Gson;
import com.picromedia.parsing.HTTPResponse;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import static com.picromedia.controllers.UserController.UserTable;

public class AuthController implements Controller {
    ReentrantLock lock;
    Gson gson;

    public AuthController() {
        lock = new ReentrantLock();
        gson = new Gson();
    }

    @Override
    public HTTPResponse GET(HashMap<String, String> options, Connection conn) {
        HTTPResponse response = new HTTPResponse();
        String[] authInfo;
        try {
            authInfo = options.get("Authorization").split(":");
        } catch (NullPointerException e) {
            response.set400();
            return response;
        }
        if (authInfo.length < 2) {
            response.set400();
            return response;
        }

        try {
            if (auth(authInfo[0], authInfo[1], conn)) {
                response.set200();
                return response;
            }
            response.set403();
            return response;
        } catch (NotFoundException e) {
            response.set404();
            return response;
        } catch (SQLException | NoSuchAlgorithmException e ) {
            response.set500();
            return response;
        }
    }

    @Override
    public HTTPResponse POST(HashMap<String, String> options, byte[] content, Connection conn) {
        HTTPResponse response = new HTTPResponse();
        response.set405("GET, HEAD");
        return response;
    }

    @Override
    public HTTPResponse PUT(HashMap<String, String> options, byte[] content, Connection conn) {
        HTTPResponse response = new HTTPResponse();
        response.set405("GET, HEAD");
        return response;
    }

    @Override
    public HTTPResponse DELETE(HashMap<String, String> options, Connection conn) {
        HTTPResponse response = new HTTPResponse();
        response.set405("GET, HEAD");
        return response;
    }

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }

    public static boolean auth(String username, String password, Connection conn) throws NotFoundException, SQLException, NoSuchAlgorithmException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(String.format("SELECT (PasswordHash) FROM %s WHERE Username=%s", UserTable, username));
            if (!rs.next()) {
                throw new NotFoundException();
            }
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] expectedHash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            byte[] hash = rs.getBytes("PasswordHash");
            return Arrays.equals(expectedHash, hash);
        }
    }

    public static boolean authById(long id, String username, String password, Connection conn) throws NotFoundException, SQLException, NoSuchAlgorithmException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(String.format("SELECT (Username, PasswordHash) FROM %s WHERE Id=%d", UserTable, id));
            if (!rs.next()) {
                throw new NotFoundException();
            }
            if (!username.equals(rs.getString("Username"))) {
                return false;
            }
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] expectedHash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            byte[] hash = rs.getBytes("PasswordHash");
            return Arrays.equals(expectedHash, hash);
        }
    }
}
