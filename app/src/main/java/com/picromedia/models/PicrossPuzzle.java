package com.picromedia.models;

import java.util.HashMap;

public class PicrossPuzzle {
    private long id;
    private long creatorId;
    private int[][] solution;
    // Key = Id of whoever rated it
    // Value = Rating
    private final HashMap<Long, Integer> ratings;

    public PicrossPuzzle(long creatorId, int[][] solution) {
        this.creatorId = creatorId;
        this.solution = solution;
        ratings = new HashMap<>();
    }

    public long getId() {
        return id;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public HashMap<Long, Integer> getRatings() {
        return ratings;
    }

    public void addRating(long raterId, int rating) {
        ratings.put(raterId, rating);
    }

    public int[][] getSolution() {
        return solution;
    }

    public void setSolution(int[][] solution) {
        this.solution = solution;
    }
}
