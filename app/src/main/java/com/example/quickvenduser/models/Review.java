package com.example.quickvenduser.models;
public class Review {
    private String reviewerName;
    private String reviewText;
    private float rating;
    private long timestamp;  // Store timestamp as long

    // Constructor with long for timestamp
    public Review(String reviewerName, String reviewText, float rating, long timestamp) {
        this.reviewerName = reviewerName;
        this.reviewText = reviewText;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    // Getter methods
    public String getReviewerName() {
        return reviewerName;
    }

    public String getReviewText() {
        return reviewText;
    }

    public float getRating() {
        return rating;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Optional: Format timestamp to a user-friendly string
    public String getFormattedTimestamp() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy, hh:mm a", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }
}
