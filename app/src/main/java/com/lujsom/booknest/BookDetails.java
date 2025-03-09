package com.lujsom.booknest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.lujsom.booknest.adapters.BookAdapter;
import com.lujsom.booknest.api.BookApiService;
import com.lujsom.booknest.models.Book;
import com.lujsom.booknest.models.BookResponse;
import com.lujsom.booknest.utils.FirestoreHelper;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * BookDetails activity displays detailed information about a selected book,
 * including options to add it to favorites, reading list, or shopping cart.
 * It also fetches similar books based on the selected book's title.
 */
public class BookDetails extends AppCompatActivity {

    private Button buttonFavorite, buttonReadingList, buttonAddToCart;
    private BookAdapter bookAdapter;
    private final List<Book> similarBooksList = new ArrayList<>();
    private String title, imageUrl, googleBooksUrl, bookId;
    private FirestoreHelper firestoreHelper;
    private boolean isFavorite = false, isInCart = false;

    private static final String API_KEY = "AIzaSyADp0H1gYuMqyL_fKLXbXK5K9zbr-AktBw";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        firestoreHelper = new FirestoreHelper();

        initUI(); // Initialize UI components

        // Retrieve book details from the intent
        Intent intent = getIntent();
        if (intent != null) {
            setupBookDetails(intent);
        }

        // Fetch similar books if title is available
        if (title != null) {
            fetchSimilarBooks(title);
        }

