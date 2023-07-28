package edu.ohiostate.whereami;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.MapsInitializer.Renderer;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;


public class MapsApplication extends Application implements OnMapsSdkInitializedCallback {

	private final static String TAG = MapsApplication.class.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();
		MapsInitializer.initialize(getApplicationContext(), Renderer.LATEST, this);
	}

	@Override
	public void onMapsSdkInitialized(@NonNull Renderer renderer) {
		switch (renderer) {
			case LATEST -> Log.d(TAG, "The latest version of the renderer is used.");
			case LEGACY -> Log.d(TAG, "The legacy version of the renderer is used.");
		}
	}
}
