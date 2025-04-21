package com.example.quickvenduser.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.quickvenduser.LoginActivity;
import com.example.quickvenduser.ProfileEditActivity;
import com.example.quickvenduser.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private ImageView profileImageView;
    private TextView fullNameTextView, emailTextView, phoneTextView, locationTextView;
    private Button editProfileButton;
    private ImageView logoutButton;

    private String userUid;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile_fragment, container, false);
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        requireActivity().getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // clear backstack
            startActivity(intent);
        });

        // Initialize views
        profileImageView = view.findViewById(R.id.profileImage);
        fullNameTextView = view.findViewById(R.id.fullNameText);
        emailTextView = view.findViewById(R.id.emailText);
        phoneTextView = view.findViewById(R.id.phoneNumberText);
        locationTextView = view.findViewById(R.id.currentLocationText);
        editProfileButton = view.findViewById(R.id.editProfileButton);

        // Get the current user UID from Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userUid = currentUser.getUid();

            // Fetch user data from Firestore
            FirebaseFirestore.getInstance().collection("users")
                    .document(userUid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            String email = documentSnapshot.getString("email");
                            String phone = documentSnapshot.getString("phone");
                            String location = documentSnapshot.getString("location");
                            String encodedImage = documentSnapshot.getString("profileImage");

                            // Set in UI as needed
                            fullNameTextView.setText(fullName);
                            emailTextView.setText(email);
                            phoneTextView.setText(phone);
                            locationTextView.setText(location);

                            // Decode image
                            if (encodedImage != null && !encodedImage.isEmpty()) {
                                byte[] imageBytes = Base64.decode(encodedImage, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                profileImageView.setImageBitmap(getCroppedBitmap(bitmap)); // Use this method to crop the image
                            }
                        } else {
                            Toast.makeText(getContext(), "Profile not found!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error fetching profile", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Handle case where the user is not logged in
            Toast.makeText(getContext(), "User is not logged in", Toast.LENGTH_SHORT).show();
        }

        // Set up the Edit Profile button to navigate to ProfileEditActivity
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileEditActivity.class);
            startActivity(intent);  // Start the ProfileEditActivity to edit the profile
        });

        return view;
    }

    // Helper function to crop the image into a circle
    private Bitmap getCroppedBitmap(Bitmap bitmap) {
        // Create a square bitmap (as circular images require a square shape)
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        int x = (bitmap.getWidth() - size) / 2;
        int y = (bitmap.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(bitmap, x, y, size, size);
        Bitmap output = Bitmap.createBitmap(squaredBitmap.getWidth(), squaredBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final android.graphics.Rect rect = new android.graphics.Rect(0, 0, squaredBitmap.getWidth(), squaredBitmap.getHeight());
        final android.graphics.RectF rectF = new android.graphics.RectF(rect);

        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        canvas.drawARGB(0, 0, 0, 0);  // Transparent background
        canvas.drawRoundRect(rectF, squaredBitmap.getWidth() / 2, squaredBitmap.getHeight() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(squaredBitmap, rect, rect, paint);

        return output;
    }
}
