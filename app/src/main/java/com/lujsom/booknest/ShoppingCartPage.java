package com.lujsom.booknest;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.lujsom.booknest.adapters.ShoppingCartAdapter;
import com.lujsom.booknest.models.Book;
import com.lujsom.booknest.utils.FirestoreHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * ShoppingCartPage displays the books that the user has added to their shopping cart.
 * Users can search for books in their cart and view them in a grid format.
 */
public class ShoppingCartPage extends AppCompatActivity {
    private ShoppingCartAdapter adapter;
    private List<Book> cartBooks, filteredCartBooks;
    private FirestoreHelper firestoreHelper;
    private TextView emptyMessage;
    private RecyclerView recyclerView;

    /**
     * Called when the activity is created.
     * Initializes UI components, sets up search functionality, and loads the shopping cart.
     *
     * @param savedInstanceState The saved state of the activity, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart_page);

        // Initialize FirestoreHelper instance
        firestoreHelper = new FirestoreHelper();

        // Set up UI component
        recyclerView = findViewById(R.id.shopping_cart_recycler);
        EditText searchBar = findViewById(R.id.search_bar);
        emptyMessage = findViewById(R.id.empty_message);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        cartBooks = new ArrayList<>();
        filteredCartBooks = new ArrayList<>();
        adapter = new ShoppingCartAdapter(this, cartBooks, firestoreHelper);
        recyclerView.setAdapter(adapter);

        // Back button to return to the previous screen
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Set up search bar to filter books in real-time
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterShoppingCart(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Load books from Firestore shopping cart
        loadShoppingCart();
    }

    /**
     * Loads the user's shopping cart from Firestore.
     * Updates the UI to show the cart's contents or display a message if it's empty.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void loadShoppingCart() {
        firestoreHelper.getShoppingCart(cartBooks -> {
            this.cartBooks.clear();
            this.cartBooks.addAll(cartBooks);
            adapter.notifyDataSetChanged();

            // Show or hide the empty message based on cart contents
            if (cartBooks.isEmpty()) {
                emptyMessage.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyMessage.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }, this::showToast);
    }

    /**
     * Filters the shopping cart based on the user's search query.
     * Updates the RecyclerView to display matching results.
     *
     * @param query The search text entered by the user.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void filterShoppingCart(String query) {
        query = query.trim().toLowerCase();
        filteredCartBooks.clear();

        // If search query is empty, show all books
        if (query.isEmpty()) {
            filteredCartBooks.addAll(cartBooks);
        } else {
            // Otherwise, filter books by title
            for (Book book : cartBooks) {
                if (book.getTitle().toLowerCase().contains(query)) {
                    filteredCartBooks.add(book);
                }
            }
        }

        adapter.updateList(filteredCartBooks);

        // Show or hide the empty message based on search results
        if (filteredCartBooks.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Displays a toast message when an error occurs.
     */
    private void showToast() {
        Toast.makeText(this, "Failed to load cart", Toast.LENGTH_SHORT).show();
    }
}