package com.lujsom.booknest.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ApiClient is a singleton class responsible for creating and providing a Retrofit instance.
 * It is used to interact with the Google Books API.
 */
public class ApiClient {

    // Base URL for Google Books API
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/"; // The API address
    // Retrofit instance (singleton)
    private static Retrofit retrofit = null;

    /**
     * Provides a Retrofit client instance.
     * If the instance is null, it initializes a new one with an OkHttpClient.
     *
     * @return A Retrofit instance configured for API communication.
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            // Create an OkHttpClient with custom timeout settings
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS) // Maximum time to establish a connection
                    .readTimeout(15, TimeUnit.SECONDS) // Maximum time to wait for response data
                    .writeTimeout(15, TimeUnit.SECONDS) // Maximum time for sending data
                    .build();

            // Build the Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // Set the base URL for API requests
                    .client(client) // Use the configured OkHttpClient
                    .addConverterFactory(GsonConverterFactory.create()) // Convert JSON responses to Java objects
                    .build();
        }
        return retrofit;
    }
}
