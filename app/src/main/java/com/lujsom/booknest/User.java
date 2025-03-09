package com.lujsom.booknest;
import androidx.annotation.NonNull;

/**
 * The User class represents a user in the application.
 * It includes the user's ID, username, and email.
 * This class is used for Firebase Firestore operations and user authentication handling.
 */
public class User {
    private String userId;
    private String username;
    private String email;

    /**
     * Default constructor required for Firestore data mapping.
     */
    public User() {}

    /**
     * Constructor to create a new User object with specified details.
     *
     * @param userId   The unique identifier of the user.
     * @param username The username of the user.
     * @param email    The email address of the user.
     */
    public User(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    /**
     * Sets the user ID.
     *
     * @param userId The unique identifier for the user.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Retrieves the username. If the username is null, returns "Guest".
     *
     * @return The username or "Guest" if no username is set.
     */
    @NonNull
    public String getUsername() {
        return username != null ? username : "Guest";
    }

    /**
     * Sets the username for the user.
     *
     * @param username The username to be assigned.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Retrieves the user's email. If the email is null, returns "Unknown".
     *
     * @return The email address or "Unknown" if no email is set.
     */
    @NonNull
    public String getEmail() {
        return email != null ? email : "Unknown";
    }

    /**
     * Sets the email address for the user.
     *
     * @param email The email to be assigned.
     */
    public void setEmail(String email) {
        this.email = email;
    }
}