package com.example.quickvenduser.models;

public class FoodCategory {
    private String name;
    private int iconResId;  // Store the drawable resource ID for the category icon

    public FoodCategory(String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconResId() {
        return iconResId; // This is the method you need
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }
}
