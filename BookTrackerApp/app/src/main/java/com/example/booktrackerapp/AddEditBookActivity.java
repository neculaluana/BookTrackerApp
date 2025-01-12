package com.example.booktrackerapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class AddEditBookActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTextTitle, editTextAuthor, editTextPagesRead, editTextTotalPages;
    private ImageView imageViewCover;
    private Button buttonSaveBook;

    private Uri coverImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_book);

        // Initialize views
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextAuthor = findViewById(R.id.edit_text_author);
        editTextPagesRead = findViewById(R.id.edit_text_pages_read);
        editTextTotalPages = findViewById(R.id.edit_text_total_pages);
        imageViewCover = findViewById(R.id.image_view_cover);
        buttonSaveBook = findViewById(R.id.button_save_book);

        // Image selection
        imageViewCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageChooser();
            }
        });

        // Save book
        buttonSaveBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBook();
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Handle image selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if image was selected
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            coverImageUri = data.getData();
            Glide.with(this).load(coverImageUri).into(imageViewCover);
        }
    }

    private void saveBook() {
        String title = editTextTitle.getText().toString().trim();
        String author = editTextAuthor.getText().toString().trim();
        String pagesReadStr = editTextPagesRead.getText().toString().trim();
        String totalPagesStr = editTextTotalPages.getText().toString().trim();

        // Input validation
        if (title.isEmpty() || author.isEmpty() || pagesReadStr.isEmpty() || totalPagesStr.isEmpty()) {
            Snackbar.make(findViewById(R.id.linear_layout_add_edit),
                    "Please fill in all fields", Snackbar.LENGTH_LONG).show();
            return;
        }

        int pagesRead = Integer.parseInt(pagesReadStr);
        int totalPages = Integer.parseInt(totalPagesStr);

        if (pagesRead > totalPages) {
            Snackbar.make(findViewById(R.id.linear_layout_add_edit),
                    "Pages read cannot exceed total pages", Snackbar.LENGTH_LONG).show();
            return;
        }

        // Prepare data to send back to MainActivity
        Intent data = new Intent();
        data.putExtra("title", title);
        data.putExtra("author", author);
        data.putExtra("pages_read", pagesRead);
        data.putExtra("total_pages", totalPages);
        data.putExtra("cover_image_uri", coverImageUri != null ? coverImageUri.toString() : null);

        setResult(RESULT_OK, data);
        finish();
    }
}