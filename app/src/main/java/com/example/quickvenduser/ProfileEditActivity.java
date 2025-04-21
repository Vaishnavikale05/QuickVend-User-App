package com.example.quickvenduser;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quickvenduser.fragments.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
public class ProfileEditActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "ProfileEditActivity";

    private ImageView profileImageView;
    private EditText fullNameEditText, emailEditText, phoneEditText, locationEditText;
    private Button saveProfileButton;
    private Bitmap selectedBitmap;
    private String encodedImage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));


        // Initialize views
        profileImageView = findViewById(R.id.profileImageView);
        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        locationEditText = findViewById(R.id.locationEditText);
        saveProfileButton = findViewById(R.id.saveProfileButton);

        // Handle image click to open image picker
        profileImageView.setOnClickListener(v -> openImageChooser());

        // Save button logic
        saveProfileButton.setOnClickListener(v -> saveUserProfile());
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                profileImageView.setImageBitmap(selectedBitmap);
                encodedImage = encodeImageToBase64(selectedBitmap);
            } catch (IOException e) {
                Log.e(TAG, "Error loading image", e);
            }
        }
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void saveUserProfile() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();  // Collected during profile edit
        String location = locationEditText.getText().toString().trim();

        // Basic validation
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current Firebase user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            Log.e("SaveProfile", "FirebaseAuth user is null. Cannot fetch UID.");
            return;
        }

        String uid = currentUser.getUid();
        Log.d("SaveProfile", "Current UID: " + uid);

        // Create the user data map
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("fullName", fullName);
        userMap.put("email", email);
        userMap.put("phone", phone);
        userMap.put("location", location);
        userMap.put("profileImage", encodedImage); // Base64-encoded image

        // Save to Firestore using UID
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("SaveProfile", "Profile saved successfully");
                    Toast.makeText(ProfileEditActivity.this, "Profile Saved!", Toast.LENGTH_SHORT).show();

                    // Navigate back to ProfileFragment or wherever appropriate
                    Intent intent = new Intent(ProfileEditActivity.this, MainActivity.class);  // Assuming MainActivity hosts the ProfileFragment
                    intent.putExtra("navigateToProfile", true); // Optional: tell MainActivity to open ProfileFragment
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("SaveProfile", "Error saving profile", e);
                    Toast.makeText(ProfileEditActivity.this, "Error saving profile", Toast.LENGTH_SHORT).show();
                });
    }
}
