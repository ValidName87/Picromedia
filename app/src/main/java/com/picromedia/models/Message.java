package com.picromedia.models;

public class Message {
    private long id;
    private long creatorId;
    private long puzzleId;
    private String message;

    public Message(long creatorId, long puzzleId, String message) {
        this.creatorId = creatorId;
        this.puzzleId = puzzleId;
        this.message = message;
    }

    public Message(long id, long creatorId, long puzzleId, String message) {
        this.id = id;
        this.creatorId = creatorId;
        this.puzzleId = puzzleId;
        this.message = message;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long posterId) {
        this.creatorId = posterId;
    }

    public long getPuzzleId() {
        return puzzleId;
    }

    public void setPuzzleId(long puzzleId) {
        this.puzzleId = puzzleId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String text) {
        this.message = text;
    }

    public long getId() {
        return id;
    }
}
