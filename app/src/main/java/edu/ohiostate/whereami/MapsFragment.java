package edu.ohiostate.whereami;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

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
	private Location mLocation;
	private LatLng mDefaultLocation;

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
		final Activity activity = requireActivity();
		mDefaultLocation = new LatLng(40.0, -83.0);
		LocationRequest locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setNumUpdates(1);
		locationRequest.setInterval(0);
		FusedLocationProviderClient locationProvider =
				LocationServices.getFusedLocationProviderClient(activity);

		if (hasLocationPermission()) {
			updateLocationUI();
			Task<Location> locationResult = locationProvider.getLastLocation();
			locationResult.addOnCompleteListener(activity, task -> {
				if (task.isSuccessful()) {
					// Set the map's camera position to the current location of the device.
					mLocation = (Location) task.getResult();
					if (mLocation != null) {
						mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
								new LatLng(mLocation.getLatitude(),
										mLocation.getLongitude()), 18));
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
			if (lacksLocationPermission()) {
				mActivityResult.launch(Manifest.permission.ACCESS_FINE_LOCATION);
			} else {
				findLocation();
			}
		}
		return true;
	}


	@SuppressLint("MissingPermission")
	@RequiresApi(api = Build.VERSION_CODES.M)
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
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		mMap.addMarker(new MarkerOptions().position(new LatLng(40.0, -83.0))
				.title("Ohio State University"));
		mMap.addMarker(new MarkerOptions().position(new LatLng(37.7749, -122.14494))
				.title("San Francisco Bay Area, CA"));
		if (hasLocationPermission()) {
			mMap.setMyLocationEnabled(true);
			mMap.getUiSettings().setMyLocationButtonEnabled(true);
		}
		mMap.setBuildingsEnabled(true);
		mMap.setIndoorEnabled(true);
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	private boolean lacksLocationPermission() {
		final Activity activity = requireActivity();
		int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
		return result != PackageManager.PERMISSION_GRANTED;
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	private boolean hasLocationPermission() {
		return !lacksLocationPermission();
	}
}
