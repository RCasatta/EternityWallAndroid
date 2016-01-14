package it.eternitywall.eternitywall.wallet;

import android.util.Log;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.core.WalletEventListener;
import org.bitcoinj.script.Script;

import java.util.Collection;
import java.util.List;

/**
 * Created by Riccardo Casatta @RCasatta on 30/11/15.
 */
public class EWWalletEventListener implements WalletEventListener {
    private static final String TAG = "EWWalletEventListener";

    private WalletObservable walletObservable;

    public EWWalletEventListener(WalletObservable walletObservable) {
        this.walletObservable=walletObservable;
    }

    @Override
    public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
        Log.i(TAG, "onCoinsReceived");
        walletObservable.setWalletBalance(wallet.getBalance());
        walletObservable.setWalletUnconfirmedBalance(wallet.getBalance(Wallet.BalanceType.ESTIMATED));
        walletObservable.notifyObservers();

    }

    @Override
    public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
        Log.i(TAG, "onCoinsSent");
        walletObservable.setWalletBalance(wallet.getBalance());
        walletObservable.setWalletUnconfirmedBalance(wallet.getBalance(Wallet.BalanceType.ESTIMATED));
        walletObservable.notifyObservers();
    }

    @Override
    public void onReorganize(Wallet wallet) {
        Log.i(TAG, "onReorganize");
    }

    @Override
    public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
        if(tx.getConfidence().getDepthInBlocks()<6) {
            Log.i(TAG, "onTransactionConfidenceChanged " + tx.getHash().toString() + " " + tx.getConfidence() + " " + tx.getConfidence().getConfidenceType());
            walletObservable.setWalletBalance(wallet.getBalance());
            walletObservable.setWalletUnconfirmedBalance(wallet.getBalance(Wallet.BalanceType.ESTIMATED));

            //TODO update messagePending
            Collection<Transaction> pendingTransactions = wallet.getPendingTransactions();
            int messagePending=0;
            for (Transaction current : pendingTransactions) {
                if( EWWalletService.isEWMessage(current) ) {
                    messagePending++;
                }

            }
            walletObservable.setMessagePending(messagePending);
            walletObservable.notifyObservers();
        }
    }

    @Override
    public void onWalletChanged(Wallet wallet) {
        //Log.d(TAG, "onWalletChanged");
        walletObservable.setWalletBalance(wallet.getBalance());
        walletObservable.setWalletUnconfirmedBalance(wallet.getBalance(Wallet.BalanceType.ESTIMATED));
        walletObservable.notifyObservers();
    }

    @Override
    public void onScriptsChanged(Wallet wallet, List<Script> scripts, boolean isAddingScripts) {
    }

    @Override
    public void onKeysAdded(List<ECKey> keys) {
    }


}
