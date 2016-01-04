package it.eternitywall.eternitywall.dialogfragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.net.InetAddresses;
import com.google.common.util.concurrent.Runnables;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import it.eternitywall.eternitywall.Preferences;
import it.eternitywall.eternitywall.R;

/**
 * Created by Riccardo Casatta @RCasatta on 18/12/15.
 */
public class PinAlertDialogFragment extends DialogFragment {

    public static PinAlertDialogFragment newInstance(int title) {
        PinAlertDialogFragment frag = new PinAlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

    //View view;
    Runnable mRunnable=null;
    View view=null;

    public void setPositive(Runnable runnable){
        this.mRunnable=runnable;
    }

    public String getPin(){
        if(view==null)
            return null;
        else
            return ((EditText)view.findViewById(R.id.editText)).getText().toString();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");
        view = getActivity().getLayoutInflater().inflate(R.layout.dlg_pin, null);

        return new AlertDialog.Builder(getActivity())
                // Set Dialog Title
                .setTitle(title)
                // Set Dialog Message : custom view
                .setView(view)
                // Positive button
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do something
                        if (mRunnable!=null)
                            mRunnable.run();
                    }
                })
                // Negative Button
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,	int which) {
                        // Do nothing
                    }
                }).create();
    }


}
