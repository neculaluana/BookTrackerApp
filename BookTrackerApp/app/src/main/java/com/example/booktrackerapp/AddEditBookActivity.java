package com.example.booktrackerapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.booktrackerapp.model.Book;
import com.example.booktrackerapp.viewmodel.BookViewModel;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddEditBookActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private BookViewModel bookViewModel;
    private EditText editTextTitle, editTextAuthor, editTextPagesRead, editTextTotalPages;
    private ImageView imageViewCover;
    private Button buttonSaveBook;
    private Button buttonUploadCover;

    private String coverImagePath;
    private int bookId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_book);
        bookViewModel = new ViewModelProvider(this).get(BookViewModel.class);

        // Initialize views
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextAuthor = findViewById(R.id.edit_text_author);
        editTextPagesRead = findViewById(R.id.edit_text_pages_read);
        editTextTotalPages = findViewById(R.id.edit_text_total_pages);
        imageViewCover = findViewById(R.id.image_view_cover);
        buttonSaveBook = findViewById(R.id.button_save_book);
        buttonUploadCover = findViewById(R.id.button_upload_cover);

        // Image selection
        buttonUploadCover.setOnClickListener(new View.OnClickListener() {
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
            String title = intent.getStringExtra("search_title");
            String author = intent.getStringExtra("search_author");
            String coverId = intent.getStringExtra("search_cover_id");

            editTextTitle.setText(title != null ? title : "");
            editTextAuthor.setText(author != null ? author : "");

            if (coverId != null && !coverId.isEmpty()) {
                String coverUrl = "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg";
                Glide.with(this)
                        .load(coverUrl)
                        .placeholder(R.drawable.ic_book_placeholder)
                        .into(imageViewCover);

                // Save URL to coverImagePath or handle accordingly
                coverImagePath = coverUrl;
            } else {
                imageViewCover.setImageResource(R.drawable.ic_book_placeholder);
            }
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


        if (coverImagePath != null && coverImagePath.startsWith("http")) {
            // Download the image asynchronously
            downloadAndSaveImage(coverImagePath, new OnImageDownloadedListener() {
                @Override
                public void onImageDownloaded(String imagePath) {
                    coverImagePath = imagePath; // Update the path to the saved image
                    saveBookToDatabase(title, author, pagesRead, totalPages, coverImagePath);
                }

                @Override
                public void onImageDownloadFailed(Exception e) {
                    Toast.makeText(AddEditBookActivity.this, "Failed to save cover image", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            saveBookToDatabase(title, author, pagesRead, totalPages, coverImagePath);
        }
    }

    private void saveBookToDatabase(String title, String author, int pagesRead, int totalPages, String coverImagePath) {
        if (bookId != -1) {
            // Editing an existing book
            Book book = new Book(title, author, pagesRead, totalPages, coverImagePath);
            book.setId(bookId);
            bookViewModel.update(book);
            Toast.makeText(this, "Book updated", Toast.LENGTH_SHORT).show();
        } else {
            // Adding a new book
            Book book = new Book(title, author, pagesRead, totalPages, coverImagePath);
            bookViewModel.insert(book);
            Toast.makeText(this, "Book saved", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void downloadAndSaveImage(String imageUrl, OnImageDownloadedListener listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // Download the image using OkHttp
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(imageUrl)
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to download image: " + response);
                }

                InputStream inputStream = response.body().byteStream();

                // Save the image to internal storage
                File imagesDir = new File(getFilesDir(), "images");
                if (!imagesDir.exists()) {
                    imagesDir.mkdir();
                }

                String fileName = "book_" + System.currentTimeMillis() + ".jpg";
                File imageFile = new File(imagesDir, fileName);

                OutputStream outputStream = new FileOutputStream(imageFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.close();
                inputStream.close();

                String savedImagePath = imageFile.getAbsolutePath();

                // Return to the main thread to update UI
                handler.post(() -> listener.onImageDownloaded(savedImagePath));

            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> listener.onImageDownloadFailed(e));
            }
        });
    }

    public interface OnImageDownloadedListener {
        void onImageDownloaded(String imagePath);
        void onImageDownloadFailed(Exception e);
    }
}