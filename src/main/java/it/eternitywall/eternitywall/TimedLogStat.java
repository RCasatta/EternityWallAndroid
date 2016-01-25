package it.eternitywall.eternitywall;

import android.util.Log;

import com.google.common.base.Joiner;

import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Wallet;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
            if(ewWalletService.getBlockChain()!=null) {
                final int bestChainHeight = ewWalletService.getBlockChain().getBestChainHeight();
                Log.i(TAG, "blockchain height: " + bestChainHeight);
            }
            final PeerGroup peerGroup = ewWalletService.getPeerGroup();
            if(peerGroup !=null) {
                Log.i(TAG, "peerGroup is running? " + peerGroup.isRunning());
                Log.i(TAG, "peerGroup connected peers? " + peerGroup.numConnectedPeers());

            }
            final Wallet wallet = ewWalletService.getWallet();
            if(wallet !=null) {
                Log.i(TAG, "wallet height: " + wallet.getLastBlockSeenHeight());
                final Set<Transaction> transactions = wallet.getTransactions(true);
                final List<String> hashes=new LinkedList<>();
                for (Transaction tx : transactions) {
                    hashes.add(tx.getHashAsString());
                }
                Log.i(TAG, "wallet txs: " + Joiner.on(",").join(hashes) );

            }
        } else {
            Log.i(TAG, "WalletObservable is null ()");
        }

    }
}
