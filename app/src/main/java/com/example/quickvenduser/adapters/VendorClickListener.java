package com.example.quickvenduser.adapters;

import com.google.firebase.firestore.QueryDocumentSnapshot;

public interface VendorClickListener {
    void onVendorClick(QueryDocumentSnapshot vendorSnapshot);
}
