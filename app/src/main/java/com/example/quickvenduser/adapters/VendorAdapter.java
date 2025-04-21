package com.example.quickvenduser.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.quickvenduser.R;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.Map;

public class VendorAdapter extends RecyclerView.Adapter<VendorAdapter.VendorViewHolder> {

    public interface OnVendorClickListener {
        void onVendorClick(QueryDocumentSnapshot vendor);
    }

    private List<QueryDocumentSnapshot> vendorList;
    private OnVendorClickListener listener;

    public VendorAdapter(List<QueryDocumentSnapshot> vendorList, OnVendorClickListener listener) {
        this.vendorList = vendorList;
        this.listener = listener;
    }

    @Override
    public VendorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vendor, parent, false);
        return new VendorViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(VendorViewHolder holder, int position) {
        QueryDocumentSnapshot vendor = vendorList.get(position);

        // Get values from Firestore and handle possible nulls
        String name = vendor.getString("stallName");
        String address = vendor.getString("address");
        String contact = vendor.getString("contactNumber");
        String category = vendor.getString("category");
        Boolean isOnline = vendor.getBoolean("isOnline");
        String profileImageEncoded = vendor.getString("profileImageEncrypted");

        // Set profile image
        if (profileImageEncoded != null) {
            try {
                byte[] decodedBytes = Base64.decode(profileImageEncoded, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.vendorProfileImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace(); // Optional: Log error if profile image decoding fails
            }
        }

        // Set text views with default values if data is null
        holder.vendorNameTextView.setText(name != null ? name : "No Name");
        holder.vendorLocationTextView.setText(address != null ? address : "Location unavailable");
        holder.vendorContact.setText("Contact: " + (contact != null ? contact : "N/A"));
        holder.vendorCategory.setText("Category: " + (category != null ? category : "N/A"));

        // Set online status
        if (isOnline != null && isOnline) {
            holder.vendorOnlineStatus.setText("Online");
            holder.vendorOnlineStatus.setTextColor(0xFF228B22); // Forest Green
        } else {
            holder.vendorOnlineStatus.setText("Offline");
            holder.vendorOnlineStatus.setTextColor(0xFFB22222); // Firebrick Red
        }

        // Show menu items
        StringBuilder menuBuilder = new StringBuilder("Menu:");
        List<Map<String, Object>> menuItems = (List<Map<String, Object>>) vendor.get("menuItems");
        if (menuItems != null && !menuItems.isEmpty()) {
            for (Map<String, Object> item : menuItems) {
                String itemName = (String) item.get("name");
                Object price = item.get("price");
                menuBuilder.append("\n• ").append(itemName).append(": ₹").append(price);
            }
        } else {
            menuBuilder.append(" No items listed");
        }
        holder.vendorMenuItemsTextView.setText(menuBuilder.toString());

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onVendorClick(vendorList.get(pos));
            }
        });
    }

    @Override
    public int getItemCount() {
        return vendorList.size();
    }

    public static class VendorViewHolder extends RecyclerView.ViewHolder {
        public ImageView vendorProfileImage;
        public TextView vendorNameTextView, vendorLocationTextView, vendorContact,
                vendorCategory, vendorOnlineStatus, vendorMenuItemsTextView;

        public VendorViewHolder(View itemView) {
            super(itemView);
            vendorProfileImage = itemView.findViewById(R.id.vendorProfileImage);
            vendorNameTextView = itemView.findViewById(R.id.vendorName);
            vendorLocationTextView = itemView.findViewById(R.id.vendorLocationTextView);
            vendorContact = itemView.findViewById(R.id.vendorContact);
            vendorCategory = itemView.findViewById(R.id.vendorCategory);
            vendorOnlineStatus = itemView.findViewById(R.id.vendorOnlineStatus);
            vendorMenuItemsTextView = itemView.findViewById(R.id.vendorMenuItemsTextView);
        }
    }
}
