package com.picromedia.controllers;

import com.google.gson.Gson;
import com.picromedia.models.Message;
import com.picromedia.parsing.HTTPResponse;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class MessageController implements Controller {
    private static final String MessageTable = "Message";
    private final Gson gson;
    private final ReentrantLock reentrantLock;

    public MessageController() {
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
     * By default, returns a list of all message
     * Options (In order of precedence):
     * id: If present, get the message with the given id
     * madeBy: If present, gets the messages made by the user with the given id
     * withPuzzle: If present, gets the message attached to the puzzle with the given id
     */
    @Override
    public HTTPResponse GET(HashMap<String, String> options, Connection conn) {
        HTTPResponse response = new HTTPResponse();

        try {
            if (options.containsKey("id")) {
                Message message = getById(Long.parseLong(options.get("id")), conn);
                response.setBody(gson.toJson(message).getBytes(StandardCharsets.UTF_8));
            } else if (options.containsKey("madeby")) {
                List<Message> messages = getByCreator(Long.parseLong(options.get("madeby")), conn);
                response.setBody(gson.toJson(messages).getBytes(StandardCharsets.UTF_8));
            } else if (options.containsKey("withpuzzle")) {
                Message message = getByPuzzleId(Long.parseLong(options.get("withpuzzle")), conn);
                response.setBody(gson.toJson(message).getBytes(StandardCharsets.UTF_8));
            } else {
                List<Message> messages = getAllMessages(conn);
                response.setBody(gson.toJson(messages).getBytes(StandardCharsets.UTF_8));
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
    * Json format:
    * creatorId: number - The id of the creator of the message,
    * puzzleId: number - The id of the puzzle associated with the message,
    * message: string - The actual text of the message
    */
    @Override
    public HTTPResponse POST(HashMap<String, String> options, byte[] content, Connection conn) {
        HTTPResponse response = new HTTPResponse();
        String json = new String(content, StandardCharsets.UTF_8);
        Message message = gson.fromJson(json, Message.class);
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(String.format("INSERT INTO %s (CreatorId, PuzzleId, Message) VALUES (%d, %d, '%s');",
                    MessageTable, message.getCreatorId(), message.getPuzzleId(), message.getMessage()));

            ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
            if (!rs.next()) {
                response.set500();
                return response;
            }
            long id = rs.getLong("LAST_INSERT_ID()");
            Message createdMessage = getById(id, conn);
            response.set201();
            response.setBody(gson.toJson(createdMessage).getBytes(StandardCharsets.UTF_8));
            response.putHeader("Content-Type", "application/json");
            return response;
        } catch (NotFoundException e) {
            response.set500();
            return response;
        } catch (SQLException e) {
            switch (e.getErrorCode()) {
                case 1452:
                    response.setStatusCode("422 Unprocessable Entity");
                    break;
                case 1062:
                    response.setStatusCode("409 Conflict");
                default:
                    response.set500();
            }
            return response;
        }
    }

    /*
     * Currently just adds a new rating
     * Options:
     * action (required): What action to do
     *   Values: UpdateMessage
     *
     * Json Format:
     * id: number - The id of the message to be formatted
     * message: string (UpdateMessage only) - The new text for the message
     */
    @Override
    public HTTPResponse PUT(HashMap<String, String> options, byte[] content, Connection conn) {
        HTTPResponse response = new HTTPResponse();
        String action = options.get("action");
        if (action == null) {
            response.set400();
            return response;
        }
        response.set204();
        switch (action) {
            case "updatemessage" -> response = updateMessage(content, conn);
            default -> response.set400();
        }
        return response;
    }

    /*
     * Options (required):
     * id: The id of the puzzle to delete
     */
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
            int rowsAffected = stmt.executeUpdate(String.format("DELETE FROM %s WHERE Id=%d", MessageTable, id));
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

    private Message getById(long id, Connection conn) throws SQLException, NotFoundException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM %s WHERE Id=%d", MessageTable, id));
            if (!rs.next()) {
                throw new NotFoundException(String.format("No message with the id %d was found", id));
            }
            return new Message(rs.getLong("Id"), rs.getLong("CreatorId"),
                    rs.getLong("PuzzleId"), rs.getString("Message"));
        }
    }

    private List<Message> getByCreator(long id, Connection conn) throws SQLException, NotFoundException {
        try (Statement stmt = conn.createStatement()) {
            List<Message> messageList = new ArrayList<>();
            ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM %s WHERE CreatorId=%d", MessageTable, id));
            while (rs.next()) {
                messageList.add(new Message(rs.getLong("Id"), rs.getLong("CreatorId"),
                        rs.getLong("PuzzleId"), rs.getString("Message")));
            }
            return messageList;
        }
    }

    private Message getByPuzzleId(long id, Connection conn) throws SQLException, NotFoundException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM %s WHERE PuzzleId=%d", MessageTable, id));
            if (!rs.next()) {
                throw new NotFoundException(String.format("No message with the puzzle id=%d was found", id));
            }
            return new Message(rs.getLong("Id"), rs.getLong("CreatorId"),
                    rs.getLong("PuzzleId"), rs.getString("Message"));
        }
    }

    private List<Message> getAllMessages(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            List<Message> messageList = new ArrayList<>();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + MessageTable);
            while (rs.next()) {
                messageList.add(new Message(rs.getLong("Id"), rs.getLong("CreatorId"),
                        rs.getLong("PuzzleId"), rs.getString("Message")));
            }
            return messageList;
        }
    }

    static class MessageUpdater {
        long id;
        String message;
    }

    private HTTPResponse updateMessage(byte[] content, Connection conn) {
        HTTPResponse response = new HTTPResponse();
        try {
            String json = new String(content, StandardCharsets.UTF_8);
            MessageUpdater message = gson.fromJson(json, MessageUpdater.class);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(String.format("UPDATE %s SET Message='%s' WHERE Id=%d",
                        MessageTable, message.message, message.id));
                response.set204();
                return response;
            }
        } catch (SQLException e) {
            response.set500();
            return response;
        }
    }
}
