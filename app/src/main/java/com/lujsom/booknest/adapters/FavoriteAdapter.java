package com.lujsom.booknest.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.lujsom.booknest.R;
import com.lujsom.booknest.models.Book;
import com.lujsom.booknest.utils.FirestoreHelper;
import java.util.List;

/**
 * FavoriteAdapter is a RecyclerView adapter for displaying a list of favorite books.
 * It allows users to view and remove books from their favorites.
 */
public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {
    private final Context context;
    private List<Book> favoriteBooks;
    private final FirestoreHelper firestoreHelper;

    /**
     * Constructor: Initializes the adapter with the application context and a list of favorite books.
     * It also creates an instance of FirestoreHelper to handle database operations.
     *
     * @param context       The application or activity context.
     * @param favoriteBooks A list of books marked as favorites.
     */
    public FavoriteAdapter(Context context, List<Book> favoriteBooks) {
        this.context = context;
        this.favoriteBooks = favoriteBooks;
        this.firestoreHelper = new FirestoreHelper();
    }

    /**
     * Inflates the layout for each item in the RecyclerView and returns a new ViewHolder instance.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The view type (not used in this case).
     * @return A new ViewHolder instance.
     */
    @NonNull
    @Override
    //Inflates the layout for each item in the RecyclerView and returns a new ViewHolder instance.
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_book, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Populates the view components (title and image) with book details.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    @SuppressLint("NotifyDataSetChanged")
    @Override
    //Populates the view components (title and image) with book details.
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = favoriteBooks.get(position);

        // Set book title, fallback to "Unknown Title" if null
        holder.bookTitle.setText(book.getTitle());

        // Load book image using Glide with placeholder and error handling
        Glide.with(context)
                .load(book.getImageUrl())
                .placeholder(R.drawable.magazine)
                .error(R.drawable.magazine)
                .into(holder.bookImage);

        // Remove book from favorites when the remove button is clicked
        holder.buttonRemove.setOnClickListener(v -> removeBookFromFavorites(position, book.getBookId()));
    }

    /**
     * Removes a book from the user's favorite list in Firestore and updates the UI.
     *
     * @param position The position of the book in the list.
     * @param bookId   The unique identifier of the book.
     */
    private void removeBookFromFavorites(int position, String bookId) {
        firestoreHelper.removeBookFromFavorites(bookId, success -> {
            if (success) {
                favoriteBooks.remove(position);
                notifyItemRemoved(position);
            }
        });
    }

    /**
     * Returns the total number of books in the list.
     *
     * @return The size of the favoriteBooks list.
     */
    @Override
    public int getItemCount() {
        return favoriteBooks.size();
    }

    /**
     * ViewHolder class representing each item in the RecyclerView.
     * Contains ImageView, TextView, and Button elements that represent each book in the favorite list.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView bookImage;
        TextView bookTitle;
        Button  buttonRemove;

        /**
         * Constructor for ViewHolder.
         *
         * @param itemView The view representing a single book item.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bookImage = itemView.findViewById(R.id.book_image);
            bookTitle = itemView.findViewById(R.id.book_title);
            buttonRemove = itemView.findViewById(R.id.button_remove);
        }
    }

    /**
     * Updates the list of favorite books and refreshes the UI.
     *
     * @param newList The updated list of favorite books.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<Book> newList) {
        this.favoriteBooks = newList;
        notifyDataSetChanged();
    }
}