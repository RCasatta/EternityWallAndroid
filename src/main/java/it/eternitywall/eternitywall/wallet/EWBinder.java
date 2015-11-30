package it.eternitywall.eternitywall.wallet;

import android.os.Binder;

/**
 * Created by Riccardo Casatta @RCasatta on 29/11/15.
 */
public class EWBinder extends Binder {
    public EWWalletService ewWalletService;

    public EWBinder(EWWalletService ewWalletService) {
        this.ewWalletService = ewWalletService;
    }
}
