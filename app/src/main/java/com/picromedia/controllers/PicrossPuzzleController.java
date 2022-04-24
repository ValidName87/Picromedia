package com.picromedia.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.picromedia.models.PicrossPuzzle;
import com.picromedia.parsing.HTTPResponse;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class PicrossPuzzleController implements Controller {
    static final String PicrossTable = "PicrossPuzzle";
    private final Gson gson;
    private final ReentrantLock reentrantLock;

    public PicrossPuzzleController() {
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
    * By default, returns a list of all puzzles
    * Options (In order of precedence):
    * id: If present, get the puzzle with the given id
    * madeBy: If present, gets the puzzles made by the user with the given id
    */
    @Override
    public HTTPResponse GET(HashMap<String, String> options, Connection conn) {
        HTTPResponse response = new HTTPResponse();

        try {
            if (options.containsKey("id")) {
                PicrossPuzzle puzzle = getById(Long.parseLong(options.get("id")), conn);
                response.setBody(gson.toJson(puzzle).getBytes(StandardCharsets.UTF_8));
            } else if (options.containsKey("madeby")) {
                List<PicrossPuzzle> puzzles = getByCreator(Long.parseLong(options.get("madeby")), conn);
                response.setBody(gson.toJson(puzzles).getBytes(StandardCharsets.UTF_8));
            } else {
                List<PicrossPuzzle> puzzles = getAllPuzzles(conn);
                response.setBody(gson.toJson(puzzles).getBytes(StandardCharsets.UTF_8));
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
    * creatorId: number - The id of the puzzle creator
    * solution: 2d number array - Each inner array represents one row of the board;
    *   a value of 0 means the given square is empty, and 1 means filled
    * ratings: object - The keys are the ids of the people leaving ratings and the values are the ratings
    */
    @Override
    public HTTPResponse POST(HashMap<String, String> options, byte[] content, Connection conn) {
        HTTPResponse response = new HTTPResponse();
        String json = new String(content, StandardCharsets.UTF_8);
        PicrossPuzzle puzzle = gson.fromJson(json, PicrossPuzzle.class);

        String[] authInfo;
        try {
            authInfo = options.get("Authorization").split(":");
        } catch (NullPointerException e) {
            response.set403();
            return response;
        }
        try {
            if (!AuthController.authById(puzzle.getCreatorId(), authInfo[0], authInfo[1], conn)) {
                response.set403();
                return response;
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            response.set500();
            return response;
        } catch (NotFoundException e) {
            response.set404();
            return response;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(String.format("INSERT INTO %s (CreatorId, Solution, Ratings) VALUES (%d, '%s', '%s');",
                    PicrossTable, puzzle.getCreatorId(), gson.toJson(puzzle.getSolution()), gson.toJson(puzzle.getRatings())));

            ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
            if (!rs.next()) {
                response.set500();
                return response;
            }
            long id = rs.getLong("LAST_INSERT_ID()");
            PicrossPuzzle createdPuzzle = getById(id, conn);
            response.set201();
            response.setBody(gson.toJson(createdPuzzle).getBytes(StandardCharsets.UTF_8));
            response.putHeader("Content-Type", "application/json");
            return response;
        } catch (NotFoundException e) {
            response.set500();
            return response;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1452) {
                response.setStatusCode("422 Unprocessable Entity");
                return response;
            }
            response.set500();
            return response;
        }
    }

    /*
    * Options:
    * action (required): What action to do
    *   Values: UpdateRatings, UpdateSolution
    *
    * JsonFormat:
    * id: number - The id of the puzzle to update
    * ratings: object (UpdateRatings only) - The keys are the ids of the people leaving ratings
    *   and the values are the ratings
    * solution: 2d number array (UpdateSolution only) - Each inner array represents one row of the board;
     *   a value of 0 means the given square is empty, and 1 means filled
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

        String[] authInfo;
        try {
            authInfo = options.get("Authorization").split(":");
        } catch (NullPointerException e) {
            response.set403();
            return response;
        }

        switch (action) {
            case "updateratings" -> response = updateRatings(content, conn, authInfo[0], authInfo[1]);
            case "updatesolution" -> response = updateSolution(content, conn, authInfo[0], authInfo[1]);
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

        String[] authInfo;
        try {
            authInfo = options.get("Authorization").split(":");
        } catch (NullPointerException e) {
            response.set403();
            return response;
        }
        try {
            PicrossPuzzle puzzle = getById(id, conn);
            if (!AuthController.authById(puzzle.getCreatorId(), authInfo[0], authInfo[1], conn)) {
                response.set403();
                return response;
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            response.set500();
            return response;
        } catch (NotFoundException e) {
            response.set404();
            return response;
        }

        try (Statement stmt = conn.createStatement()) {
            int rowsAffected = stmt.executeUpdate(String.format("DELETE FROM %s WHERE Id=%d", PicrossTable, id));
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

    private PicrossPuzzle getById(long id, Connection conn) throws SQLException, NotFoundException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM %s WHERE Id=%d", PicrossTable, id));
            if (!rs.next()) {
                throw new NotFoundException(String.format("No puzzle with the id %d was found", id));
            }
            Type hashMapToken = new TypeToken<HashMap<Long, Integer>>() {}.getType();
            return new PicrossPuzzle(rs.getLong("id"), rs.getLong("CreatorId"),
                    gson.fromJson(rs.getString("Solution"), int[][].class), gson.fromJson(rs.getString("Ratings"), hashMapToken));
        }
    }

    private List<PicrossPuzzle> getByCreator(long id, Connection conn) throws SQLException, NotFoundException {
        try (Statement stmt = conn.createStatement()) {
            List<PicrossPuzzle> puzzleList = new ArrayList<>();
            ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM %s WHERE CreatorId=%d", PicrossTable, id));
            Type hashMapToken = new TypeToken<HashMap<Long, Integer>>() {}.getType();
            while (rs.next()) {
                puzzleList.add(new PicrossPuzzle(rs.getLong("id"), rs.getLong("CreatorId"),
                        gson.fromJson(rs.getString("Solution"), int[][].class), gson.fromJson(rs.getString("Ratings"), hashMapToken)));
            }
            return puzzleList;
        }
    }

    private List<PicrossPuzzle> getAllPuzzles(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            List<PicrossPuzzle> puzzleList = new ArrayList<>();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + PicrossTable);
            Type hashMapToken = new TypeToken<HashMap<Long, Integer>>() {
            }.getType();
            while (rs.next()) {
                puzzleList.add(new PicrossPuzzle(rs.getLong("id"), rs.getLong("CreatorId"),
                        gson.fromJson(rs.getString("Solution"), int[][].class), gson.fromJson(rs.getString("Ratings"), hashMapToken)));
            }
            return puzzleList;
        }
    }

    static class RatingUpdater {
        long id;
        List<ChangeRating> changes;
    }
    static class ChangeRating {
        long raterId;
        int rating;
        String action;
    }
    private HTTPResponse updateRatings(byte[] content, Connection conn, String username, String password) {
        HTTPResponse response = new HTTPResponse();
        try {
            String json = new String(content, StandardCharsets.UTF_8);
            RatingUpdater ratings = gson.fromJson(json, RatingUpdater.class);
            PicrossPuzzle puzzle = getById(ratings.id, conn);

            if (!AuthController.authById(puzzle.getCreatorId(), username, password, conn)) {
                response.set403();
                return response;
            }

            for (ChangeRating change : ratings.changes) {
                switch (change.action.toLowerCase()) {
                    case "add" -> {
                        if (puzzle.getRatings().stream().noneMatch((PicrossPuzzle.Rating r) -> r.getRaterId() == change.raterId)) {
                            puzzle.getRatings().add(new PicrossPuzzle.Rating(change.raterId, change.rating));
                        }
                    }
                    case "remove" -> puzzle.getRatings().removeIf((PicrossPuzzle.Rating r) -> r.getRaterId() == change.raterId);
                }
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(String.format("UPDATE %s SET Ratings='%s' WHERE Id=%d",
                        PicrossTable, gson.toJson(puzzle.getRatings()), ratings.id));
                response.set204();
                return response;
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            response.set500();
            return response;
        } catch (NotFoundException e) {
            response.set404();
            return response;
        }
    }

    static class SolutionUpdater {
        long id;
        int[][] solution;
    }
    private HTTPResponse updateSolution(byte[] content, Connection conn, String username, String password) {
        HTTPResponse response = new HTTPResponse();
        try {
            String json = new String(content, StandardCharsets.UTF_8);
            SolutionUpdater solution = gson.fromJson(json, SolutionUpdater.class);

            PicrossPuzzle puzzle = getById(solution.id, conn);
            if (!AuthController.authById(puzzle.getCreatorId(), username, password, conn)) {
                response.set403();
                return response;
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(String.format("UPDATE %s SET Solution='%s' WHERE Id=%d",
                        PicrossTable, gson.toJson(solution.solution), solution.id));
                response.set204();
                return response;
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            response.set500();
            return response;
        } catch (NotFoundException e) {
            response.set404();
            return response;
        }
    }
}
