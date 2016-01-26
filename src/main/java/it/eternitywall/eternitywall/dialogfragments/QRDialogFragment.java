package it.eternitywall.eternitywall.dialogfragments;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.Message;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.bitcoin.Bitcoin;
import it.eternitywall.eternitywall.wallet.WalletObservable;

/**
 * Created by Riccardo Casatta @RCasatta on 08/12/15.
 */
public class  QRDialogFragment extends DialogFragment {
    private static final String TAG = QRDialogFragment.class.toString();

    private View view;
    private Bitmap QrCodeBitmap;



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        view = getActivity().getLayoutInflater().inflate(R.layout.dlg_qr, null);

        // set the custom dialog components - text, image and button
        TextView txtAddress = (TextView) view.findViewById(R.id.txtAddress);
        ImageView currentQrCode = (ImageView) view.findViewById(R.id.currentQrCode);


        EWApplication ewApplication = (EWApplication) getActivity().getApplication();
        final WalletObservable walletObservable = ewApplication.getWalletObservable();   //Should be moved in activity, but this way you have no callback for the listener here

        txtAddress.setText(walletObservable.getCurrent().toString() );
        QrCodeBitmap=walletObservable.getCurrentQrCode();
        if (QrCodeBitmap != null)
            currentQrCode.setImageBitmap(QrCodeBitmap);

        currentQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(walletObservable!=null && walletObservable.getCurrent()!=null && Bitcoin.isValidAddress(walletObservable.getCurrent().toString())) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("bitcoin:" + walletObservable.getCurrent().toString()));
                    if(isAvailable(i))
                        startActivity(i);
                    else {
                        android.app.AlertDialog d = new android.app.AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.app_name))
                                .setMessage("There are no bitcoin wallet app installed, do you want to install GreenBits?")
                                .setNegativeButton("No, thanks", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton("Yes" + "!", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.greenaddress.greenbits_android_wallet")));
                                    }
                                })
                                .create();
                        d.show();
                    }
                }
            }
        });

        return new AlertDialog.Builder(getActivity())
                // Set Dialog Title
                .setTitle("Address")
                // Set Dialog Message : custom view
                .setView(view)
                // Positive button
                .create();
    }
    private boolean isAvailable(Intent intent) {
        final PackageManager mgr = getActivity().getPackageManager();
        List<ResolveInfo> list =
                mgr.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }


}
