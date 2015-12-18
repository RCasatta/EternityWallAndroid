package it.eternitywall.eternitywall.dialogfragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionBroadcast;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.bitcoin.Bitcoin;
import it.eternitywall.eternitywall.wallet.EWWalletService;

/**
 * Created by Riccardo Casatta @RCasatta on 18/12/15.
 */
public class EmptyWalletDialogFragment extends DialogFragment {
    private static final String TAG = "EmptyWalletDialog";

    private EditText txtAddress;
    private Button   btnSend;
    private CheckBox ckOne;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dlg_emptywallet, container, false);
        getDialog().setTitle("Empty Wallet");

        btnSend    = (Button)   view.findViewById(R.id.btnSend);
        txtAddress = (EditText) view.findViewById(R.id.txtAddress);
        ckOne      = (CheckBox) view.findViewById(R.id.ckOne);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String bitcoinAddress = txtAddress.getText().toString();
                if(!Bitcoin.isValidAddress(bitcoinAddress)) {
                    Toast.makeText(getActivity(),"Not valid bitcoin address", Toast.LENGTH_LONG).show();
                    return;
                }

                if(! ckOne.isChecked() ) {
                    Toast.makeText(getActivity(),"Checkbox not selected", Toast.LENGTH_LONG).show();
                    return;
                }

                final EWApplication ewApplication = (EWApplication) getActivity().getApplication();
                final EWWalletService ewWalletService = ewApplication.getEwWalletService();
                final Transaction tx = ewWalletService.createExitTransaction(bitcoinAddress);

                Log.i(TAG,"Created exit transaction " + tx);
                Log.i(TAG, "ExitTransactionHex " + Bitcoin.transactionToHex(tx));

                TransactionBroadcast transactionBroadcast = ewWalletService.broadcastTransaction(tx);

                dismiss();
                Toast.makeText(getActivity(),"Broadcasting...", Toast.LENGTH_LONG).show();

                ListenableFuture<Transaction> future = transactionBroadcast.future();
                Futures.addCallback(future, new FutureCallback<Transaction>() {
                    @Override
                    public void onSuccess(@Nullable Transaction result) {
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
        });




        return view;

    }

}
