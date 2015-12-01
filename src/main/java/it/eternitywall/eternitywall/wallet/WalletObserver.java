package it.eternitywall.eternitywall.wallet;

import android.graphics.Bitmap;
import android.util.Log;

import org.bitcoinj.core.Address;

import java.util.Observable;
import java.util.Observer;

import it.eternitywall.eternitywall.IdenticonGenerator;

/**
 * Created by Riccardo Casatta @RCasatta on 30/11/15.
 */
public class WalletObserver implements Observer {
    private static final String TAG = "WalletObserver";
    private EWWalletService ewWalletService;

    public WalletObserver(EWWalletService ewWalletService) {
        this.ewWalletService = ewWalletService;
    }

    @Override
    public void update(Observable observable, Object data) {
        WalletObservable walletObservable= (WalletObservable) observable;
        Log.i(TAG, android.os.Process.myTid() + " TID :" + walletObservable);

        if(walletObservable.getState()== WalletObservable.State.DOWNLOADED) {
            Log.i(TAG,"calling on sync");
            ewWalletService.onSynced();
        }

        final String alias = walletObservable.getAlias();
        if(alias!=null && !alias.equals(walletObservable.getCurrentIdenticonSource())) {
            Log.i(TAG,"CreateOrRefreshingIdenticon");
            Bitmap identicon = IdenticonGenerator.generate(alias);
            walletObservable.setCurrentIdenticon(identicon,alias);
        }

        final Address currentAddress = walletObservable.getCurrent();
        if(currentAddress!=null && !currentAddress.toString().equals(walletObservable.getCurrentQrCodeSource())) {
            Log.i(TAG, "CreateOrRefreshingIdenticon");
            final String current = currentAddress.toString();
            Bitmap bitmap = QrBitmap.toBitmap(current);
            walletObservable.setCurrentQrCode(bitmap, current);
        }

    }
}
