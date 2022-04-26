package com.picromedia.models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PicrossPuzzle {
    private long id;
    private long creatorId;
    private int[] solution;
    // Key = Id of whoever rated it
    // Value = Rating
    private final List<Rating> ratings;

    private Instant lastEdited;

    private String message;
    private String title;

    public PicrossPuzzle(long creatorId, int[] solution) {
        this.creatorId = creatorId;
        this.solution = solution;
        ratings = new ArrayList<>();
    }

    public PicrossPuzzle(long id, long creatorId, int[] solution, List<Rating> ratings, String message, String title) {
        this.id = id;
        this.creatorId = creatorId;
        this.solution = solution;
        this.ratings = ratings;
        this.lastEdited = Instant.now();
        this.message = message;
        this.title = title;
    }

    public PicrossPuzzle(long id, long creatorId, int[] solution, List<Rating> ratings, Instant lastEdited, String message, String title) {
        this.id = id;
        this.creatorId = creatorId;
        this.solution = solution;
        this.ratings = ratings;
        this.lastEdited = lastEdited;
        this.message = message;
        this.title = title;
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

    public int[] getSolution() {
        return solution;
    }

    public void setSolution(int[] solution) {
        this.solution = solution;
    }

    public Instant getLastEdited() {
        return lastEdited;
    }

    public void setLastEdited(Instant lastEdited) {
        this.lastEdited = lastEdited;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
