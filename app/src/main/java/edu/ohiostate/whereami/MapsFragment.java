package edu.ohiostate.whereami;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Class for Maps Fragment. Sources:
 * - Big Nerd Ranch Guide to Android Programming, Chap. 34
 * - Google: https://developers.google.com/maps/documentation/android-api/current-place-tutorial
 * <p>
 * Created by adamcchampion on 2017/09/24.
 */

public class MapsFragment extends SupportMapFragment implements OnMapReadyCallback {
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mApiClient;
    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private Location mLocation;
    private LatLng mDefaultLocation;
    private static final int REQUEST_LOCATION_PERMISSIONS = 0;
    private boolean mLocationPermissionGranted = false;

    private final String TAG = getClass().getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Activity activity = requireActivity();
		mApiClient = new GoogleApiClient.Builder(activity)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
					@Override
					public void onConnected(@Nullable Bundle bundle) {
						Activity theActivity = requireActivity();
						theActivity.invalidateOptionsMenu();
					}

					@Override
					public void onConnectionSuspended(int i) {
						Log.d(TAG, "GoogleAPIClient connection suspended");
					}
				})
				.build();
		getMapAsync(this);
	}

    @Override
    public void onResume() {
        super.onResume();
        setUpEula();
        findLocation();
    }

    @SuppressLint("MissingPermission")
    private void findLocation() {
        updateLocationUI();
        if (hasLocationPermission()) {
        	final Activity activity = requireActivity();
			mDefaultLocation = new LatLng(40.0, -83.0);
			LocationRequest locationRequest = LocationRequest.create();
			locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			locationRequest.setNumUpdates(1);
			locationRequest.setInterval(0);
			FusedLocationProviderClient locationProvider =
					LocationServices.getFusedLocationProviderClient(activity);
			Task<Location> locationResult = locationProvider.getLastLocation();
			locationResult.addOnCompleteListener(activity, new OnCompleteListener<Location>() {
				@Override
				public void onComplete(@NonNull Task task) {
					if (task.isSuccessful()) {
						// Set the map's camera position to the current location of the device.
						mLocation = (Location) task.getResult();
						if (mLocation != null) {
							mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
									new LatLng(mLocation.getLatitude(),
											mLocation.getLongitude()), 16));
						}
					} else {
						Log.d(TAG, "Current location is null. Using defaults.");
						Log.e(TAG, "Exception: %s", task.getException());
						mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 16));
						mMap.getUiSettings().setMyLocationButtonEnabled(false);
					}
				}
			});
		} else {
            requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSIONS);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Activity activity = requireActivity();
		activity.invalidateOptionsMenu();
		mApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mApiClient.disconnect();
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.maps_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_showcurrentlocation) {
            Log.d(TAG, "Showing current location");
            if (hasLocationPermission()) {
                findLocation();
            } else {
                requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSIONS);
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        if (requestCode == REQUEST_LOCATION_PERMISSIONS) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLocation = null;
                requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSIONS);
            }
        } catch (SecurityException e) {
        	String msg = e.getMessage();
        	if (msg != null) {
				Log.e("Exception: %s", msg);
			}
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(new LatLng(40.0, -83.0))
                .title("Ohio State University"));
        if (hasLocationPermission()) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
    }

    private boolean hasLocationPermission() {
        Activity activity = requireActivity();
        int result;
		result = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
		return result == PackageManager.PERMISSION_GRANTED;
    }
}
