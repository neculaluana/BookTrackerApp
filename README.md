# BookTrackerApp 📚

An Android app to track your reading progress and manage your book list.

## Features
- **Add and Manage Books**: Track books with details like title, author, pages read, and total pages.
- **Upload Book Covers**: Add cover images to your books.
- **Update Reading Progress**: Update pages read and visualize progress.
- **Daily Reading Reminders**: Receive daily notifications to read, scheduled using the system alarm.
- **Inspirational Quotes**: Display a random quote when you open the app.

---

## Activities and Components

### **Activities**
- **`MainActivity`**: Displays the list of books and an inspirational quote.
- **`AddEditBookActivity`**: Allows adding or editing book details.
- **`BookDetailsActivity`**: Shows detailed information about a selected book.

### **Components Used**
- **RecyclerView**: Display the list of books.
- **MaterialToolbar**: For the app bar.
- **FloatingActionButton**: To add new books.
- **MaterialTextView**: Display text elements.
- **TextInputLayout & TextInputEditText**: Input fields with Material Design.
- **MaterialCardView**: Display books and quotes inside cards.
- **LinearProgressIndicator**: Show reading progress.
- **ImageView**: Display book cover images.
- **AlertDialog & MaterialAlertDialogBuilder**: For dialogs.
- **Notifications**: Using `NotificationCompat` and `NotificationManagerCompat`.
- **AlarmManager**: Schedule daily reminders.
- **Glide Library**: For image loading.

---

## Libraries Used
- **Material Components for Android**: Implement Material Design components.
- **Room Persistence Library**: Local database storage.
- **Glide**: Image loading and caching.
- **OkHttp & Gson**: Making HTTP calls and parsing JSON.

---

## Installation

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/yourusername/BookTrackerApp.git
   ```
2. **Open in Android Studio and sync Gradle**.
3. **Configure API Key**:
    - Obtain an API key from API Ninjas.
    - Add `apiKey=your_api_key` to the `local.properties` file (do not commit this file to version control).
3. **Run the App on a device or emulator**.

## Permissions

- **Notifications**: To send daily reminders.
- **Exact Alarms**: Required on Android 12+ for precise alarm scheduling.
- **Storage Access**: To select and display cover images.
   
