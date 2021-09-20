package edu.ohiostate.whereami;

import android.app.Dialog;

import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MapsActivity extends SingleFragmentActivity {
    private static final int REQUEST_ERROR = 0;

    @Override
    protected Fragment createFragment() {
        return new MapsFragment();
    }

    /**
     * onResume() checks if Google Play services is available. If not, the Activity shows an
     * error dialog. Code from Chap. 33, Big Nerd Ranch Guide to Android Programming, 3rd ed.
     */
    @Override
    protected void onResume() {
        super.onResume();

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int errorCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (errorCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = apiAvailability.getErrorDialog(this, errorCode, REQUEST_ERROR,
					dialogInterface -> {
						// Quit the activity if Google Play services are not available.
						finish();
					});
			assert errorDialog != null;
			errorDialog.show();
        }
    }
}
