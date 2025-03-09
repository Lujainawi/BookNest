package com.lujsom.booknest;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.lujsom.booknest.adapters.ReviewAdapter;
import com.lujsom.booknest.models.Review;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReviewPage allows users to submit and view book reviews.
 * Users can enter a review, provide a rating, and submit it to Firestore.
 * Reviews are displayed in real-time using Firebase Firestore's snapshot listener.
 */
public class ReviewPage extends AppCompatActivity {

    private EditText reviewInput;
    private RatingBar ratingBar;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewData;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String bookId;
    private String username = "Unknown User";
    private ListenerRegistration listenerRegistration;

    /**
     * Called when the activity is first created.
     * Initializes UI components, retrieves the book ID from the intent,
     * loads the user's username, and fetches existing reviews.
     *
     * @param savedInstanceState The saved state of the activity, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_page);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        bookId = getIntent().getStringExtra("book_id"); // Get the book ID from the intent


        // Initialize UI elements
        reviewInput = findViewById(R.id.review_input);
        ratingBar = findViewById(R.id.ratingBar);
        Button submitReview = findViewById(R.id.submit_review);
        RecyclerView reviewsList = findViewById(R.id.reviews_list);

        // Initialize review list and set up RecyclerView
        reviewData = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviewData, bookId);
        reviewsList.setLayoutManager(new LinearLayoutManager(this));
        reviewsList.setAdapter(reviewAdapter);

        // Back button to return to the previous screen
        ImageButton backButton = findViewById(R.id.back);
        backButton.setOnClickListener(v -> finish());

        // Load user's username and reviews
        loadUserName();
        loadReviews();

        // Set up the submit review button
        submitReview.setOnClickListener(v -> submitReview());
    }

    /**
     * Loads the username of the currently logged-in user from Firestore.
     */
    private void loadUserName() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return; // No user is logged in

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        username = documentSnapshot.getString("username"); // Retrieve the username
                    }
                });
    }

    /**
     * Loads reviews for the selected book from Firestore in real-time.
     * Reviews are ordered by timestamp in descending order.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void loadReviews() {
        listenerRegistration = db.collection("books").document(bookId).collection("reviews")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        showToast("Error loading reviews");
                        return;
                    }
                    reviewData.clear(); // Clear the list before adding new reviews

                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Review review = doc.toObject(Review.class);
                            if (review != null) {
                                reviewData.add(review);
                            }
                        }
                    }
                    reviewAdapter.notifyDataSetChanged(); // Refresh the RecyclerView
                });
    }

    /**
     * Handles submitting a new review to Firestore.
     * Ensures the user is logged in, validates input, and saves the review.
     */
    private void submitReview() {
        // Ensure the username is loaded before submitting a review
        if (TextUtils.isEmpty(username) || username.equals("Unknown User")) {
            showToast("Loading username, please try again");
            loadUserName();
            return;
        }

        String reviewText = reviewInput.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (TextUtils.isEmpty(reviewText)) {
            showToast("Please write a review!");
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            showToast("You must be logged in to submit a review");
            return;
        }

        // Create a new review object
        Map<String, Object> review = new HashMap<>();
        review.put("userId", user.getUid());
        review.put("username", username);
        review.put("text", reviewText);
        review.put("rating", rating);
        review.put("timestamp", FieldValue.serverTimestamp());

        // Save the review to Firestore
        db.collection("books").document(bookId).collection("reviews")
                .add(review)
                .addOnSuccessListener(documentReference -> documentReference.update("reviewId", documentReference.getId())
                        .addOnSuccessListener(aVoid -> {
                            showToast("Review submitted!");
                            reviewInput.setText(""); // Clear input field
                            ratingBar.setRating(0); // Reset rating bar
                        })
                        .addOnFailureListener(e -> showToast("Failed to update review ID: " + e.getMessage())))
                .addOnFailureListener(e -> showToast("Failed to submit review: " + e.getMessage()));
    }

    /**
     * Cleans up Firestore resources when the activity is destroyed.
     * Removes the snapshot listener to prevent memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    /**
     * Displays a toast message to the user.
     *
     * @param message The message to be displayed.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}