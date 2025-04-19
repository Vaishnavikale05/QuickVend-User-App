package com.example.quickvenduser.fragments;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.quickvenduser.R;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.api.IMapController;

public class MapFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int REQUEST_CHECK_SETTINGS = 2001;

    private MapView mapView;
    private IMapController mapController;
    private FusedLocationProviderClient fusedLocationClient;
    private org.osmdroid.util.GeoPoint userLocation;
    private Marker userMarker;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_map_fragment, container, false);
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        mapView = view.findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);
        mapController = mapView.getController();
        mapController.setZoom(15.0);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        checkLocationPermission();

        return view;
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            checkLocationEnabled();
        }
    }

    private void checkLocationEnabled() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(requireActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(requireActivity(), locationSettingsResponse -> getUserLocation());

        task.addOnFailureListener(requireActivity(), e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    Log.e("MapFragment", "Failed to show location dialog");
                }
            }
        });
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLocation = new org.osmdroid.util.GeoPoint(location.getLatitude(), location.getLongitude());
                        mapController.setCenter(userLocation);
                        drawUserMarker(userLocation);
                        drawRadiusCircle(userLocation, 5000);
                        fetchVendorsWithinRange(userLocation, 5000);
                    } else {
                        Log.e("MapFragment", "Location is null");
                    }
                })
                .addOnFailureListener(e -> Log.e("MapFragment", "Failed to get user location", e));
    }

    private void drawUserMarker(org.osmdroid.util.GeoPoint location) {
        if (userMarker != null) {
            mapView.getOverlays().remove(userMarker);
        }

        userMarker = new Marker(mapView);
        userMarker.setPosition(location);
        userMarker.setTitle("Your Location");
        userMarker.setIcon(getResources().getDrawable(R.drawable.ic_user_marker)); // Optional custom icon
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(userMarker);
    }

    private void drawRadiusCircle(org.osmdroid.util.GeoPoint center, double radiusMeters) {
        Polygon circle = new Polygon();
        circle.setPoints(Polygon.pointsAsCircle(center, radiusMeters));
        circle.setFillColor(0x40FFA500); // Translucent orange
        circle.setStrokeColor(0xFFFFA500); // Solid orange
        circle.setStrokeWidth(3.0f);
        mapView.getOverlayManager().add(circle);
        mapView.invalidate();
    }

    private void fetchVendorsWithinRange(org.osmdroid.util.GeoPoint userLocation, double maxDistanceMeters) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("vendors").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String stallName = document.getString("stallName");
                GeoPoint location = document.getGeoPoint("location");
                String vendorId = document.getId();  // Assuming the document ID is the vendor ID

                if (location != null) {
                    double distance = calculateDistance(
                            userLocation.getLatitude(), userLocation.getLongitude(),
                            location.getLatitude(), location.getLongitude());

                    if (distance <= maxDistanceMeters) {
                        Marker vendorMarker = new Marker(mapView);
                        vendorMarker.setPosition(new org.osmdroid.util.GeoPoint(location.getLatitude(), location.getLongitude()));
                        vendorMarker.setTitle(stallName != null ? stallName : "Vendor"); // Set the stall name as the title
                        vendorMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        vendorMarker.setIcon(getResources().getDrawable(R.drawable.ic_vendor_marker)); // Optional custom icon

                        // Set the click listener to show the BottomSheet
                        vendorMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker, MapView mapView) {
                                // Show the Vendor Details in BottomSheet
                                showVendorDetailsBottomSheet(vendorId, stallName); // Pass vendorId and stallName

                                // Return false to allow the default marker behavior (zoom and highlight)
                                return false;
                            }
                        });

                        mapView.getOverlays().add(vendorMarker);
                    }
                } else {
                    Log.e("MapFragment", "Invalid location data for vendor: " + stallName);
                }
            }
            mapView.invalidate();
        }).addOnFailureListener(e -> Log.e("MapFragment", "Error fetching vendor data", e));
    }

    private void showVendorDetailsBottomSheet(String vendorId, String stallName) {
        // Create an instance of the VendorDetailsBottomSheet
        VendorDetailsBottomSheet bottomSheet = VendorDetailsBottomSheet.newInstance(vendorId, stallName);

        // Show the bottom sheet
        bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
    }


    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] result = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, result);
        return result[0];
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkLocationEnabled();
        } else {
            Log.e("MapFragment", "Location permission denied");
        }
    }
}
