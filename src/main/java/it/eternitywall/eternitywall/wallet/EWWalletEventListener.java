package it.eternitywall.eternitywall.wallet;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.core.WalletEventListener;
import org.bitcoinj.script.Script;

import java.util.List;

/**
 * Created by Riccardo Casatta @RCasatta on 30/11/15.
 */
public class EWWalletEventListener implements WalletEventListener {
    private WalletObservable walletObservable;

    public EWWalletEventListener(WalletObservable walletObservable) {
        this.walletObservable=walletObservable;
    }

    @Override
    public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
        walletObservable.setWalletBalance(wallet.getBalance());
    }

    @Override
    public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
        walletObservable.setWalletBalance(wallet.getBalance());
    }

    @Override
    public void onReorganize(Wallet wallet) {

    }

    @Override
    public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {

    }

    @Override
    public void onWalletChanged(Wallet wallet) {

    }

    @Override
    public void onScriptsChanged(Wallet wallet, List<Script> scripts, boolean isAddingScripts) {

    }

    @Override
    public void onKeysAdded(List<ECKey> keys) {

    }
}
