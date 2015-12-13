package it.eternitywall.eternitywall;

import android.util.Log;

import java.util.TimerTask;

import it.eternitywall.eternitywall.wallet.EWWalletService;
import it.eternitywall.eternitywall.wallet.WalletObservable;

/**
 * Created by Riccardo Casatta @RCasatta on 09/12/15.
 */
public class TimedLogStat extends TimerTask {
    private static final String TAG = "TimedLogStat";
    private EWApplication ewApplication;

    public TimedLogStat(EWApplication ewApplication) {
        Log.i(TAG, "TimedLogStat()");
        this.ewApplication = ewApplication;
    }

    @Override
    public void run() {
        final WalletObservable walletObservable = ewApplication.getWalletObservable();

        if(walletObservable!=null) {
            Log.i(TAG, walletObservable.toString());
            final EWWalletService ewWalletService = ewApplication.getEwWalletService();
            if(ewWalletService.getBlockChain()!=null) Log.i(TAG, "blockchain height:" + ewWalletService.getBlockChain().getBestChainHeight() );
            if(ewWalletService.getPeerGroup()!=null) {
                Log.i(TAG, "peerGroup is running? " + ewWalletService.getPeerGroup().isRunning() );
                Log.i(TAG, "peerGroup connected peers? " + ewWalletService.getPeerGroup().numConnectedPeers());
            }
            if(ewWalletService.getWallet()!=null) Log.i(TAG, "wallet height" + ewWalletService.getWallet().getLastBlockSeenHeight());
        } else {
            Log.i(TAG, "WalletObservable is null ()");
        }

    }
}
