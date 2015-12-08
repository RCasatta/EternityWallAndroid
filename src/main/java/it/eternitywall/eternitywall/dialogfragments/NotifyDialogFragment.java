package it.eternitywall.eternitywall.dialogfragments;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
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
    private Button btnSend;
    private ProgressBar progress;

    private String address;

    public void setAddress(String address) {
        this.address = address;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dlg_notifyme, container, false);
        getDialog().setTitle(getString(R.string.app_name));

        txtEmail = (EditText) view.findViewById(R.id.txtEmail);
        ckOne = (CheckBox) view.findViewById(R.id.ckOne);
        ckTwo = (CheckBox) view.findViewById(R.id.ckTwo);

        btnSend = (Button) view.findViewById(R.id.btnSend);
        progress = (ProgressBar) view.findViewById(R.id.progress);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        btnSend.setVisibility(View.INVISIBLE);
                        progress.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        progress.setVisibility(View.INVISIBLE);
                        btnSend.setVisibility(View.VISIBLE);
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
                                            "email=" + txtEmail.getText() + "&address=" + address +
                                            (ckOne.isChecked() ? "&subscribe=true" : "") +
                                            (ckTwo.isChecked() ? "&notifyreply=true" : ""));

                            ok = res.isPresent();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                };
                t.execute();


            }
        });

        return view;
    }
}
