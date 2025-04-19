package com.example.quickvenduser.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.quickvenduser.R;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class VendorAdapter extends RecyclerView.Adapter<VendorAdapter.VendorViewHolder> {

    private List<QueryDocumentSnapshot> vendorList;

    public VendorAdapter(List<QueryDocumentSnapshot> vendorList) {
        this.vendorList = vendorList;
    }

    @Override
    public VendorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vendor, parent, false);
        return new VendorViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(VendorViewHolder holder, int position) {
        QueryDocumentSnapshot vendor = vendorList.get(position);

        String vendorName = vendor.getString("stallName");
        String vendorAddress = vendor.getString("address"); // Get the string address field
        if (vendorAddress == null) vendorAddress = "Location unavailable";

        holder.vendorNameTextView.setText(vendorName);
        holder.vendorLocationTextView.setText(vendorAddress); // Show the address instead of lat/lng
    }

    @Override
    public int getItemCount() {
        return vendorList.size();
    }

    public static class VendorViewHolder extends RecyclerView.ViewHolder {
        public TextView vendorNameTextView;
        public TextView vendorLocationTextView;

        public VendorViewHolder(View itemView) {
            super(itemView);
            vendorNameTextView = itemView.findViewById(R.id.vendorName);
            vendorLocationTextView = itemView.findViewById(R.id.vendorLocationTextView);
        }
    }
}
