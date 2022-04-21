package com.picromedia.models;

public class Message {
    private long id;
    private long posterId;
    private long puzzleId;
    private String text;

    public Message(long posterId, long puzzleId, String text) {
        this.posterId = posterId;
        this.puzzleId = puzzleId;
        this.text = text;
    }

    public long getPosterId() {
        return posterId;
    }

    public void setPosterId(long posterId) {
        this.posterId = posterId;
    }

    public long getPuzzleId() {
        return puzzleId;
    }

    public void setPuzzleId(long puzzleId) {
        this.puzzleId = puzzleId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getId() {
        return id;
    }
}
