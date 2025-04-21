package com.example.quickvenduser;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quickvenduser.R;

public class VendorDetailsActivity extends AppCompatActivity {

    private TextView stallName, category, phone, location;
    private ImageView vendorImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_details);
        getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));

        vendorImage = findViewById(R.id.vendorImage);
        stallName = findViewById(R.id.stallName);
        category = findViewById(R.id.category);
        phone = findViewById(R.id.phone);
        location = findViewById(R.id.location);

        // Retrieve data from intent
        String name = getIntent().getStringExtra("stallName");
        String cat = getIntent().getStringExtra("category");
        String type = getIntent().getStringExtra("foodType");
        String phoneNum = getIntent().getStringExtra("phone");
        String emailId = getIntent().getStringExtra("email");
        String loc = getIntent().getStringExtra("location");
        String profileImageEncoded = getIntent().getStringExtra("profileImageEncrypted");

        // Set values to views
        stallName.setText(name != null ? name : "N/A");
        category.setText("Category: " + (cat != null ? cat : "N/A"));
        phone.setText("Phone: " + (phoneNum != null ? phoneNum : "N/A"));
        location.setText("Address: " + (loc != null ? loc : "N/A"));

        if (profileImageEncoded != null) {
            try {
                byte[] decodedBytes = Base64.decode(profileImageEncoded, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                vendorImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
