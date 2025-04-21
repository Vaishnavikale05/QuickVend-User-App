package com.example.quickvenduser.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quickvenduser.R;
import com.example.quickvenduser.adapters.ReviewAdapter;
import com.example.quickvenduser.models.Review;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public class VendorDetailsBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "VendorDetails";

    private TextView vendorNameTextView;
    private TextView vendorCategoryTextView;
    private RatingBar vendorRatingBar;
    private RecyclerView vendorReviewsRecyclerView;
    private EditText reviewTextEditText;
    private RatingBar reviewRatingBar;

    private String vendorId;
    private String stallName;

    public static VendorDetailsBottomSheet newInstance(String vendorId, String stallName) {
        VendorDetailsBottomSheet fragment = new VendorDetailsBottomSheet();
        Bundle args = new Bundle();
        args.putString("vendorId", vendorId);
        args.putString("stallName", stallName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_details, container, false);
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        requireActivity().getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Retrieve vendorId and stallName from arguments
        if (getArguments() != null) {
            vendorId = getArguments().getString("vendorId");
            stallName = getArguments().getString("stallName");
        }

        vendorNameTextView = view.findViewById(R.id.vendorName);
        vendorCategoryTextView = view.findViewById(R.id.vendorCategory);
        vendorRatingBar = view.findViewById(R.id.vendorRating);
        vendorReviewsRecyclerView = view.findViewById(R.id.vendorReviews);
        reviewTextEditText = view.findViewById(R.id.reviewEditText);
        reviewRatingBar = view.findViewById(R.id.reviewRatingBar);

        vendorNameTextView.setText(stallName);

        fetchVendorDetails(vendorId);

        view.findViewById(R.id.submitReviewButton).setOnClickListener(v -> submitReview());

        return view;
    }

    private void fetchVendorDetails(String vendorId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("vendors").document(vendorId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String category = documentSnapshot.getString("category");
                        Double rating = documentSnapshot.getDouble("rating");

                        vendorCategoryTextView.setText(category != null ? category : "Category not available");
                        vendorRatingBar.setRating(rating != null ? rating.floatValue() : 0);

                        fetchVendorReviews(vendorId);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch vendor details", e));
    }

    private void fetchVendorReviews(String vendorId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("vendors").document(vendorId).collection("reviews").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Review> reviews = new ArrayList<>();
                        float totalRating = 0;
                        int reviewCount = 0;

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String reviewText = document.getString("reviewText");
                            String reviewerName = document.getString("reviewerName");
                            float rating = document.getDouble("rating").floatValue();

                            // Check the type of 'timestamp' field
                            Object timestampObj = document.get("timestamp");
                            long timestampLong = 0;
                            if (timestampObj instanceof com.google.firebase.Timestamp) {
                                timestampLong = ((com.google.firebase.Timestamp) timestampObj).getSeconds() * 1000;
                            } else if (timestampObj instanceof Long) {
                                timestampLong = (Long) timestampObj;
                            }

                            // Format timestamp into a user-friendly string
                            String formattedTime = formatTimestamp(timestampLong);

                            reviews.add(new Review(reviewerName, reviewText, rating, timestampLong));

                            totalRating += rating;
                            reviewCount++;
                        }

                        // Calculate average rating
                        float averageRating = reviewCount > 0 ? totalRating / reviewCount : 0;

                        // Set the average rating in the RatingBar
                        vendorRatingBar.setRating(averageRating);

                        // Set up RecyclerView for displaying reviews
                        setupReviewsRecyclerView(reviews);
                    }
                })
                .addOnFailureListener(e -> Log.e("VendorDetails", "Failed to fetch reviews", e));
    }


    private String formatTimestamp(long timestamp) {
        if (timestamp == 0) {
            return "Unknown time"; // Handle null or missing timestamps
        }

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy, hh:mm a", java.util.Locale.getDefault());
            java.util.Date resultdate = new java.util.Date(timestamp);
            return sdf.format(resultdate);
        } catch (Exception e) {
            Log.e("FormatTimestamp", "Error formatting timestamp", e);
            return "Invalid time";
        }
    }

    private void setupReviewsRecyclerView(List<Review> reviews) {
        ReviewAdapter adapter = new ReviewAdapter(reviews);
        vendorReviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        vendorReviewsRecyclerView.setAdapter(adapter);
    }

    private void submitReview() {
        String reviewText = reviewTextEditText.getText().toString().trim();
        float rating = reviewRatingBar.getRating();

        if (reviewText.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a review text.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rating == 0) {
            Toast.makeText(getContext(), "Please give a rating.", Toast.LENGTH_SHORT).show();
            return;
        }

        fetchUserFullNameAndSubmitReview(reviewText, rating);
    }

    private void fetchUserFullNameAndSubmitReview(String reviewText, float rating) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (uid == null) {
            Toast.makeText(getContext(), "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("fullName");
                        if (userName != null) {
                            submitOrUpdateReview(uid, userName, reviewText, rating);
                        } else {
                            Toast.makeText(getContext(), "User name not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "User profile not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch user profile.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error fetching user profile", e);
                });
    }

    private void submitOrUpdateReview(String userId, String userName, String reviewText, float rating) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the current timestamp (in milliseconds)
        long timestamp = System.currentTimeMillis();  // This will be in milliseconds

        DocumentReference reviewRef = db.collection("vendors")
                .document(vendorId)
                .collection("reviews")
                .document(userId);  // Use UID as document ID

        Review review = new Review(userName, reviewText, rating, timestamp);

        reviewRef.set(review) // Creates or overwrites existing review by this user
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Review submitted successfully.", Toast.LENGTH_SHORT).show();

                    // Reload reviews after submission
                    fetchVendorReviews(vendorId);

                    // Clear fields
                    reviewTextEditText.setText("");
                    reviewRatingBar.setRating(0);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to submit review.", Toast.LENGTH_SHORT).show();
                    Log.e("ReviewSubmission", "Error submitting review", e);
                });
    }
}
