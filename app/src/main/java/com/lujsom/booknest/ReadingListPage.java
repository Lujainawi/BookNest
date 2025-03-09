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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.lujsom.booknest.adapters.ReadingListAdapter;
import com.lujsom.booknest.models.Book;
import java.util.ArrayList;
import java.util.List;

/**
 * ReadingListPage displays the user's reading list.
 * Users can search for books in their reading list.
 * Data is retrieved from Firestore in real-time.
 */
public class ReadingListPage extends AppCompatActivity {

    private ReadingListAdapter adapter;
    private List<Book> readingList;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private EditText searchBar;
    private ListenerRegistration listenerRegistration;

    /**
     * Called when the activity is first created.
     * Initializes UI components, sets up search functionality,
     * and loads the user's reading list if they are logged in.
     *
     * @param savedInstanceState The saved state of the activity, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_list_page);

        // Initialize UI components
        RecyclerView recyclerView = findViewById(R.id.reading_list_recycler);
        searchBar = findViewById(R.id.search_bar);
        TextView emptyMessage = findViewById(R.id.empty_message);
        ImageButton backButton = findViewById(R.id.back_button);

        // Set up RecyclerView with a GridLayout (2 columns)
        int numberOfColumns = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

        // Initialize the reading list and adapter
        readingList = new ArrayList<>();
        List<Book> filteredList = new ArrayList<>();
        adapter = new ReadingListAdapter(this, filteredList);
        recyclerView.setAdapter(adapter);

        // Initialize Firebase Firestore and get the current user
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Back button functionality to return to the previous screen
        backButton.setOnClickListener(v -> finish());

        // Set up search bar to filter books in real-time
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterReadingList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // If user is not logged in, show a message and do not load books
        if (user == null) {
            showToast("You must be logged in to see your reading list.");
            emptyMessage.setVisibility(View.VISIBLE);
        } else {
            loadReadingList(); // Load the reading list from Firestore
        }
    }

    /**
     * Loads the user's reading list from Firestore and updates the UI in real-time.
     * If there are changes in Firestore, the list updates dynamically.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void loadReadingList() {
        listenerRegistration = db.collection("users")
                .document(user.getUid())
                .collection("reading_list")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        showToast("Error loading reading list.");
                        return;
                    }

                    readingList.clear(); // Clear the previous list before updating

                    if (value != null && !value.isEmpty()) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Book book = doc.toObject(Book.class);
                            if (book != null) {
                                readingList.add(book);
                            }
                        }
                    }
                    // Apply the search filter after loading the list
                    filterReadingList(searchBar.getText().toString());
                });
    }

    /**
     * Filters the reading list based on the user's search query and updates the RecyclerView.
     *
     * @param query The search text entered by the user.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void filterReadingList(String query) {
        List<Book> filteredList;

        if (query.trim().isEmpty()) {
            // If the search bar is empty, show the full list
            filteredList = new ArrayList<>(readingList);
        } else {
            // Otherwise, filter books by title
            filteredList = new ArrayList<>();
            query = query.toLowerCase();
            for (Book book : readingList) {
                if (book.getTitle().toLowerCase().contains(query)) {
                    filteredList.add(book);
                }
            }
        }
        // Update the RecyclerView with the filtered list
        adapter.updateList(filteredList);
    }

    /**
     * Cleans up resources when the activity is destroyed.
     * Removes Firestore snapshot listener to prevent memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
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
