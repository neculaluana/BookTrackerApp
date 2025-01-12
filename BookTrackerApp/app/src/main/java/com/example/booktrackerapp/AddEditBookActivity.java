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
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AddEditBookActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTextTitle, editTextAuthor, editTextPagesRead, editTextTotalPages;
    private ImageView imageViewCover;
    private Button buttonSaveBook;

    private String coverImagePath;
    private int bookId = -1;

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

        Intent intent = getIntent();
        if (intent.hasExtra("book_id")) {
            setTitle("Edit Book");

            bookId = intent.getIntExtra("book_id", -1);
            editTextTitle.setText(intent.getStringExtra("title"));
            editTextAuthor.setText(intent.getStringExtra("author"));
            editTextPagesRead.setText(String.valueOf(intent.getIntExtra("pages_read", 0)));
            editTextTotalPages.setText(String.valueOf(intent.getIntExtra("total_pages", 0)));
            coverImagePath = intent.getStringExtra("cover_image_path");
            if (coverImagePath != null && !coverImagePath.isEmpty()) {
                Glide.with(this)
                        .load(new File(coverImagePath))
                        .placeholder(R.drawable.ic_book_placeholder)
                        .into(imageViewCover);
            } else {
                imageViewCover.setImageResource(R.drawable.ic_book_placeholder);
            }
        } else {
            setTitle("Add Book");
        }
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

            Uri imageUri = data.getData();
            try {
                // Save the image to internal storage
                String imagePath = saveImageToInternalStorage(imageUri);
                // Set the image path to the coverImagePath variable
                coverImagePath = imagePath;

                // Load the image into the ImageView
                Glide.with(this)
                        .load(new File(imagePath))
                        .placeholder(R.drawable.ic_book_placeholder)
                        .into(imageViewCover);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private String saveImageToInternalStorage(Uri imageUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            throw new IOException("Unable to open input stream from URI");
        }

        // Create an images directory in internal storage
        File imagesDir = new File(getFilesDir(), "images");
        if (!imagesDir.exists()) {
            imagesDir.mkdir();
        }

        // Create a unique file name
        String fileName = "book_" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(imagesDir, fileName);

        OutputStream outputStream = new FileOutputStream(imageFile);

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();
        inputStream.close();

        return imageFile.getAbsolutePath();
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
        data.putExtra("cover_image_path", coverImagePath);

        if (bookId != -1) {
            data.putExtra("book_id", bookId);
        }
        setResult(RESULT_OK, data);
        finish();
    }
}