package com.lujsom.booknest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.lujsom.booknest.adapters.BookAdapter;
import com.lujsom.booknest.api.ApiClient;
import com.lujsom.booknest.api.BookApiService;
import com.lujsom.booknest.models.Book;
import com.lujsom.booknest.models.BookItem;
import com.lujsom.booknest.models.BookResponse;
import com.lujsom.booknest.utils.FirestoreHelper;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * LibraryPage displays a collection of books for the user to browse.
 * Users can search for books, filter by category, and access their favorites, cart, and reading list.
 */
public class LibraryPage extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private RecyclerView bookGrid;
    private ProgressBar progressBar;
    private TextView usernameText;
    private FirestoreHelper firestoreHelper;
    private static final String TAG = "LibraryPage";
    private static final String API_KEY = BuildConfig.GOOGLE_BOOKS_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_page);

        initUI();
        loadUserName();
        setupNavigation();
        setupSearchBar();
        setupFilters();
        setupBackButtonHandler();

        loadBooks("bestsellers"); // Default book category
    }


    /**
     * Initializes UI components and sets up event listeners.
     */
    private void initUI() {
        drawerLayout = findViewById(R.id.drawer_layout);
        bookGrid = findViewById(R.id.book_grid);
        usernameText = findViewById(R.id.username_text);
        progressBar = findViewById(R.id.progressBar);
        firestoreHelper = new FirestoreHelper();

        ImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        bookGrid.setLayoutManager(new GridLayoutManager(this, 2)); // Display books in a grid format
    }

    /**
     * Loads the user's name from Firestore and updates the UI.
     */
    @SuppressLint("SetTextI18n")
    private void loadUserName() {
        firestoreHelper.getUserName(userName -> usernameText.setText("HELLO, " + userName + "!"));
    }

    /**
     * Sets up the navigation drawer and handles menu item selection.
     */
    private void setupNavigation() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_my_library) {
                startActivity(new Intent(this, ReadingListPage.class));
            } else if (itemId == R.id.nav_favorites) {
                startActivity(new Intent(this, FavoritesPage.class));
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(this, ShoppingCartPage.class));
            } else if (itemId == R.id.nav_logout) {
                handleLogout();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    /**
     * Logs out the current user and navigates to the main screen.
     */
    private void handleLogout() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Sets up the search bar to filter books in real-time as the user types.
     */
    private void setupSearchBar() {
        EditText searchBar = findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadBooks(s.length() > 0 ? s.toString() : "bestsellers");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Sets up category filter buttons that load books by genre.
     */
    private void setupFilters() {
        findViewById(R.id.filter_all).setOnClickListener(v -> loadBooks("bestsellers"));
        findViewById(R.id.filter_scifi).setOnClickListener(v -> loadBooks("science fiction"));
        findViewById(R.id.filter_drama).setOnClickListener(v -> loadBooks("drama"));
        findViewById(R.id.filter_nonfiction).setOnClickListener(v -> loadBooks("non-fiction"));
        findViewById(R.id.filter_selfhelp).setOnClickListener(v -> loadBooks("self-help"));
        findViewById(R.id.filter_romance).setOnClickListener(v -> loadBooks("romance"));
    }

    /**
     * Handles back button behavior, closing the navigation drawer if open.
     */
    private void setupBackButtonHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    /**
     * Fetches books from the API based on the search query or selected category.
     *
     * @param searchQuery The search keyword or category name.
     */
    private void loadBooks(String searchQuery) {
        progressBar.setVisibility(View.VISIBLE); // Show loading indicator

        BookApiService bookApiService = ApiClient.getClient().create(BookApiService.class);
        Call<BookResponse> call = bookApiService.getBooks(searchQuery, API_KEY);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BookResponse> call, @NonNull Response<BookResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<BookItem> bookItems = response.body().getItems();
                    if (bookItems == null || bookItems.isEmpty()) {
                        showToast("No books found.");
                        return;
                    }
                    updateBookList(bookItems);
                } else {
                    logError("Error loading books: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                logError("API Error: " + t.getMessage());
            }
        });
    }

    /**
     * Updates the book list in the RecyclerView with the fetched data.
     *
     * @param bookItems The list of books retrieved from the API.
     */
    private void updateBookList(List<BookItem> bookItems) {
        List<Book> books = new ArrayList<>();
        for (BookItem item : bookItems) {
            books.add(new Book(item));
        }
        BookAdapter bookAdapter = new BookAdapter(this, books);
        bookGrid.setAdapter(bookAdapter);
    }

    /**
     * Displays a short toast message.
     *
     * @param message The message to be displayed.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Logs an error message and displays a toast notification.
     *
     * @param message The error message to be logged.
     */
    private void logError(String message) {
        Log.e(TAG, message);
        showToast(message);
    }

    /**
     * Called when the activity becomes visible again.
     * This method ensures that the user's name is reloaded in case it was updated while the activity was in the background.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadUserName(); // Refresh the displayed username
    }

}