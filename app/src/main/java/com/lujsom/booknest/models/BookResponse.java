package com.lujsom.booknest.models;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * BookResponse represents the response from the Google Books API.
 * It contains a list of book items retrieved from the API.
 */
public class BookResponse {
    @SerializedName("items")
    private List<BookItem> items;

    /**
     * Retrieves the list of book items from the API response.
     *
     * @return A list of BookItem objects, or an empty list if none are available.
     */
    public List<BookItem> getItems() {
        return (items != null) ? items : Collections.emptyList();
    }

    /**
     * Converts the list of BookItem objects into a list of Book objects.
     * This method maps API book data into the app's internal book representation.
     *
     * @return A list of Book objects.
     */
    public List<Book> toBookList() {
        List<Book> bookList = new ArrayList<>();
        if (items != null) {
            for (BookItem item : items) {
                if (item != null) {
                    bookList.add(new Book(item)); // Convert each BookItem to a Book object
                }
            }
        }
        return bookList;
    }
}