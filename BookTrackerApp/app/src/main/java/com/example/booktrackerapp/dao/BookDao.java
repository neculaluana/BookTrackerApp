package com.example.booktrackerapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Dao;

import com.example.booktrackerapp.model.Book;

import java.util.List;

@Dao
public interface BookDao {

    @Insert
    void insert(Book book);

    @Update
    void update(Book book);

    @Delete
    void delete(Book book);

    @Query("DELETE FROM book_table")
    void deleteAllBooks();

    @Query("SELECT * FROM book_table ORDER BY title ASC")
    LiveData<List<Book>> getAllBooks();

    @Query("SELECT * FROM book_table WHERE id = :bookId")
    LiveData<Book> getBookById(int bookId);
}