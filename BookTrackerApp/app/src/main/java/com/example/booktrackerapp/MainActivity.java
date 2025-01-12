package com.example.booktrackerapp;

import android.content.Intent;
import android.os.Bundle;

import com.example.booktrackerapp.adapter.BookAdapter;
import com.example.booktrackerapp.model.Book;
import com.example.booktrackerapp.viewmodel.BookViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int ADD_BOOK_REQUEST = 1;
    public static final int EDIT_BOOK_REQUEST = 2;

    private BookViewModel bookViewModel;
    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private FloatingActionButton buttonAddBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        recyclerView = findViewById(R.id.recycler_view_books);
        buttonAddBook = findViewById(R.id.button_add_book);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new BookAdapter();
        recyclerView.setAdapter(adapter);

        // Initialize ViewModel
        bookViewModel = new ViewModelProvider(this).get(BookViewModel.class);

        // Observe LiveData
        bookViewModel.getAllBooks().observe(this, new Observer<List<Book>>() {
            @Override
            public void onChanged(List<Book> books) {
                // Update RecyclerView
                adapter.setBooks(books);
            }
        });

        buttonAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddEditBookActivity.class);
                startActivityForResult(intent, ADD_BOOK_REQUEST);
            }
        });

        // Handle RecyclerView item clicks
        adapter.setOnItemClickListener(new BookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Book book) {
                Intent intent = new Intent(MainActivity.this, BookDetailsActivity.class);
                intent.putExtra("book_id", book.getId());
                startActivity(intent);
            }
        });

    }

    // Handle result from AddEditBookActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // Get data from intent
            String title = data.getStringExtra("title");
            String author = data.getStringExtra("author");
            int pagesRead = data.getIntExtra("pages_read", 0);
            int totalPages = data.getIntExtra("total_pages", 0);
            String coverImagePath = data.getStringExtra("cover_image_path");

            if (requestCode == ADD_BOOK_REQUEST) {
                // Adding a new book
                Book book = new Book(title, author, pagesRead, totalPages, coverImagePath);
                bookViewModel.insert(book);
                Toast.makeText(this, "Book saved", Toast.LENGTH_SHORT).show();
            } else if (requestCode == EDIT_BOOK_REQUEST) {
                // Updating an existing book
                int id = data.getIntExtra("book_id", -1);
                if (id != -1) {
                    Book book = new Book(title, author, pagesRead, totalPages, coverImagePath);
                    book.setId(id);
                    bookViewModel.update(book);
                    Toast.makeText(this, "Book updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Book not updated", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Book not saved", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }




}