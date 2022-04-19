package com.picromedia.models;

public class Message {
    public long id;
    public long posterId;
    public long puzzleId;
    @SqlType("MEDIUMTEXT")
    public String text;

    public Message(long posterId, long puzzleId, String text) {
        this.posterId = posterId;
        this.puzzleId = puzzleId;
        this.text = text;
    }
}
