package it.eternitywall.eternitywall.dialogfragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionBroadcast;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.bitcoin.Bitcoin;
import it.eternitywall.eternitywall.wallet.EWWalletService;
import it.eternitywall.eternitywall.wallet.NotEnoughAddressException;
import it.eternitywall.eternitywall.wallet.NotSyncedException;

/**
 * Created by Riccardo Casatta @RCasatta on 18/12/15.
 */
public class EmptyWalletDialogFragment extends DialogFragment {
    private static final String TAG = "EmptyWalletDialog";

    private EditText txtAddress;
    private CheckBox ckOne;
    private View view;



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = getActivity().getLayoutInflater().inflate(R.layout.dlg_emptywallet, null);

        txtAddress = (EditText) view.findViewById(R.id.txtAddress);
        ckOne      = (CheckBox) view.findViewById(R.id.ckOne);

        return new AlertDialog.Builder(getActivity())
                // Set Dialog Title
                .setTitle("Empty Wallet")
                // Set Dialog Message : custom view
                .setView(view)
                // Positive button
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
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

    public void positive(){
        final String bitcoinAddress = txtAddress.getText().toString();
        if(!Bitcoin.isValidAddress(bitcoinAddress)) {
            Toast.makeText(getActivity(),"Not valid bitcoin address", Toast.LENGTH_LONG).show();
            return;
        }

        if(! ckOne.isChecked() ) {
            Toast.makeText(getActivity(),"Checkbox not selected", Toast.LENGTH_LONG).show();
            return;
        }

        final EWApplication ewApplication = (EWApplication) getActivity().getApplicationContext();
        final EWWalletService ewWalletService = ewApplication.getEwWalletService();

        Transaction tx;
        try {
            tx = ewWalletService.createExitTransaction(bitcoinAddress);
        } catch (NotSyncedException e) {
            Toast.makeText(getActivity(), "Not synced",Toast.LENGTH_LONG).show();
            return;
        } catch (NotEnoughAddressException e) {
            Toast.makeText(getActivity(), "100 Address limit reached",Toast.LENGTH_LONG).show();
            return;
        } catch (InsufficientMoneyException e) {
            Toast.makeText(getActivity(), "You haven't enough bitcoin",Toast.LENGTH_LONG).show();
            return;
        } catch (AddressFormatException e) {
            Toast.makeText(getActivity(), "AddressFormatException",Toast.LENGTH_LONG).show();
            return;
        }


        Log.i(TAG,"Created exit transaction " + tx);
        Log.i(TAG, "ExitTransactionHex " + Bitcoin.transactionToHex(tx));

        TransactionBroadcast transactionBroadcast = ewWalletService.broadcastTransaction(tx);

        dismiss();
        Toast.makeText(getActivity(),"Broadcasting...", Toast.LENGTH_LONG).show();

        ListenableFuture<Transaction> future = transactionBroadcast.future();
        Futures.addCallback(future, new FutureCallback<Transaction>() {
            @Override
            public void onSuccess(@Nullable Transaction result) {
                Log.i(TAG,"Transaction broadcasted!");
                Toast.makeText(getActivity(),"Transaction broadcasted!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG,t.getMessage());
                t.printStackTrace();
                Toast.makeText(getActivity(),"Transaction NOT broadcasted!", Toast.LENGTH_LONG).show();
            }
        });

    }


}
