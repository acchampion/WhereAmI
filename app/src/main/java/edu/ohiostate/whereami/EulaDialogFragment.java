package edu.ohiostate.whereami;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

/**
 * Created by adamcchampion on 2014/09/22.
 */
public class EulaDialogFragment extends DialogFragment {

    public void setEulaAccepted() {
        Activity activity = requireActivity();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(getString(R.string.eula_accepted_key), true).apply();
	}

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.about_app)
                .setMessage(Utils.fromHtml(getString(R.string.eula)))
                .setPositiveButton(R.string.accept, (dialog, id) -> setEulaAccepted())
                .setNegativeButton(R.string.decline, (dialog, which) -> {
					dialog.cancel();
					System.exit(1);
				});
        return builder.create();
    }
}
