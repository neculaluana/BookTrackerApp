package com.example.booktrackerapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.booktrackerapp.model.Book;
import com.example.booktrackerapp.viewmodel.BookViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class BookDetailsActivity extends AppCompatActivity {

    public static final int EDIT_BOOK_REQUEST = 2;
    private ImageView imageViewCover;
    private TextView textViewTitle, textViewAuthor, textViewProgress;
    private ProgressBar progressBar;
    private Button buttonEditBook;

    private BookViewModel bookViewModel;
    private int bookId;

    private Book currentBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        // Initialize views
        imageViewCover = findViewById(R.id.image_view_detail_cover);
        textViewTitle = findViewById(R.id.text_view_detail_title);
        textViewAuthor = findViewById(R.id.text_view_detail_author);
        textViewProgress = findViewById(R.id.text_view_detail_progress);
        progressBar = findViewById(R.id.progress_bar_detail);
        buttonEditBook = findViewById(R.id.button_edit_book);

        // Get book ID from intent
        Intent intent = getIntent();
        bookId = intent.getIntExtra("book_id", -1);

        // Initialize ViewModel
        bookViewModel = new ViewModelProvider(this).get(BookViewModel.class);

        bookViewModel.getBookById(bookId).observe(this, new Observer<Book>() {
            @Override
            public void onChanged(Book book) {
                if (book != null) {
                    // Display book details
                    textViewTitle.setText(book.getTitle());
                    textViewAuthor.setText(book.getAuthor());
                    textViewProgress.setText("Pages read: " + book.getPagesRead() + "/" + book.getTotalPages());

                    int progress = (int) ((double) book.getPagesRead() / book.getTotalPages() * 100);
                    progressBar.setProgress(progress);

                    // Load image
                    String imagePath = book.getCoverImagePath();
                    if (imagePath != null && !imagePath.isEmpty()) {
                        Glide.with(BookDetailsActivity.this)
                                .load(new File(imagePath))
                                .placeholder(R.drawable.ic_book_placeholder)
                                .into(imageViewCover);
                    } else {
                        imageViewCover.setImageResource(R.drawable.ic_book_placeholder);
                    }

                    // Save the current book for editing
                    currentBook = book;
                }
            }
        });

        // Edit book
        buttonEditBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentBook != null) {
                    Intent editIntent = new Intent(BookDetailsActivity.this, AddEditBookActivity.class);
                    editIntent.putExtra("book_id", currentBook.getId());
                    editIntent.putExtra("title", currentBook.getTitle());
                    editIntent.putExtra("author", currentBook.getAuthor());
                    editIntent.putExtra("pages_read", currentBook.getPagesRead());
                    editIntent.putExtra("total_pages", currentBook.getTotalPages());
                    editIntent.putExtra("cover_image_path", currentBook.getCoverImagePath());
                    startActivityForResult(editIntent, EDIT_BOOK_REQUEST);
                }
            }
        });
    }
}