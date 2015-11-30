package it.eternitywall.eternitywall.wallet;

import android.os.Binder;

/**
 * Created by Riccardo Casatta @RCasatta on 29/11/15.
 */
public class EWBinder extends Binder {
    public EWWalletService ewWalletService;
    public WalletObservable walletObservable;

    public EWBinder(EWWalletService ewWalletService, WalletObservable walletObservable ) {
        this.ewWalletService = ewWalletService;
        this.walletObservable = walletObservable;
    }
}
