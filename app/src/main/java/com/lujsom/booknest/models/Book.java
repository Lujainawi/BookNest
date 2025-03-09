package com.lujsom.booknest.models;

/**
 * The Book class represents a book object in the application.
 * It includes details such as title, author, genre, rating, description, and URLs for images and reading.
 * This class is used for handling book data retrieved from the API or Firebase Firestore.
 */
public class Book {
    private String bookId;
    private  String title;
    private  String author;
    private  String publishedYear;
    private String genre;
    private  String rating;
    private String description;
    private  String imageUrl;
    private  String googleBooksUrl;
    private  String pdfUrl;
    private String readingUrl;


    /**
     * Default constructor required for Firebase deserialization.
     */
    public Book() {
        // Empty constructor required by Firebase
    }

    /**
     * Constructor to create a Book object with specified details.
     *
     * @param bookId        Unique book ID.
     * @param title         Book title.
     * @param author        Author of the book.
     * @param publishedYear Year of publication.
     * @param genre         Book genre.
     * @param rating        Rating of the book.
     * @param description   Brief description of the book.
     * @param imageUrl      URL of the book's cover image.
     * @param googleBooksUrl URL to view the book on Google Books.
     * @param pdfUrl        URL to download a PDF version if available.
     * @param readingUrl    URL to read the book online.
     */
    public Book(String bookId, String title, String author, String publishedYear, String genre,
                String rating, String description, String imageUrl, String googleBooksUrl, String pdfUrl, String readingUrl) {
        this.bookId = bookId != null ? bookId : "N/A";
        this.title = title != null ? title : "Unknown Title";
        this.author = author != null ? author : "Unknown Author";
        this.publishedYear = publishedYear != null ? publishedYear : "N/A";
        this.genre = genre != null ? genre : "Unknown";
        this.rating = rating != null ? rating : "0.0";
        this.description = description != null ? description : "No description available.";

        // Ensure image URLs use HTTPS for security
        this.imageUrl = (imageUrl != null && !imageUrl.isEmpty())
                ? imageUrl.replace("http://", "https://")
                : "android.resource://com.lujsom.booknest/drawable/magazine";
        this.googleBooksUrl = googleBooksUrl != null ? googleBooksUrl : "";
        this.pdfUrl = pdfUrl != null ? pdfUrl : "";
        this.readingUrl = readingUrl != null ? readingUrl : "";
    }

    /**
     * Constructor that initializes a Book object using a BookItem retrieved from the API.
     *
     * @param item The BookItem object containing book details from the API response.
     */
    public Book(BookItem item) {
        this.bookId = item.getId();
        this.title = item.getVolumeInfo().getTitle();
        this.author = item.getVolumeInfo().getAuthor();
        this.publishedYear = item.getVolumeInfo().getPublishedDate();
        this.genre = item.getVolumeInfo().getGenre();
        this.rating = item.getVolumeInfo().getRating();
        this.description = item.getVolumeInfo().getDescription();
        this.imageUrl = item.getVolumeInfo().getThumbnail();

        // Ensure image URLs use HTTPS
        if (this.imageUrl != null && this.imageUrl.startsWith("http://")) {
            this.imageUrl = this.imageUrl.replace("http://", "https://");
        }
        this.googleBooksUrl = item.getVolumeInfo().getPreviewLink();
        this.pdfUrl = (item.getAccessInfo() != null && item.getAccessInfo().getPdf() != null)
                ? item.getAccessInfo().getPdf().getDownloadLink() : "";
        this.readingUrl = "";
    }

    // Getters
    public String getBookId() {return bookId;}

    public String getTitle() {return title;}

    public String getAuthor() {return author;}

    public String getPublishedYear() {return publishedYear;}

    public String getGenre() {return genre;}

    public String getRating() {return rating;}

    public String getDescription() {return description;}

    public String getImageUrl() {return imageUrl;}

    public String getGoogleBooksUrl() {return googleBooksUrl;}
    public String getReadingUrl() { return readingUrl; }
}
