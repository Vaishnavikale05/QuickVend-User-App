package com.example.quickvenduser.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.quickvenduser.R;
import com.example.quickvenduser.models.Review;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);

        // Set review details in the views
        holder.reviewerNameTextView.setText(review.getReviewerName());
        holder.reviewTextTextView.setText(review.getReviewText());
        holder.reviewRatingBar.setRating(review.getRating());

        // Check if the timestamp is valid and format it
        long timestamp = review.getTimestamp();
        if (timestamp != 0) { // Make sure the timestamp is not zero or invalid
            String formattedTimestamp = formatTimestamp(timestamp);
            holder.timestampTextView.setText(formattedTimestamp);
        } else {
            holder.timestampTextView.setText("Unknown time"); // Or any default text
        }
    }


    @Override
    public int getItemCount() {
        return reviews.size();
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(timestamp);
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView reviewerNameTextView;
        TextView reviewTextTextView;
        RatingBar reviewRatingBar;
        TextView timestampTextView; // Add timestamp TextView

        public ReviewViewHolder(View itemView) {
            super(itemView);
            reviewerNameTextView = itemView.findViewById(R.id.reviewerName);
            reviewTextTextView = itemView.findViewById(R.id.reviewText);
            reviewRatingBar = itemView.findViewById(R.id.reviewRating);
            timestampTextView = itemView.findViewById(R.id.timestamp); // Initialize timestamp TextView
        }
    }
}
