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

import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.DialogInterface;
import android.widget.Toast;
import android.app.AlertDialog;
import android.widget.EditText;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.io.File;
import java.util.List;

public class BookDetailsActivity extends AppCompatActivity {

    public static final int EDIT_BOOK_REQUEST = 2;
    private ImageView imageViewCover;
    private TextView textViewTitle, textViewAuthor, textViewProgress;
    private ProgressBar progressBar;
    private Button buttonEditBook;
    private Button buttonUpdateProgress;
    private Button buttonDeleteBook;

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
        buttonUpdateProgress = findViewById(R.id.button_update_progress);
        buttonDeleteBook = findViewById(R.id.button_delete_book);

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

        // Update Progress
        buttonUpdateProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentBook != null) {
                    showUpdateProgressDialog();
                }
            }
        });

// Delete Book
        buttonDeleteBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentBook != null) {
                    showDeleteConfirmationDialog();
                }
            }
        });
    }
    private void showUpdateProgressDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Update Progress");

        // Set up the input layout
        TextInputLayout inputLayout = new TextInputLayout(this);
        inputLayout.setHint("Enter pages read");

        // Set up the input
        final TextInputEditText input = new TextInputEditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(currentBook.getPagesRead()));

        // Add the input to the input layout
        inputLayout.addView(input);

        // Add the input layout to the dialog
        builder.setView(inputLayout);

        // Set up the buttons
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputText = input.getText().toString().trim();
                if (!inputText.isEmpty()) {
                    int pagesRead = Integer.parseInt(inputText);
                    if (pagesRead >= 0 && pagesRead <= currentBook.getTotalPages()) {
                        currentBook.setPagesRead(pagesRead);
                        bookViewModel.update(currentBook);
                        Toast.makeText(BookDetailsActivity.this, "Progress updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(BookDetailsActivity.this, "Invalid number of pages", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BookDetailsActivity.this, "Please enter pages read", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Delete Book");
        builder.setMessage("Are you sure you want to delete this book?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                bookViewModel.delete(currentBook);
                Toast.makeText(BookDetailsActivity.this, "Book deleted", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}