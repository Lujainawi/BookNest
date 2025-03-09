package com.lujsom.booknest.models;

/**
 * The Review class represents a user review for a book.
 * It includes details such as the review ID, user ID, username, review text, and rating.
 */
public class Review {
    private String reviewId;
    private String userId;
    private String username;
    private String text;
    private float rating;

    /**
     * Default constructor required for Firebase deserialization.
     */
    public Review() {
        // Empty constructor required by Firebase
    }

    /**
     * Constructor to create a Review object with specified details.
     *
     * @param reviewId  Unique review ID.
     * @param userId    The ID of the user who wrote the review.
     * @param username  The username of the reviewer.
     * @param text      The text content of the review.
     * @param rating    The rating given by the user (0-5 scale).
     */
    public Review(String reviewId, String userId, String username, String text, float rating) {
        this.reviewId = (reviewId != null) ? reviewId : "";
        this.userId = (userId != null) ? userId : "Unknown";
        this.username = (username != null) ? username : "Anonymous";
        this.text = (text != null) ? text : "";
        this.rating = rating >= 0 ? rating : 0.0f; // Ensures rating is non-negative
    }


    // Getters
    public String getReviewId() {
        return reviewId != null ? reviewId : "";
    }

    public String getUserId() {
        return userId != null ? userId : "Unknown";
    }

    public String getUsername() {
        return username != null ? username : "Anonymous";
    }

    public String getText() {
        return text != null ? text : "";
    }

    public float getRating() {
        return rating >= 0 ? rating : 0.0f;
    }

    // Setters
    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setRating(float rating) {
        this.rating = rating >= 0 ? rating : 0.0f;
    }
}