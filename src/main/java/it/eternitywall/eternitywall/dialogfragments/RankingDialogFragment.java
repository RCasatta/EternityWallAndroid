package it.eternitywall.eternitywall.dialogfragments;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionBroadcast;

import java.util.concurrent.ExecutionException;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.Message;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.wallet.EWWalletService;
import it.eternitywall.eternitywall.wallet.WalletObservable;

/**
 * Created by Riccardo Casatta @RCasatta on 08/12/15.
 */
public class RankingDialogFragment extends DialogFragment {
    private static final String TAG = RankingDialogFragment.class.toString();

    private View view;
    private Message mMessage;

    public void setMessage(Message message){
        this.mMessage = message;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_ranking, null);

        // set the custom dialog components - text, image and button
        TextView txtRank = (TextView) view.findViewById(R.id.txtRank);
        TextView txtValue = (TextView) view.findViewById(R.id.txtValue);
        TextView txtText = (TextView) view.findViewById(R.id.txtText);
        txtText.setText("This message has been viewed " + mMessage.getView() + " times of which " + mMessage.getWeekView() + " in the last seven days.");

        long integer = Math.round(mMessage.getValue() * 1000);
        Double doubled = Double.valueOf(integer) / 1000;

        txtValue.setText(doubled.toString());
        if (mMessage.getRank() == 1)
            txtRank.setText("top");
        else if (mMessage.getRank() == 2)
            txtRank.setText("middle");
        else if (mMessage.getRank() == 3)
            txtRank.setText("low");


        return new AlertDialog.Builder(getActivity())
                // Set Dialog Title
                .setTitle("Message ranking")
                // Set Dialog Message : custom view
                .setView(view)
                // Positive button
                .create();
    }


}
