package com.lujsom.booknest.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * BookItem represents a book object retrieved from the Google Books API.
 * It includes metadata such as title, author, published date, rating, description, and access information.
 */
public class BookItem {
    @SerializedName("id")
    private String id;

    @SerializedName("volumeInfo")
    private VolumeInfo volumeInfo;

    @SerializedName("accessInfo")
    private AccessInfo accessInfo;

    public String getId() {
        return id != null ? id : "N/A";
    }

    public VolumeInfo getVolumeInfo() {
        return volumeInfo != null ? volumeInfo : new VolumeInfo();
    }

    public AccessInfo getAccessInfo() {
        return accessInfo != null ? accessInfo : new AccessInfo();
    }

    public static class VolumeInfo {
        @SerializedName("title")
        private String title;

        @SerializedName("authors")
        private List<String> authors;

        @SerializedName("publishedDate")
        private String publishedDate;

        @SerializedName("categories")
        private List<String> categories;

        @SerializedName("averageRating")
        private Double averageRating;

        @SerializedName("description")
        private String description;

        @SerializedName("imageLinks")
        private ImageLinks imageLinks;

        @SerializedName("previewLink")
        private String previewLink;

        /**
         * Retrieves the book's title.
         *
         * @return The title or "Unknown Title" if unavailable.
         */
        public String getTitle() {
            return title != null ? title : "Unknown Title";
        }

        /**
         * Retrieves the book's primary author.
         *
         * @return The author's name or "Unknown Author" if unavailable.
         */
        public String getAuthor() {
            return (authors != null && !authors.isEmpty()) ? authors.get(0) : "Unknown Author";
        }

        /**
         * Retrieves the book's published date.
         *
         * @return The published date or "N/A" if unavailable.
         */

        public String getPublishedDate() {
            return publishedDate != null ? publishedDate : "N/A";
        }

        /**
         * Retrieves the book's primary genre/category.
         *
         * @return The genre or "N/A" if unavailable.
         */
        public String getGenre() {
            return (categories != null && !categories.isEmpty()) ? categories.get(0) : "N/A";
        }

        /**
         * Retrieves the book's rating.
         *
         * @return The rating as a string or "N/A" if unavailable.
         */
        public String getRating() {
            return (averageRating != null) ? String.valueOf(averageRating) : "N/A";
        }

        /**
         * Retrieves the book's description.
         *
         * @return The description or "No description available." if unavailable.
         */
        public String getDescription() {
            return (description != null) ? description : "No description available.";
        }

        /**
         * Retrieves the book's thumbnail image URL.
         * Ensures the URL uses HTTPS for security.
         *
         * @return The image URL or a default placeholder if unavailable.
         */
        public String getThumbnail() {
            if (imageLinks != null && imageLinks.thumbnail != null && !imageLinks.thumbnail.isEmpty()) {
                return imageLinks.thumbnail.replace("http://", "https://");
            }
            return "android.resource://com.lujsom.booknest/drawable/magazine.png";
        }

        /**
         * Retrieves the book's preview link on Google Books.
         *
         * @return The preview link or an empty string if unavailable.
         */
        public String getPreviewLink() {
            return previewLink != null ? previewLink : "";
        }
    }

    /**
     * ImageLinks contains the URLs for different image formats of the book.
     */
    public static class ImageLinks {
        @SerializedName("thumbnail")
        private String thumbnail;
    }

    /**
     * AccessInfo provides details about the book's availability and access options.
     */
    public static class AccessInfo {
        @SerializedName("pdf")
        private PdfInfo pdf;

        /**
         * Retrieves the PDF information for the book.
         *
         * @return A PdfInfo object containing PDF availability and download details.
         */
        public PdfInfo getPdf() {
            return pdf != null ? pdf : new PdfInfo();
        }
    }

    /**
     * PdfInfo contains information about the availability of a downloadable PDF version of the book.
     */
    public static class PdfInfo {
        @SerializedName("isAvailable")
        private boolean isAvailable;

        @SerializedName("downloadLink")
        private String downloadLink;

        /**
         * Checks whether the book has a downloadable PDF version.
         *
         * @return True if available, false otherwise.
         */
        public boolean isAvailable() {
            return isAvailable;
        }

        /**
         * Retrieves the download link for the book's PDF version if available.
         *
         * @return The PDF download link or an empty string if unavailable.
         */
        public String getDownloadLink() {
            return (isAvailable && downloadLink != null) ? downloadLink : "";
        }
    }
}