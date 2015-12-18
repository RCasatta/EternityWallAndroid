package it.eternitywall.eternitywall.dialogfragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.eternitywall.eternitywall.R;

/**
 * Created by Riccardo Casatta @RCasatta on 18/12/15.
 */
public class PersonalNodeDialogFragment extends DialogFragment {
    private static final String TAG = "PersonalNodeDialogFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dlg_personalnodes, container, false);
        getDialog().setTitle(getString(R.string.app_name));


        return view;

    }
}
