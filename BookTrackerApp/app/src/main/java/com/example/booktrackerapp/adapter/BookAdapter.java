package com.example.booktrackerapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.example.booktrackerapp.R;
import com.example.booktrackerapp.model.Book;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookHolder> {

    private List<Book> books = new ArrayList<>();
    private OnItemClickListener listener;

    // ViewHolder class
    class BookHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewCover;
        private TextView textViewTitle;
        private TextView textViewAuthor;
        private ProgressBar progressBar;

        public BookHolder(View itemView) {
            super(itemView);

            imageViewCover = itemView.findViewById(R.id.image_view_book_cover);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewAuthor = itemView.findViewById(R.id.text_view_author);
            progressBar = itemView.findViewById(R.id.progress_bar);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(books.get(position));
                    }
                }
            });
        }
    }

    // Adapter methods
    @NonNull
    @Override
    public BookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate item layout
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BookHolder holder, int position) {
        Book currentBook = books.get(position);
        // Bind data to views
        holder.textViewTitle.setText(currentBook.getTitle());
        holder.textViewAuthor.setText(currentBook.getAuthor());

        int progress = (int) ((double) currentBook.getPagesRead() / currentBook.getTotalPages() * 100);
        holder.progressBar.setProgress(progress);

        // Load image using Glide
        String imageUri = currentBook.getCoverImageUri();
        if (imageUri != null) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUri)
                    .placeholder(R.drawable.ic_book_placeholder)
                    .into(holder.imageViewCover);
        } else {
            holder.imageViewCover.setImageResource(R.drawable.ic_book_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public void setBooks(List<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Book book);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}