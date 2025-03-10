package com.lujsom.booknest.adapters;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
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
import com.lujsom.booknest.R;
import com.lujsom.booknest.models.Book;
import com.lujsom.booknest.utils.FirestoreHelper;

import java.util.List;

/**
 * ShoppingCartAdapter is a RecyclerView adapter for displaying books in the shopping cart.
 * It allows users to view books, purchase them, or remove them from the cart.
 */
public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.CartViewHolder> {
    private final Context context;
    private List<Book> cartBooks;
    private final FirestoreHelper firestoreHelper;

    /**
     * Constructor: Initializes the adapter with the application context, list of books, and Firestore helper.
     *
     * @param context         The application or activity context.
     * @param cartBooks       A list of books in the shopping cart.
     * @param firestoreHelper An instance of FirestoreHelper for managing cart operations.
     */
    public ShoppingCartAdapter(Context context, List<Book> cartBooks, FirestoreHelper firestoreHelper) {
        this.context = context;
        this.cartBooks = cartBooks;
        this.firestoreHelper = firestoreHelper;
    }

    /**
     * Updates the list of books in the cart and refreshes the UI.
     *
     * @param newList The updated list of books.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<Book> newList) {
        this.cartBooks = newList;
        notifyDataSetChanged();
    }

    /**
     * Creates a new ViewHolder by inflating the item layout.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The view type (not used in this case).
     * @return A new CartViewHolder instance.
     */
    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shopping_cart, parent, false);
        return new CartViewHolder(view);
    }

    /**
     * Binds data to a ViewHolder at a given position.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        if (cartBooks == null || position >= cartBooks.size()) {
            Log.e(TAG, "Invalid cart book list or position");
            return;
        }
        Book book = cartBooks.get(position);

        // Display book title, fallback to "Unknown Title" if null
        holder.bookTitle.setText(book.getTitle());

        // Load book image using Glide with placeholder and error handling
        Glide.with(context)
                .load(book.getImageUrl())
                .placeholder(R.drawable.magazine)
                .error(R.drawable.magazine)
                .into(holder.bookImage);

        // Handle purchase button click
        holder.buyButton.setOnClickListener(v -> {
            if (book.getGoogleBooksUrl() != null && !book.getGoogleBooksUrl().trim().isEmpty()) {
                // Open the purchase link in a browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(book.getGoogleBooksUrl()));
                context.startActivity(browserIntent);
            } else {
                Toast.makeText(context, "Purchase link not available", Toast.LENGTH_SHORT).show();
            }
        });


        // Handle remove from cart button click
        holder.removeButton.setOnClickListener(v -> {
            if (position < 0 || position >= cartBooks.size()) return;
            firestoreHelper.removeBookFromShoppingCart(book.getBookId(), success -> {
                if (success) {
                    // Remove book from local list and update UI
                    cartBooks.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, cartBooks.size());
                    Toast.makeText(context, "Book removed from cart", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to remove book", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /**
     * Returns the total number of books in the cart.
     *
     * @return The size of the cartBooks list.
     */
    @Override
    public int getItemCount() {
        return cartBooks != null ? cartBooks.size() : 0;
    }

    /**
     * ViewHolder class representing each item in the RecyclerView.
     * Contains TextView, ImageView, and Button elements for each book in the cart.
     */
    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView bookTitle; // Book title text
        ImageView bookImage; // Book cover image
        Button buyButton, removeButton; // Buttons for purchasing and removing the book

        /**
         * Constructor for CartViewHolder.
         *
         * @param itemView The view representing a single cart item.
         */
        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            bookTitle = itemView.findViewById(R.id.cart_book_title);
            bookImage = itemView.findViewById(R.id.cart_book_image);
            buyButton = itemView.findViewById(R.id.button_buy_book);
            removeButton = itemView.findViewById(R.id.button_remove_cart);
        }
    }
}