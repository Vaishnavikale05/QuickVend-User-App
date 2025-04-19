package com.example.quickvenduser.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quickvenduser.R;
import com.example.quickvenduser.adapters.FoodCategoryAdapter;
import com.example.quickvenduser.adapters.FilterAdapter;
import com.example.quickvenduser.adapters.VendorAdapter;
import com.example.quickvenduser.models.FoodCategory;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView foodCategoriesRecyclerView;
    private RecyclerView filtersRecyclerView;
    private RecyclerView vendorListRecyclerView;
    private EditText searchBar;

    private FoodCategoryAdapter foodCategoryAdapter;
    private FilterAdapter filterAdapter;
    private VendorAdapter vendorAdapter;

    private List<FoodCategory> foodCategoryList;
    private List<String> filterList;
    private List<QueryDocumentSnapshot> vendorList;
    private FirebaseFirestore firestore;

    private String selectedCategory = "All";
    private String selectedFilter = "All";
    private String searchQuery = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fragment_home, container, false);

        firestore = FirebaseFirestore.getInstance();

        searchBar = view.findViewById(R.id.search_bar);
        foodCategoriesRecyclerView = view.findViewById(R.id.food_categories_recyclerview);
        filtersRecyclerView = view.findViewById(R.id.filters_recyclerview);
        vendorListRecyclerView = view.findViewById(R.id.vendor_list_recyclerview);

        foodCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        filtersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        vendorListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        foodCategoryList = new ArrayList<>();
        filterList = new ArrayList<>();
        vendorList = new ArrayList<>();

        foodCategoryAdapter = new FoodCategoryAdapter(getContext(), foodCategoryList, category -> {
            selectedCategory = category.getName();
            applyFilters();
        });

        filterAdapter = new FilterAdapter(getContext(), filterList, filter -> {
            selectedFilter = filter;
            applyFilters();
        });

        vendorAdapter = new VendorAdapter(vendorList);

        foodCategoriesRecyclerView.setAdapter(foodCategoryAdapter);
        filtersRecyclerView.setAdapter(filterAdapter);
        vendorListRecyclerView.setAdapter(vendorAdapter);

        fetchFoodCategories();
        fetchFilters();
        fetchVendorsFromFirestore();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void fetchFoodCategories() {
        foodCategoryList.clear();
        foodCategoryList.add(new FoodCategory("Burgers", R.drawable.ic_burger));
        foodCategoryList.add(new FoodCategory("Pizza", R.drawable.ic_pizza));
        foodCategoryList.add(new FoodCategory("Brevrages", R.drawable.ic_brevrages));
        foodCategoryList.add(new FoodCategory("Sushi", R.drawable.ic_sushi));
        foodCategoryList.add(new FoodCategory("Momos", R.drawable.ic_momos));
        foodCategoryList.add(new FoodCategory("Rice Plates", R.drawable.ic_curry));
        foodCategoryList.add(new FoodCategory("Sandwich", R.drawable.ic_sandwich));
        foodCategoryList.add(new FoodCategory("Rolls", R.drawable.ic_rolls));
        foodCategoryAdapter.notifyDataSetChanged();
    }

    private void fetchFilters() {
        filterList.clear();
        filterList.add("All");
        filterList.add("Vegetarian");
        filterList.add("Non-Vegetarian");
        filterList.add("Vegan");
        filterAdapter.notifyDataSetChanged();
    }

    private void fetchVendorsFromFirestore() {
        firestore.collection("vendors")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    vendorList.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        vendorList.add(documentSnapshot);
                    }
                    vendorAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void applyFilters() {
        firestore.collection("vendors")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    vendorList.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String vendorCategory = documentSnapshot.getString("category");
                        String vendorFilter = documentSnapshot.getString("foodType");
                        String vendorName = documentSnapshot.getString("stallName");

                        boolean matchesCategory = selectedCategory == null || selectedCategory.equalsIgnoreCase("All")
                                || (vendorCategory != null && vendorCategory.equalsIgnoreCase(selectedCategory));

                        boolean matchesFilter = selectedFilter == null || selectedFilter.equalsIgnoreCase("All")
                                || (vendorFilter != null && vendorFilter.equalsIgnoreCase(selectedFilter));

                        boolean matchesSearch = searchQuery.isEmpty()
                                || (vendorName != null && vendorName.toLowerCase().contains(searchQuery.toLowerCase()));

                        if (matchesCategory && matchesFilter && matchesSearch) {
                            vendorList.add(documentSnapshot);
                        }
                    }
                    vendorAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }
}
