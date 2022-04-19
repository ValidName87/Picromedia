package com.picromedia.models;

public class Message {
    @PrimaryKey
    public long id;
    @ForeignKey(table = "User", column = "Id")
    public long posterId;
    @ForeignKey(table = "PicrossPuzzle", column = "Id")
    public long puzzleId;
    @SqlType("MEDIUMTEXT")
    public String text;

    public Message(long posterId, long puzzleId, String text) {
        this.posterId = posterId;
        this.puzzleId = puzzleId;
        this.text = text;
    }
}
