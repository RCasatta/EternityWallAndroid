package it.eternitywall.eternitywall.dialogfragments;

import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.base.Optional;

import it.eternitywall.eternitywall.EmailValidation;
import it.eternitywall.eternitywall.Http;
import it.eternitywall.eternitywall.Preferences;
import it.eternitywall.eternitywall.R;

/**
 * Created by federicoserrelli on 02/09/15.
 */
public class NotifyDialogFragment extends DialogFragment {

    private EditText txtEmail;
    private CheckBox ckOne, ckTwo;
    private String address;
    private View view;

    public void setAddress(String address) {
        this.address = address;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dlg_notifyme, null);

        txtEmail = (EditText) view.findViewById(R.id.txtEmail);
        ckOne = (CheckBox) view.findViewById(R.id.ckOne);
        ckTwo = (CheckBox) view.findViewById(R.id.ckTwo);


        return new AlertDialog.Builder(getActivity())
                // Set Dialog Title
                .setTitle(getResources().getString(R.string.app_name))
                // Set Dialog Message : custom view
                .setView(view)
                // Positive button
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do something
                        positive();
                    }
                })
                // Negative Button
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,	int which) {
                        // Do nothing
                    }
                }).create();
    }

    void positive(){
        if(txtEmail.getText().length() == 0 || !EmailValidation.isValid(txtEmail.getText().toString())) {
            Toast.makeText(getActivity(), getActivity().getString(R.string.err_email_address), Toast.LENGTH_SHORT).show();
            return;
        }

        //save the settings
        SharedPreferences sp = getActivity().getSharedPreferences(Application.class.getSimpleName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Preferences.EMAIL, txtEmail.getText().toString());
        editor.putBoolean(Preferences.CHK_ONE, ckOne.isChecked());
        editor.putBoolean(Preferences.CHK_TWO, ckTwo.isChecked());
        editor.commit();

        //something wrong!! address should always be set...
        if(address == null) {
            Toast.makeText(getActivity(), getActivity().getString(R.string.err_generic), Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        AsyncTask t = new AsyncTask() {

            boolean ok = false;
            String email;
            Boolean isOne,isTwo;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                email=txtEmail.getText().toString();
                isOne=ckOne.isChecked();
                isTwo=ckTwo.isChecked();
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if (ok) {
                    dismiss();
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.err_check_internet), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected Object doInBackground(Object[] params) {
                try {

                    //https://eternitywall.appspot.com/v1/notify?email=[email]&hash=[hash]&address=[address]&subscribe=[subscribe]&notifyreply=[notifyreply]
                    Optional<String> res = Http.get(
                            "https://eternitywall.appspot.com/v1/notify?" +
                                    "email=" + email + "&address=" + address +
                                    (isOne ? "&subscribe=true" : "") +
                                    (isTwo ? "&notifyreply=true" : ""));

                    ok = res.isPresent();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        };
        t.execute();
    }
}
