package it.eternitywall.eternitywall;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.subgraph.orchid.crypto.PRNGFixes;

import it.eternitywall.eternitywall.wallet.WalletObserver;
import it.eternitywall.eternitywall.wallet.EWBinder;
import it.eternitywall.eternitywall.wallet.EWWalletService;
import it.eternitywall.eternitywall.wallet.WalletObservable;

public class EWApplication extends MultiDexApplication {
    private static final String TAG = "EWApplication";
    private EWWalletService ewWalletService;
    private WalletObservable walletObservable;
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(final ComponentName className,
                                       final IBinder service) {
            final EWBinder binder = (EWBinder) service;
            ewWalletService = binder.ewWalletService;
            walletObservable = binder.walletObservable;
            walletObservable.addObserver(new WalletObserver(ewWalletService) );

            Log.i(TAG,".onServiceConnected() " + ewWalletService);
        }

        @Override
        public void onServiceDisconnected(final ComponentName arg0) {
            Log.i(TAG,".onServiceDisconnected()");
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, ".onCreate()");
        PRNGFixes.apply();
        final Intent intent = new Intent(this, EWWalletService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        //startService(intent);
    }

    public EWWalletService getEwWalletService() {
        return ewWalletService;
    }

    public WalletObservable getWalletObservable() {
        return walletObservable;
    }
}