package com.lujsom.booknest.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lujsom.booknest.BookDetails;
import com.lujsom.booknest.R;
import com.lujsom.booknest.models.Book;
import java.util.List;

/**
 * ReadingListAdapter is a RecyclerView adapter for displaying a list of books in the user's reading list.
 * It allows users to view book details, start reading, or remove books from the list.
 */
public class ReadingListAdapter extends RecyclerView.Adapter<ReadingListAdapter.ViewHolder> {

    private final Context context;
    private List<Book> bookList;

    /**
     * Constructor: Initializes the adapter with the application context and a list of books.
     *
     * @param context  The application or activity context.
     * @param bookList A list of books in the reading list.
     */
    public ReadingListAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    /**
     * Updates the list of books in the reading list and refreshes the UI.
     *
     * @param newList The updated list of books.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<Book> newList) {
        this.bookList = newList;
        notifyDataSetChanged();
    }

    /**
     * Creates a new ViewHolder by inflating the item layout.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The view type (not used in this case).
     * @return A new ViewHolder instance.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reading_list, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds data to a ViewHolder at a given position.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = bookList.get(position);

        // Set book title, fallback to "Unknown Title" if null
        holder.bookTitle.setText(book.getTitle());

        // Load book image using Glide with placeholder and error handling
        Glide.with(context)
                .load(book.getImageUrl())
                .placeholder(R.drawable.magazine)
                .error(R.drawable.magazine)
                .into(holder.bookImage);

        // Set click listeners for viewing details, removing from the list, and starting to read
        holder.itemView.setOnClickListener(v -> openBookDetails(book));
        holder.removeButton.setOnClickListener(v -> removeBookFromReadingList(position, book.getBookId()));
        holder.startReadingButton.setOnClickListener(v -> startReading(book));
    }

    /**
     * Opens the BookDetails activity with the selected book's data.
     *
     * @param book The selected book object.
     */
    private void openBookDetails(Book book) {
        Intent intent = new Intent(context, BookDetails.class);
        intent.putExtra("book_id", book.getBookId());
        intent.putExtra("book_title", book.getTitle());
        context.startActivity(intent);
    }

    /**
     * Removes a book from the user's reading list in Firestore and updates the UI.
     *
     * @param position The position of the book in the list.
     * @param bookId   The unique identifier of the book.
     */
    private void removeBookFromReadingList(int position, String bookId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        db.collection("users").document(userId).collection("reading_list").document(bookId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    bookList.remove(position);
                    notifyItemRemoved(position);
                });
    }

    /**
     * Starts reading the book by opening the reading URL or Google Books page.
     *
     * @param book The book to start reading.
     */
    private void startReading(Book book) {
        if (book.getReadingUrl() != null && !book.getReadingUrl().isEmpty()) {
            // Open book's reading URL if available
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(book.getReadingUrl()));
            context.startActivity(intent);
        } else if (book.getBookId() != null && !book.getBookId().trim().isEmpty()) {
            // Open Google Books page if no direct reading URL is provided
            String googleBooksUrl = "https://books.google.com/books?id=" + book.getBookId();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(googleBooksUrl));
            context.startActivity(intent);
        } else {
            // Show toast if no reading options are available
            Toast.makeText(context, "No reading options available", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Returns the total number of books in the list.
     *
     * @return The size of the bookList.
     */
    @Override
    public int getItemCount() {
        return bookList.size();
    }

    /**
     * ViewHolder class representing each item in the RecyclerView.
     * Contains TextView, ImageView, and Button elements that represent each book in the reading list.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView bookTitle; // Book title text
        ImageView bookImage; // Book cover image
        Button removeButton, startReadingButton; // Buttons for removing and reading the book

        /**
         * Constructor for ViewHolder.
         *
         * @param itemView The view representing a single book item.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bookTitle = itemView.findViewById(R.id.book_title);
            bookImage = itemView.findViewById(R.id.book_image);
            removeButton = itemView.findViewById(R.id.remove_button);
            startReadingButton = itemView.findViewById(R.id.start_reading_button);
        }
    }
}