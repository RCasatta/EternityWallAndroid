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
    private WalletObservable.State was;

    public WalletObserver(EWWalletService ewWalletService) {
        this.ewWalletService = ewWalletService;
        was= WalletObservable.State.STARTED;
    }

    @Override
    public void update(Observable observable, Object data) {
        final WalletObservable walletObservable= (WalletObservable) observable;
        Log.i(TAG, android.os.Process.myTid() + " TID :" + walletObservable+ " was: " + was);

        final String alias = walletObservable.getAlias();
        if (alias != null && !alias.equals(walletObservable.getCurrentIdenticonSource())) {
            Log.i(TAG, "CreateOrRefreshingIdenticon");
            Bitmap identicon = IdenticonGenerator.generate(alias);
            walletObservable.setCurrentIdenticon(identicon);
            walletObservable.setCurrentIdenticonSource(alias);
            walletObservable.notifyObservers();
        }

        final WalletObservable.State currentState = walletObservable.getState();
        if(was==WalletObservable.State.SYNCING && currentState == WalletObservable.State.DOWNLOADED) {
            Log.i(TAG,"calling on sync");
            was= currentState;
            ewWalletService.onSynced();
        } else if(currentState == WalletObservable.State.SYNCED) {


            final Address currentAddress = walletObservable.getCurrent();
            if (currentAddress != null && !currentAddress.toString().equals(walletObservable.getCurrentQrCodeSource())) {
                Log.i(TAG, "CreateOrRefreshingQrcode");
                final String current = currentAddress.toString();
                Bitmap bitmap = QrBitmap.toBitmap(current);
                walletObservable.setCurrentQrCode(bitmap);
                walletObservable.setCurrentQrCodeSource(current);
                walletObservable.notifyObservers();
            }
        }

        was= currentState;

    }
}
