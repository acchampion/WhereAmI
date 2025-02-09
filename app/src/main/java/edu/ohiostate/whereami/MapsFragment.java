package edu.ohiostate.whereami;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;

/**
 * Class for Maps Fragment. Sources:
 * - Big Nerd Ranch Guide to Android Programming, Chap. 34
 * - Google: <a href="https://developers.google.com/maps/documentation/android-api/current-place-tutorial">...</a>
 * <p>
 * Created by adamcchampion on 2017/09/24.
 */

public class MapsFragment extends SupportMapFragment implements OnMyLocationButtonClickListener,
        OnMyLocationClickListener, OnMapReadyCallback, MenuProvider {
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Location mLocation;
    private LatLng mDefaultLocation = new LatLng(40.0, -83.0);

    private final String TAG = getClass().getSimpleName();


    private final ActivityResultLauncher<String> mActivityResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result) {
                    // We have permission, so show the user's location.
                    findLocation();
                    updateLocationUI();
                } else {
                    // The user denied location permission, so show them a message.
                    Log.e(TAG, "Error: location permission denied");

                    if (lacksLocationPermission()) {
                        Toast.makeText(requireActivity(), "Location permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            });


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(this);
    }

    @Override
    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpEula();
        findLocation();
    }

    @SuppressLint("MissingPermission")
    private void findLocation() {
        final Activity activity = requireActivity();
        LocationRequest.Builder builder = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L);
        LocationRequest request = builder.build();
        FusedLocationProviderClient locationProvider =
                LocationServices.getFusedLocationProviderClient(activity);

        if (hasLocationPermission()) {
            updateLocationUI();
            Task<Location> locationResult = locationProvider.getLastLocation();
            locationResult.addOnCompleteListener(activity, task -> {
                if (task.isSuccessful()) {
                    // Set the map's camera position to the current location of the device.
                    mLocation = task.getResult();
                    if (mLocation != null) {
                        mDefaultLocation = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 18));
                    }
                } else {
                    Log.d(TAG, "Current location is null. Using defaults.");
                    Log.e(TAG, "Exception: %s", task.getException());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 16));
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Activity activity = requireActivity();
        activity.invalidateOptionsMenu();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MenuHost menuHost = requireActivity();
        menuHost.removeMenuProvider(this);
    }

    private void setUpEula() {
        FragmentActivity activity = requireActivity();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean isEulaAccepted = sharedPrefs.getBoolean(getString(R.string.eula_accepted_key), false);
        if (!isEulaAccepted) {
            DialogFragment eulaDialogFragment = new EulaDialogFragment();
            eulaDialogFragment.show(activity.getSupportFragmentManager(), "eula");
        }
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.maps_menu, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_showcurrentlocation) {
            Log.d(TAG, "Showing current location");
            if (lacksLocationPermission()) {
                mActivityResult.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            } else {
                findLocation();
            }
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    private void updateLocationUI() {
        if (hasLocationPermission()) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (hasLocationPermission()) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
    }

    private boolean lacksLocationPermission() {
        final Activity activity = requireActivity();
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        return result != PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasLocationPermission() {
        return !lacksLocationPermission();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Context context = requireContext();
        Toast.makeText(context, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        if (hasLocationPermission()) {
            findLocation();
        }
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Context context = requireContext();
        Toast.makeText(context, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }
}
