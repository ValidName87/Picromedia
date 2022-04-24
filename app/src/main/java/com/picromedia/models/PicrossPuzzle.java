package com.picromedia.models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PicrossPuzzle {
    private long id;
    private long creatorId;
    private int[][] solution;
    // Key = Id of whoever rated it
    // Value = Rating
    private final List<Rating> ratings;

    private Instant lastEdited;

    public PicrossPuzzle(long creatorId, int[][] solution) {
        this.creatorId = creatorId;
        this.solution = solution;
        ratings = new ArrayList<>();
    }

    public PicrossPuzzle(long id, long creatorId, int[][] solution, List<Rating> ratings) {
        this.id = id;
        this.creatorId = creatorId;
        this.solution = solution;
        this.ratings = ratings;
        this.lastEdited = Instant.now();
    }

    public PicrossPuzzle(long id, long creatorId, int[][] solution, List<Rating> ratings, Instant lastEdited) {
        this.id = id;
        this.creatorId = creatorId;
        this.solution = solution;
        this.ratings = ratings;
        this.lastEdited = lastEdited;
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

    public List<Rating> getRatings() {
        return ratings;
    }

    public void addRating(Rating rating) {
        ratings.add(rating);
    }

    public int[][] getSolution() {
        return solution;
    }

    public void setSolution(int[][] solution) {
        this.solution = solution;
    }

    public Instant getLastEdited() {
        return lastEdited;
    }

    public void setLastEdited(Instant lastEdited) {
        this.lastEdited = lastEdited;
    }

    public static class Rating {
        long raterId;
        int rating;

        public Rating(long raterId, int rating) {
            this.raterId = raterId;
            this.rating = rating;
        }

        public long getRaterId() {
            return raterId;
        }

        public int getRating() {
            return rating;
        }
    }
}
