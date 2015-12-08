package it.eternitywall.eternitywall.dialogfragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionBroadcast;

import java.util.concurrent.ExecutionException;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.wallet.EWWalletService;

/**
 * Created by Riccardo Casatta @RCasatta on 08/12/15.
 */
public class RegAliasDialogFragment extends DialogFragment {
    private static final String TAG = RegAliasDialogFragment.class.toString();

    private Button btnRegister;
    private TextView txtAlias;

    public RegAliasDialogFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.dlg_registeralias, container, false);
        getDialog().setTitle("Register Alias");

        btnRegister = (Button) view.findViewById(R.id.btnRegister);
        txtAlias = (TextView) view.findViewById(R.id.txtAlias);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick");

                String aliasString = txtAlias.getText().toString();
                if (aliasString == null || aliasString.isEmpty()) {
                    Toast.makeText(getActivity(), "Alias cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                if (aliasString.length() > 20) {
                    Toast.makeText(getActivity(), "Alias cannot be longer than 20 chars", Toast.LENGTH_LONG).show();
                    return;
                }

                EWWalletService ewWalletService = ((EWApplication) getActivity().getApplication()).getEwWalletService();

                //TODO check messages
                TransactionBroadcast transactionBroadcast = ewWalletService.registerAlias(aliasString);
                try {
                    Transaction transaction = transactionBroadcast.future().get();
                    if (transaction == null) {
                        Toast.makeText(getActivity(), "hash is null", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(getActivity(), "register alias tx created", Toast.LENGTH_LONG).show();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                dismiss();
            }
        });

        return view;

    }

}
