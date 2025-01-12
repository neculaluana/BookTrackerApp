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

            }
        });

        // Handle RecyclerView item clicks
        adapter.setOnItemClickListener(new BookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Book book) {

            }
        });


    }


}