package com.lujsom.booknest.adapters;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lujsom.booknest.R;
import com.lujsom.booknest.models.Review;
import java.util.List;
import java.util.Objects;

/**
 * ReviewAdapter is a RecyclerView adapter for displaying user reviews.
 * It allows users to view book reviews and delete their own reviews.
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private final Context context;
    private final List<Review> reviewList;
    private final String bookId;

    /**
     * Constructor: Initializes the adapter with the application context, a list of reviews, and the book ID.
     *
     * @param context    The application or activity context.
     * @param reviewList A list of reviews for the book.
     * @param bookId     The ID of the book associated with these reviews.
     */
    public ReviewAdapter(Context context, List<Review> reviewList, String bookId) {
        this.context = context;
        this.reviewList = reviewList;
        this.bookId = bookId;
    }

    /**
     * Creates a new ViewHolder by inflating the item layout.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The view type (not used in this case).
     * @return A new ReviewViewHolder instance.
     */
    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    /**
     * Binds data to a ViewHolder at a given position.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        if (reviewList == null || position >= reviewList.size()) {
            Log.e(TAG, "Invalid review list or position");
            return;
        }
        Review review = reviewList.get(position);

        // Set review data, using fallback values if necessary
        holder.username.setText(review.getUsername());
        holder.reviewText.setText(review.getText());
        holder.reviewRating.setRating(review.getRating());

        // Show delete button only if the logged-in user wrote the review
        if (Objects.equals(FirebaseAuth.getInstance().getUid(), review.getUserId())) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> deleteReview(review));
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }
    }

    /**
     * Deletes a review from Firestore and updates the UI.
     *
     * @param review The review to be deleted.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void deleteReview(Review review) {
        if (review.getReviewId() == null) {
            Toast.makeText(context, "Cannot delete review without ID", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("books").document(bookId)
                .collection("reviews").document(review.getReviewId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    reviewList.remove(review);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Review deleted!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete review: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Returns the total number of reviews in the list.
     *
     * @return The size of the reviewList.
     */
    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    /**
     * ViewHolder class representing each item in the RecyclerView.
     * Contains TextView, RatingBar, and ImageButton elements that represent each review.
     */
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView username, reviewText;  // Reviewer's username and review text
        RatingBar reviewRating; // Review rating in stars
        ImageButton deleteButton; // Button to delete the review

        /**
         * Constructor for ReviewViewHolder.
         *
         * @param itemView The view representing a single review item.
         */
        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.review_username);
            reviewText = itemView.findViewById(R.id.review_text);
            reviewRating = itemView.findViewById(R.id.review_rating);
            deleteButton = itemView.findViewById(R.id.delete_review);
        }
    }
}