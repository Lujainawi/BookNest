package com.lujsom.booknest.adapters;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.lujsom.booknest.BookDetails;
import com.lujsom.booknest.R;
import com.lujsom.booknest.models.Book;
import java.util.List;

/**
 * BookAdapter is a RecyclerView adapter responsible for displaying a list of books.
 * Each book is represented with an image, title, and a details button.
 */
public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private final Context context;
    private final List<Book> bookList;

    /**
     * Constructor for the BookAdapter.
     *
     * @param context The application or activity context.
     * @param bookList A list of Book objects to display.
     */
    public BookAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    /**
     * Creates a new ViewHolder by inflating the item layout.
     *
     * @param parent The parent ViewGroup.
     * @param viewType The view type (not used in this case).
     * @return A new BookViewHolder instance.
     */
    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout (item_book.xml) and create a new ViewHolder
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    /**
     * Binds data to a ViewHolder at a given position.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);

        // Set book title, fallback to "Unknown Title" if null
        holder.bookTitle.setText(book.getTitle());

        // Load book image using Glide, with error and placeholder handling
        Glide.with(context)
                .load(book.getImageUrl() == null || book.getImageUrl().isEmpty() ? R.drawable.magazine : book.getImageUrl())
                .placeholder(R.drawable.loading) // Placeholder while loading image
                .error(R.drawable.magazine) // Default image if loading fails
                .centerCrop()
                .into(holder.bookImage);

        // Set click listeners to open book details
        holder.itemView.setOnClickListener(v -> openBookDetails(book));
        holder.detailsButton.setOnClickListener(v -> openBookDetails(book));
    }

    /**
     * Opens the BookDetails activity with the selected book's data.
     *
     * @param book The selected book object.
     */
    private void openBookDetails(Book book) {
        if (book == null) {
            Log.e(TAG, "Book is null");
            return;
        }
        Intent intent = new Intent(context, BookDetails.class);
        Bundle bundle = new Bundle();

        // Add book details to the intent
        bundle.putString("book_id", book.getBookId());
        bundle.putString("book_title", book.getTitle());
        bundle.putString("book_image", book.getImageUrl() != null && !book.getImageUrl().trim().isEmpty()
                ? book.getImageUrl()
                : null);
        bundle.putString("book_description", book.getDescription());
        bundle.putString("book_author", book.getAuthor());
        bundle.putString("book_genre", book.getGenre());
        bundle.putString("book_rating", book.getRating());
        bundle.putString("book_published_year", book.getPublishedYear());
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    /**
     * Returns the total number of books in the list.
     *
     * @return The size of the book list.
     */
    @Override
    public int getItemCount() {
        // Return the total number of books in the list
        return bookList != null ? bookList.size() : 0;
    }


    /**
     * ViewHolder class representing each item in the RecyclerView.
     */
    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView bookImage; // Image of the book
        TextView bookTitle; // Book title text
        Button detailsButton; // Button to view more details about the book

        /**
         * Constructor for BookViewHolder.
         *
         * @param itemView The view representing a single book item.
         */
        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize UI components
            bookImage = itemView.findViewById(R.id.book_image);
            bookTitle = itemView.findViewById(R.id.book_title);
            detailsButton = itemView.findViewById(R.id.details_button);
        }
    }
}