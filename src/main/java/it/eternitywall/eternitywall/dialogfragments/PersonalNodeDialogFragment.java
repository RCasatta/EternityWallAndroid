package it.eternitywall.eternitywall.dialogfragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.net.InetAddresses;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import it.eternitywall.eternitywall.Preferences;
import it.eternitywall.eternitywall.R;

/**
 * Created by Riccardo Casatta @RCasatta on 18/12/15.
 */
public class PersonalNodeDialogFragment extends DialogFragment {
    private static final String TAG = "PersonalNodeDlg";

    private EditText txtNode;
    private ListView lstNodes;
    private TextView advise;
    private View view;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dlg_personalnodes, null);

        lstNodes = (ListView) view.findViewById(R.id.lstNodes);
        txtNode = (EditText) view.findViewById(R.id.txtNode);
        advise = (TextView) view.findViewById(R.id.advise);


        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final Set<String> stringSet = sharedPref.getStringSet(Preferences.NODES, new HashSet<String>());
        final List<String> stringList = new LinkedList<>();
        stringList.addAll(stringSet);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, stringList);
        lstNodes.setAdapter(arrayAdapter);

        adviseVisibility(stringSet);
        lstNodes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String current = stringList.get(position);
                Log.i(TAG, "deleting " + current);
                Set<String> newStringSet = new HashSet<String>(stringSet);
                newStringSet.remove(current);

                SharedPreferences.Editor edit = sharedPref.edit();
                edit.putStringSet(Preferences.NODES, newStringSet);
                edit.commit();
                arrayAdapter.remove(current);
                arrayAdapter.notifyDataSetChanged();
                adviseVisibility(newStringSet);

                return true;
            }
        });

        return new AlertDialog.Builder(getActivity())
                // Set Dialog Title
                .setTitle("Personal nodes")
                // Set Dialog Message : custom view
                .setView(view)
                // Positive button
                .setNeutralButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do something
                        String stringNode = txtNode.getText().toString();

                        boolean inetAddress = InetAddresses.isInetAddress(stringNode);
                        if (!inetAddress) {
                            Log.i(TAG, "Invalid address");
                            Toast.makeText(getActivity(), "Invalid address", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Set<String> newStringSet = new HashSet<String>(stringSet);
                        newStringSet.add(stringNode);

                        SharedPreferences.Editor edit = sharedPref.edit();
                        edit.putStringSet(Preferences.NODES, newStringSet);
                        edit.commit();

                        arrayAdapter.add(stringNode);
                        arrayAdapter.notifyDataSetChanged();
                        adviseVisibility(newStringSet);
                        txtNode.setText("");
                    }
                })
                // Negative Button
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,	int which) {
                        // Do nothing
                    }
                }).create();
    }


    private void adviseVisibility(Set<String> stringSet) {
        if(stringSet.size()>0) {
            advise.setVisibility(View.VISIBLE);
        } else {
            advise.setVisibility(View.GONE);
        }
    }
}