        // Check if the book is in the user's favorites or shopping cart
        if (bookId != null) {
            checkBookStatus();
        }
    }

    /**
     * Initializes the UI components and sets event listeners for buttons.
     */
    private void initUI() {
        buttonFavorite = findViewById(R.id.button_favorite);
        buttonReadingList = findViewById(R.id.button_reading_list);
        buttonAddToCart = findViewById(R.id.button_add_to_cart);
        ImageButton backButton = findViewById(R.id.back);
        Button buttonWriteReview = findViewById(R.id.button_write_review);
        Button buttonViewOnGoogleBooks = findViewById(R.id.button_view_google_books);

        RecyclerView similarBooksRecyclerView = findViewById(R.id.similar_books_recycler_view);
        similarBooksRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        bookAdapter = new BookAdapter(this, similarBooksList);
        similarBooksRecyclerView.setAdapter(bookAdapter);

        // Set button click listeners
        backButton.setOnClickListener(v -> finish());
        buttonAddToCart.setOnClickListener(v -> toggleCart());
        buttonReadingList.setOnClickListener(v -> toggleReadingList());
        buttonFavorite.setOnClickListener(v -> toggleFavorite());

        buttonWriteReview.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetails.this, ReviewPage.class);
            intent.putExtra("book_id", bookId);
            startActivity(intent);
        });

        buttonViewOnGoogleBooks.setOnClickListener(v -> openGoogleBooks());
    }

    /**
     * Opens the Google Books page for the selected book.
     */
    private void openGoogleBooks() {
        if (googleBooksUrl == null || googleBooksUrl.trim().isEmpty()) {
            showToast("No Google Books link available.");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(android.net.Uri.parse(googleBooksUrl));
        startActivity(intent);
    }


    /**
     * Retrieves book details from the intent and updates the UI accordingly.
     *
     * @param intent The intent containing book details.
     */
    @SuppressLint("SetTextI18n")
    private void setupBookDetails(Intent intent) {
        // Extract book details from the intent
        bookId = intent.getStringExtra("book_id");
        title = intent.getStringExtra("book_title");
        imageUrl = intent.getStringExtra("book_image");
        String description = intent.getStringExtra("book_description");
        String author = intent.getStringExtra("book_author");
        String genre = intent.getStringExtra("book_genre");
        String rating = intent.getStringExtra("book_rating");
        String publishedYear = intent.getStringExtra("book_published_year");

        // Generate Google Books URL based on the title
        googleBooksUrl = title != null ? "https://books.google.com/books?q=" + title.replace(" ", "+") : "https://books.google.com/";

        ImageView bookImage = findViewById(R.id.book_image_details);
        TextView bookTitle = findViewById(R.id.book_title_details);
        TextView bookDescription = findViewById(R.id.book_description_details);
        TextView bookAuthor = findViewById(R.id.book_authors_details);
        TextView bookGenre = findViewById(R.id.book_genre_details);
        TextView bookRating = findViewById(R.id.book_rating_details);
        TextView bookPublishedYear = findViewById(R.id.book_published_date_details);

        // Load the book image using Glide
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            Glide.with(this)
                    .load(R.drawable.magazine)
                    .into(bookImage);
        } else {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.magazine)
                    .into(bookImage);
        }

        bookTitle.setText(title != null ? title : "Title not available");
        bookDescription.setText(description != null && !description.isEmpty() ? description : "No description available");

        if (author == null || author.trim().isEmpty()) {
            bookAuthor.setVisibility(View.GONE);
        } else {
            bookAuthor.setText("Author: " + author);
            bookAuthor.setVisibility(View.VISIBLE);
        }

        if (genre == null || genre.trim().isEmpty() || genre.equalsIgnoreCase("N/A")) {
            bookGenre.setVisibility(View.GONE);
        } else {
            bookGenre.setText("Genre: " + genre);
            bookGenre.setVisibility(View.VISIBLE);
        }

        if (rating == null || rating.trim().isEmpty() || rating.equalsIgnoreCase("N/A")) {
            bookRating.setVisibility(View.GONE);
        } else {
            bookRating.setText("Rating: " + rating);
            bookRating.setVisibility(View.VISIBLE);
        }

        if (publishedYear == null || publishedYear.trim().isEmpty() || publishedYear.equalsIgnoreCase("....")) {
            bookPublishedYear.setVisibility(View.GONE);
        } else {
            bookPublishedYear.setText("Published Year: " + publishedYear);
            bookPublishedYear.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Checks if the book is in the user's favorites or shopping cart and updates UI accordingly.
     */
    private void checkBookStatus() {
        firestoreHelper.checkIfFavorite(bookId, isFavorite -> {
            this.isFavorite = isFavorite;
            buttonFavorite.setText(isFavorite ? "Remove From Favorites" : "Add to Favorites");
        });

        firestoreHelper.isBookInCart(bookId, isInCart -> {
            this.isInCart = isInCart;
            buttonAddToCart.setText(isInCart ? "Remove From Cart" : "Add to Cart");
        });
    }

    /**
     * Fetches similar books from the Google Books API based on the current book's title.
     *
     * @param bookTitle The title of the book to find similar books for.
     */
    private void fetchSimilarBooks(String bookTitle) {
        if (bookTitle == null || bookTitle.isEmpty()) return;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/books/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BookApiService apiService = retrofit.create(BookApiService.class);
        Call<BookResponse> call = apiService.getBooks(bookTitle, API_KEY);

        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<BookResponse> call, @NonNull Response<BookResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> books = response.body().toBookList();
                    if (books != null && !books.isEmpty()) {
                        similarBooksList.clear();
                        similarBooksList.addAll(books);
                        bookAdapter.notifyItemRangeInserted(0, books.size());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookResponse> call, @NonNull Throwable t) {
                Log.e("BookDetails", "Failed to fetch similar books", t);
            }
        });
    }

    /**
     * Toggles the book's status in the reading list. If the book is already in the reading list,
     * it will be removed; otherwise, it will be added.
     */
    @SuppressLint("SetTextI18n")
    private void toggleReadingList() {
        if (isBookDataMissing()) return;

        firestoreHelper.isBookInReadingList(bookId, isInList -> {
            if (isInList) {
                firestoreHelper.removeBookFromReadingList(bookId, success -> {
                    if (success) {
                        buttonReadingList.setText("Add to Reading List");
                        showToast("Removed from Reading List");
                    }
                });
            } else {
                firestoreHelper.addBookToReadingList(bookId, title, imageUrl, success -> {
                    if (success) {
                        buttonReadingList.setText("Remove From Reading List");
                        showToast("Added to Reading List!");
                    }
                });
            }
        });
    }

    /**
     * Toggles the book's status in the favorites list. If the book is already a favorite,
     * it will be removed; otherwise, it will be added.
     */
    @SuppressLint("SetTextI18n")
    private void toggleFavorite() {
        if (isBookDataMissing()) return;

        if (isFavorite) {
            firestoreHelper.removeBookFromFavorites(bookId, success -> {
                if (success) {
                    isFavorite = false;
                    buttonFavorite.setText("Add to Favorites");
                    showToast("Removed from Favorites");
                }
            });
        } else {
            firestoreHelper.addBookToFavorites(bookId, title, imageUrl, success -> {
                if (success) {
                    isFavorite = true;
                    buttonFavorite.setText("Remove From Favorites");
                    showToast("Added to Favorites!");
                }
            });
        }
    }

    /**
     * Toggles the book's status in the shopping cart. If the book is already in the cart,
     * it will be removed; otherwise, it will be added.
     */
    @SuppressLint("SetTextI18n")
    private void toggleCart() {
        if (isBookDataMissing()) return;

        if (isInCart) {
            firestoreHelper.removeBookFromShoppingCart(bookId, success -> {
                if (success) {
                    isInCart = false;
                    buttonAddToCart.setText("Add to Cart");
                    showToast("Removed from Cart");
                }
            });
        } else {
            firestoreHelper.addBookToShoppingCart(bookId, title, imageUrl, googleBooksUrl, success -> {
                if (success) {
                    isInCart = true;
                    buttonAddToCart.setText("Remove From Cart");
                    showToast("Added to Cart");
                }
            });
        }
    }

    private boolean isBookDataMissing() {
        if (bookId == null || title == null || imageUrl == null) {
            showToast("Book data is missing!");
            return true;
        }
        return false;
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
