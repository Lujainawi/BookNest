package com.lujsom.booknest;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lujsom.booknest.adapters.FavoriteAdapter;
import com.lujsom.booknest.models.Book;
import java.util.ArrayList;
import java.util.List;

/**
 * FavoritesPage activity displays the user's favorite books.
 * Users can search through their favorites using the search bar.
 */
public class FavoritesPage extends AppCompatActivity {

    private FavoriteAdapter adapter;
    private final List<Book> favoriteBooks = new ArrayList<>();
    private final List<Book> filteredFavorites = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites_page);

        // Initialize RecyclerView with a GridLayout (2 columns)
        RecyclerView recyclerView = findViewById(R.id.recycler_favorites);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Initialize search bar functionality
        EditText searchBar = findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFavorites(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Back button to return to the previous screen
        ImageButton backButton = findViewById(R.id.back);
        backButton.setOnClickListener(v -> finish());

        // Initialize the adapter with the list of favorite books
        adapter = new FavoriteAdapter(this, favoriteBooks);
        recyclerView.setAdapter(adapter);

        // Load favorite books from Firestore
        loadFavorites();
    }

    /**
     * Loads the user's favorite books from Firestore and updates the RecyclerView.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void loadFavorites() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showToast("No user logged in");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).collection("favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    favoriteBooks.clear(); // Clear the list before adding new items
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        // Retrieve book details from Firestore
                        String bookId = doc.getString("bookId");
                        String title = doc.getString("title");
                        String imageUrl = doc.getString("imageUrl");

                        // Create a Book object and add it to the list
                        favoriteBooks.add(new Book(bookId, title, "Unknown", "N/A", "N/A", "N/A", "No description", imageUrl, "", "", ""));
                    }
                    // Update the filtered list and notify the adapter
                    filteredFavorites.clear();
                    filteredFavorites.addAll(favoriteBooks);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> showToast("Failed to load favorites"));
    }

    /**
     * Filters the favorite books based on the search query and updates the RecyclerView.
     *
     * @param query The search text entered by the user.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void filterFavorites(String query) {
        query = query.trim().toLowerCase();
        filteredFavorites.clear(); // Clear previous search results

        if (query.isEmpty()) {
            // If search is empty, show all favorite books
            filteredFavorites.addAll(favoriteBooks);
        } else {
            // Otherwise, filter books based on the search query
            for (Book book : favoriteBooks) {
                if (book.getTitle().toLowerCase().contains(query)) {
                    filteredFavorites.add(book);
                }
            }
        }

        adapter.updateList(filteredFavorites); // Update the RecyclerView with filtered results
    }

    /**
     * Displays a short toast message.
     *
     * @param message The message to be displayed.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
