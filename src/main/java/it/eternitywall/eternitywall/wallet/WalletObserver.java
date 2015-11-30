package it.eternitywall.eternitywall.wallet;

import android.util.Log;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Riccardo Casatta @RCasatta on 30/11/15.
 */
public class WalletObserver implements Observer {
    private static final String TAG = "WalletObserver";

    public WalletObserver() {
    }

    @Override
    public void update(Observable observable, Object data) {
        WalletObservable walletObservable= (WalletObservable) observable;
        Log.i(TAG, "" + walletObservable);
    }
}
