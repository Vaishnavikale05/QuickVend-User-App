package com.example.quickvenduser.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationUtils {

    public interface LocationCallback {
        void onLocationResult(Location location);
    }

    @SuppressLint("MissingPermission")
    public static void getCurrentLocation(Activity activity, LocationCallback callback) {
        // Check if we have the location permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // If permission is not granted, call the callback with null
                callback.onLocationResult(null);
                return;
            }
        }

        // If permission is granted, get the location
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocationResult(location);
                    } else {
                        callback.onLocationResult(null);
                    }
                });
    }
}
