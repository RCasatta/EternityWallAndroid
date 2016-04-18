package it.eternitywall.eternitywall.dialogfragments;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionBroadcast;

import java.util.concurrent.ExecutionException;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.wallet.EWWalletService;
import it.eternitywall.eternitywall.wallet.NotSyncedException;
import it.eternitywall.eternitywall.wallet.WalletObservable;

/**
 * Created by Riccardo Casatta @RCasatta on 08/12/15.
 */
public class RegAliasDialogFragment extends DialogFragment {
    private static final String TAG = RegAliasDialogFragment.class.toString();

    private TextView txtAlias;
    private View view;

    private WalletObservable walletObservable;

    public RegAliasDialogFragment() {
    }


    public void setWalletObservable(WalletObservable walletObservable){
        this.walletObservable = walletObservable;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dlg_registeralias, null);
        txtAlias = (TextView) view.findViewById(R.id.txtAlias);

        return new AlertDialog.Builder(getActivity())
                // Set Dialog Title
                .setTitle("Register Alias")
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
        Log.i(TAG, "onClick");

        final String aliasString = txtAlias.getText().toString();
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
        Transaction transaction;
        try {
            transaction = ewWalletService.registerAlias(aliasString);
        } catch (NotSyncedException e) {
            Toast.makeText(getActivity(), "Not synced",Toast.LENGTH_LONG).show();
            return;
        } catch (InsufficientMoneyException e) {
            Toast.makeText(getActivity(), "You haven't enough bitcoin",Toast.LENGTH_LONG).show();
            return;
        }
        final TransactionBroadcast transactionBroadcast = ewWalletService.broadcastTransaction(transaction);

        if(transactionBroadcast==null) {
            Toast.makeText(getActivity(), "Cannot broadcast", Toast.LENGTH_LONG).show();
            return;
        }

        try {

            ListenableFuture<Transaction> future = transactionBroadcast.future();
            Futures.addCallback(future, new FutureCallback<Transaction>() {
                @Override
                public void onSuccess(@Nullable Transaction result) {
                    Log.i(TAG,"onSuccess callback");
                    walletObservable.setUnconfirmedAliasName(aliasString);
                    walletObservable.notifyObservers();
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.i(TAG,"onFailure callback");

                }
            });
            Transaction transactionFuture = future.get();
            if (transactionFuture == null) {
                Toast.makeText(getActivity(), "hash is null", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(getActivity(), "Alias created!", Toast.LENGTH_LONG).show();
            }

        } catch (InterruptedException | ExecutionException e) {
            Toast.makeText(getActivity(), "Oooops...", Toast.LENGTH_LONG).show();
            Log.w(TAG, "Error " + e.getMessage());
            e.printStackTrace();
        }

        dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();

        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

}
