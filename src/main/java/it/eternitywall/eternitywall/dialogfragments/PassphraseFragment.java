package it.eternitywall.eternitywall.dialogfragments;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.Preferences;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.bitcoin.Bitcoin;
import it.eternitywall.eternitywall.wallet.WalletObservable;

/**
 * Created by Riccardo Casatta @RCasatta on 08/12/15.
 */
public class PassphraseFragment extends DialogFragment {
    private static final String TAG = PassphraseFragment.class.toString();

    private View view;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        view = getActivity().getLayoutInflater().inflate(R.layout.dlg_passphrase, null);

        // set the custom dialog components - text, image and button
        TextView textView = (TextView) view.findViewById(R.id.textView);
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String passphrase = sharedPref.getString(Preferences.PASSPHRASE, null);
        if (passphrase != null) {
            textView.setText(passphrase);
        }

            return new AlertDialog.Builder(getActivity())
                // Set Dialog Title
                .setTitle("Passphrase")
                // Set Dialog Message : custom view
                .setView(view)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .create();
    }

}
