package com.lujsom.booknest.utils;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lujsom.booknest.models.Book;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * FirestoreHelper provides utility functions for managing user data in Firestore.
 * It handles user information, favorites, reading lists, and shopping cart operations.
 */
public class FirestoreHelper {
    private final FirebaseFirestore db;
    private final String userId;

    /**
     * Initializes Firestore and gets the current user ID.
     */
    public FirestoreHelper() {
        this.db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        this.userId = (user != null) ? user.getUid() : null;
    }

    /**
     * Retrieves the username from Firestore. Uses caching to optimize performance.
     *
     * @param callback A function to handle the retrieved username.
     */
    public void getUserName(Consumer<String> callback) {
        if (userId == null) {
            callback.accept("Guest");
            return;
        }

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("username");
                        callback.accept(userName != null ? userName : "Guest");
                    } else {
                        callback.accept("Guest");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "Error fetching user name", e);
                    callback.accept("Guest");
                });
    }

    /**
     * Adds a book to the user's favorites in Firestore.
     *
     * @param bookId   The book's unique ID.
     * @param title    The book's title.
     * @param imageUrl The book's image URL.
     * @param callback A function to handle the success status.
     */
    public void addBookToFavorites(String bookId, String title, String imageUrl, Consumer<Boolean> callback) {
        if (userId == null || bookId == null || bookId.isEmpty()) {
            Log.e("FIRESTORE", "Invalid request – user not logged in or bookId missing.");
            callback.accept(false);
            return;
        }

        DocumentReference favRef = db.collection("users").document(userId).collection("favorites").document(bookId);

        Map<String, Object> book = new HashMap<>();
        book.put("bookId", bookId);
        book.put("title", title);
        book.put("imageUrl", imageUrl);

        favRef.set(book)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FIRESTORE", "✅ Book added to favorites: " + bookId);
                    callback.accept(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "❌ Error adding book to favorites", e);
                    callback.accept(false);
                });
    }

    /**
     * Removes a book from the user's favorites.
     *
     * @param bookId   The book's unique ID.
     * @param callback A function to handle the success status.
     */
    public void removeBookFromFavorites(String bookId, Consumer<Boolean> callback) {
        if (userId == null) {
            Log.e("FIRESTORE", " User not logged in – cannot remove favorite book.");
            callback.accept(false);
            return;
        }

        DocumentReference favRef = db.collection("users").document(userId).collection("favorites").document(bookId);

        favRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FIRESTORE", "✅ Book removed from favorites: " + bookId);
                    callback.accept(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "❌ Error removing book from favorites", e);
                    callback.accept(false);
                });
    }

    /**
     * Adds a book to the user's reading list in Firestore.
     *
     * @param bookId   The unique ID of the book.
     * @param title    The title of the book.
     * @param imageUrl The image URL of the book.
     * @param callback A function that returns true if the operation is successful, false otherwise.
     */
    public void addBookToReadingList(String bookId, String title, String imageUrl, Consumer<Boolean> callback) {
        if (userId == null || bookId == null || bookId.isEmpty()) {
            Log.e("FIRESTORE", "❌ User not logged in or invalid bookId – cannot add to reading list.");
            callback.accept(false);
            return;
        }

        DocumentReference bookRef = db.collection("users").document(userId).collection("reading_list").document(bookId);

        Map<String, Object> book = new HashMap<>();
        book.put("bookId", bookId);
        book.put("title", title);
        book.put("imageUrl", imageUrl);

        bookRef.set(book)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FIRESTORE", "✅ Book added to reading list: " + bookId);
                    callback.accept(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "❌ Error adding book to reading list", e);
                    callback.accept(false);
                });
    }

    /**
     * Removes a book from the user's reading list in Firestore.
     *
     * @param bookId   The unique ID of the book.
     * @param callback A function that returns true if the operation is successful, false otherwise.
     */
    public void removeBookFromReadingList(String bookId, Consumer<Boolean> callback) {
        if (userId == null || bookId == null || bookId.isEmpty()) {
            Log.e("FIRESTORE", "❌ User not logged in or invalid bookId – cannot remove from reading list.");
            callback.accept(false);
            return;
        }

        DocumentReference bookRef = db.collection("users").document(userId).collection("reading_list").document(bookId);

        bookRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FIRESTORE", "✅ Book removed from reading list: " + bookId);
                    callback.accept(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "❌ Error removing book from reading list", e);
                    callback.accept(false);
                });
    }

    /**
     * Checks if a book is in the user's reading list.
     *
     * @param bookId   The unique ID of the book.
     * @param callback A function that returns true if the book is in the reading list, false otherwise.
     */
    public void isBookInReadingList(String bookId, Consumer<Boolean> callback) {
        if (userId == null || bookId == null || bookId.isEmpty()) {
            Log.e("FIRESTORE", "❌ User not logged in or invalid bookId – cannot check reading list.");
            callback.accept(false);
            return;
        }

        DocumentReference bookRef = db.collection("users").document(userId).collection("reading_list").document(bookId);

        bookRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Log.d("FIRESTORE", "✅ Book is in reading list: " + bookId);
                callback.accept(true);
            } else {
                Log.d("FIRESTORE", "❌ Book is NOT in reading list: " + bookId);
                callback.accept(false);
            }
        }).addOnFailureListener(e -> {
            Log.e("FIRESTORE", "❌ Error checking reading list status", e);
            callback.accept(false);
        });
    }

    /**
     * Checks if a book is in the user's favorites.
     *
     * @param bookId   The book's unique ID.
     * @param callback A function to return true if the book is in favorites, false otherwise.
     */
    public void checkIfFavorite(String bookId, Consumer<Boolean> callback) {
        if (userId == null || bookId == null || bookId.isEmpty()) {
            Log.e("FIRESTORE", "❌ User not logged in or invalid bookId – cannot check favorites.");
            callback.accept(false);
            return;
        }

        DocumentReference favRef = db.collection("users").document(userId).collection("favorites").document(bookId);

        favRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Log.d("FIRESTORE", "✅ Book is in favorites: " + bookId);
                callback.accept(true);
            } else {
                Log.d("FIRESTORE", "❌ Book is NOT in favorites: " + bookId);
                callback.accept(false);
            }
        }).addOnFailureListener(e -> {
            Log.e("FIRESTORE", "❌ Error checking favorite status", e);
            callback.accept(false);
        });
    }


    /**
     * Adds a book to the shopping cart.
     *
     * @param bookId        The book's unique ID.
     * @param title         The book's title.
     * @param imageUrl      The book's image URL.
     * @param googleBooksUrl The book's Google Books URL.
     * @param callback      A function to handle the success status.
     */
    public void addBookToShoppingCart(String bookId, String title, String imageUrl, String googleBooksUrl, Consumer<Boolean> callback) {
        if (userId == null || bookId == null || bookId.isEmpty()) {
            Log.e("FIRESTORE", "❌ User not logged in or invalid bookId – cannot add to cart.");
            callback.accept(false);
            return;
        }

        DocumentReference cartRef = db.collection("users").document(userId).collection("shopping_cart").document(bookId);

        Map<String, Object> book = new HashMap<>();
        book.put("bookId", bookId);
        book.put("title", title);
        book.put("imageUrl", imageUrl);
        book.put("googleBooksUrl", googleBooksUrl);

        cartRef.set(book)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FIRESTORE", "✅ Book added to shopping cart: " + bookId);
                    callback.accept(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "❌ Error adding book to cart", e);
                    callback.accept(false);
                });
    }

    /**
     * Removes a book from the user's shopping cart in Firestore.
     *
     * @param bookId   The unique ID of the book.
     * @param callback A function that returns true if the operation is successful, false otherwise.
     */
    public void removeBookFromShoppingCart(String bookId, Consumer<Boolean> callback) {
        if (userId == null || bookId == null || bookId.isEmpty()) {
            Log.e("FIRESTORE", "❌ User not logged in or invalid bookId – cannot remove from cart.");
            callback.accept(false);
            return;
        }

        DocumentReference cartRef = db.collection("users").document(userId).collection("shopping_cart").document(bookId);

        cartRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FIRESTORE", "✅ Book removed from shopping cart: " + bookId);
                    callback.accept(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "❌ Error removing book from cart", e);
                    callback.accept(false);
                });
    }

    /**
     * Retrieves all books from the user's shopping cart.
     *
     * @param onSuccess A function to handle the retrieved list of books.
     * @param onFailure A function to handle errors.
     */
    public void getShoppingCart(Consumer<List<Book>> onSuccess, Runnable onFailure) {
        if (userId == null) {
            Log.e("FIRESTORE", "❌ User not logged in – cannot fetch shopping cart.");
            onFailure.run();
            return;
        }

        db.collection("users").document(userId).collection("shopping_cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Book> cartBooks = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Book book = document.toObject(Book.class);
                        cartBooks.add(book);
                    }
                    onSuccess.accept(cartBooks);
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "❌ Error loading shopping cart", e);
                    onFailure.run();
                });
    }


    /**
     * Checks if a book is in the user's shopping cart.
     *
     * @param bookId   The unique ID of the book.
     * @param callback A function that returns true if the book is in the shopping cart, false otherwise.
     */
    public void isBookInCart(String bookId, Consumer<Boolean> callback) {
        if (userId == null || bookId == null || bookId.isEmpty()) {
            Log.e("FIRESTORE", "❌ User not logged in or invalid bookId – cannot check cart.");
            callback.accept(false);
            return;
        }

        DocumentReference cartRef = db.collection("users").document(userId).collection("shopping_cart").document(bookId);

        cartRef.get().addOnSuccessListener(documentSnapshot -> {
            boolean exists = documentSnapshot.exists();
            Log.d("FIRESTORE", exists ? "✅ Book is in cart: " + bookId : "❌ Book is NOT in cart: " + bookId);
            callback.accept(exists);
        }).addOnFailureListener(e -> {
            Log.e("FIRESTORE", "❌ Error checking cart status", e);
            callback.accept(false);
        });
    }
}