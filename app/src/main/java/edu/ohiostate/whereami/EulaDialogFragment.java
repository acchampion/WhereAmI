package edu.ohiostate.whereami;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by adamcchampion on 2014/09/22.
 */
public class EulaDialogFragment extends DialogFragment {

    public void setEulaAccepted() {
        Activity activity = getActivity();
        if (activity != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(getString(R.string.eula_accepted_key), true).apply();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.about_app)
                .setMessage(Utils.fromHtml(getString(R.string.eula)))
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        setEulaAccepted();
                    }
                })
                .setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Activity activity = getActivity();
                        if (activity != null) {
                            activity.finish();
                        }
                    }
                });
        return builder.create();
    }
}
