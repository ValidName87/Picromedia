package com.picromedia.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PicrossPuzzle {
    private long id;
    private long creatorId;
    private int[][] horizontalClues;
    private int[][] verticalClues;
    // Key = Id of whoever rated it
    // Value = Rating
    private final HashMap<Long, Integer> ratings;

    public PicrossPuzzle(long creatorId, int[][] horizontalClues, int[][] verticalClues) {
        this.creatorId = creatorId;
        this.horizontalClues = horizontalClues;
        this.verticalClues = verticalClues;
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

    public int[][] getHorizontalClues() {
        return horizontalClues;
    }

    public void setHorizontalClues(int[][] horizontalClues) {
        this.horizontalClues = horizontalClues;
    }

    public int[][] getVerticalClues() {
        return verticalClues;
    }

    public void setVerticalClues(int[][] verticalClues) {
        this.verticalClues = verticalClues;
    }

    public HashMap<Long, Integer> getRatings() {
        return ratings;
    }

    public void addRating(long raterId, int rating) {
        ratings.put(raterId, rating);
    }
}
