package com.example.booktrackerapp;
import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.example.booktrackerapp.adapter.BookAdapter;
import com.example.booktrackerapp.model.Book;
import com.example.booktrackerapp.model.BookResult;
import com.example.booktrackerapp.model.OpenLibraryResponse;
import com.example.booktrackerapp.viewmodel.BookViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.booktrackerapp.model.Quote;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity {

    public static final int ADD_BOOK_REQUEST = 1;

    private BookViewModel bookViewModel;
    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private FloatingActionButton buttonAddBook;
    private Button buttonTestNotification;
    private TextView textViewQuote;
    private EditText editTextSearch;
    private Button buttonSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        recyclerView = findViewById(R.id.recycler_view_books);
        buttonAddBook = findViewById(R.id.button_add_book);
        textViewQuote = findViewById(R.id.text_view_quote);

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

        buttonTestNotification = findViewById(R.id.button_test_notification);
        buttonTestNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTestNotification();
            }
        });

        editTextSearch = findViewById(R.id.edit_text_search);
        buttonSearch = findViewById(R.id.button_search);

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = editTextSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    searchBooks(query);
                }
            }
        });

        // Fetch and display the quote
        fetchQuote();

        // Check and request notification permission
        requestNotificationPermission();

        // Check and request permission for exact alarms
        checkAlarmPermission();

        // Create Notification Channel
        createNotificationChannel();

        // Schedule Daily Reminder
        scheduleDailyReminder();

    }
    private void sendTestNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notifyRead")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Test Notification")
                .setContentText("This is a test notification.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        try {
            notificationManager.notify(100, builder.build());
        } catch (SecurityException e) {
            Toast.makeText(this, "Notification permission not granted.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to check and request alarm permission for API 31+
    private void checkAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("To receive daily reading reminders, please allow exact alarms.")
                        .setPositiveButton("Allow", (dialog, which) -> {
                            requestExactAlarmPermission();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            Toast.makeText(this, "Daily reminders are disabled.", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            } else {
                // Permission is granted, schedule the alarm
                scheduleDailyReminder();
            }
        } else {
            // For devices below API 31
            scheduleDailyReminder();
        }
    }


    private void scheduleDailyReminder() {
        Intent intent = new Intent(this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Set the time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 10);
        calendar.set(Calendar.SECOND, 0);
        Log.d("AlarmTest", "Alarm set for: " + calendar.getTime());
        // If the time is in the past, add one day
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (alarmManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    } else {
                        // Prompt the user to grant permission
                        requestExactAlarmPermission();
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            } catch (SecurityException e) {
                // Handle the exception (e.g., show a message to the user)
                Toast.makeText(this, "Cannot schedule exact alarms without permission.", Toast.LENGTH_SHORT).show();
                // Optionally, request permission
                requestExactAlarmPermission();
            }
        }
    }
    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
        }
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ReadingReminderChannel";
            String description = "Channel for Reading Reminder";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notifyRead", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void fetchQuote() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient();

            String apiKey = BuildConfig.API_KEY;
            String url = "https://api.api-ninjas.com/v1/quotes";

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("X-Api-Key", apiKey)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();

                    Gson gson = new Gson();
                    List<Quote> quotes = gson.fromJson(responseBody, new TypeToken<List<Quote>>(){}.getType());

                    if (quotes != null && !quotes.isEmpty()) {
                        Quote quote = quotes.get(0);
                        // Update the UI on the main thread
                        runOnUiThread(() -> displayQuote(quote));
                    }
                } else {
                    Log.e("MainActivity", "Server returned error: " + response.code());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void displayQuote(Quote quote) {
        String quoteText = "\"" + quote.getQuote() + "\"\n\n- " + quote.getAuthor();
        textViewQuote.setText(quoteText);
    }

    private void searchBooks(String query) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            String url = "https://openlibrary.org/search.json?q=" + Uri.encode(query);

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();

                    // Parse JSON response
                    Gson gson = new Gson();
                    Type responseType = new TypeToken<OpenLibraryResponse>() {}.getType();
                    OpenLibraryResponse searchResponse = gson.fromJson(responseBody, responseType);

                    // Update UI on the main thread
                    runOnUiThread(() -> displaySearchResults(searchResponse.docs));
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Search failed: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void displaySearchResults(List<BookResult> bookResults) {
        if (bookResults == null || bookResults.isEmpty()) {
            Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search Results");

        List<String> bookTitles = new ArrayList<>();
        for (BookResult book : bookResults) {
            String authors = book.author_name != null && !book.author_name.isEmpty() ? " by " + book.author_name.get(0) : "";
            bookTitles.add(book.title + authors);
        }

        builder.setItems(bookTitles.toArray(new String[0]), (dialog, which) -> {
            BookResult selectedBook = bookResults.get(which);
            openAddEditBookWithPreFilledData(selectedBook);
        });

        builder.show();
    }

    private void openAddEditBookWithPreFilledData(BookResult book) {
        Intent intent = new Intent(MainActivity.this, AddEditBookActivity.class);
        intent.putExtra("search_title", book.title);

        if (book.author_name != null && !book.author_name.isEmpty()) {
            intent.putExtra("search_author", book.author_name.get(0));
        }

        if (book.cover_i != null) {
            intent.putExtra("search_cover_id", book.cover_i);
        }

        startActivity(intent);
    }

    // Handle result from AddEditBookActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
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
            }
        } else {
            Toast.makeText(this, "Book not saved", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        editTextSearch.setText("");
        editTextSearch.clearFocus();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
                // Permission granted, schedule the alarm
                scheduleDailyReminder();
            } else {
                // Permission not granted, prompt the user
                requestExactAlarmPermission();
            }
        } else {
            // For devices below API 31
            scheduleDailyReminder();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}