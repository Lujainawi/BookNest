package com.lujsom.booknest.api;

import com.lujsom.booknest.models.BookResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * BookApiService defines the API endpoints for fetching books from the Google Books API.
 * It uses Retrofit to send HTTP requests.
 */
public interface BookApiService {

    /**
     * Fetches books from the Google Books API based on the given search query.
     *
     * @param query  The search query (e.g., book title, author, keyword).
     * @param apiKey The API key required to authenticate the request.
     * @return A Call object containing the response in the form of a BookResponse.
     */
    @GET("volumes") // This corresponds to "https://www.googleapis.com/books/v1/volumes"
    Call<BookResponse> getBooks(
            @Query("q") String query, // The search term for books
            @Query("key") String apiKey // The API key for authentication
    );
}