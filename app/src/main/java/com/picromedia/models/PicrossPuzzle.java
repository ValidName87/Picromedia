package com.picromedia.models;

import java.util.ArrayList;
import java.util.List;

public class PicrossPuzzle {
    @PrimaryKey
    public long id;
    @ForeignKey(table = "User", column = "Id")
    public long creatorId;
    public int[][] horizontalClues;
    public int[][] verticalClues;
    public List<Integer> ratings;

    public PicrossPuzzle(long creatorId, int[][] horizontalClues, int[][] verticalClues) {
        this.creatorId = creatorId;
        this.horizontalClues = horizontalClues;
        this.verticalClues = verticalClues;
        ratings = new ArrayList<>();
    }
}
