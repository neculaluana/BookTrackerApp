package com.example.booktrackerapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "book_table")
public class Book {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String author;

    @ColumnInfo(name = "pages_read")
    private int pagesRead;

    @ColumnInfo(name = "total_pages")
    private int totalPages;

    @ColumnInfo(name = "cover_image_path")
    private String coverImagePath;

    // Constructor
    public Book(String title, String author, int pagesRead, int totalPages, String coverImagePath) {
        this.title = title;
        this.author = author;
        this.pagesRead = pagesRead;
        this.totalPages = totalPages;
        this.coverImagePath = coverImagePath;
    }

    // Getters and Setters

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getPagesRead() {
        return pagesRead;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPagesRead(int pagesRead) {
        this.pagesRead = pagesRead;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

}