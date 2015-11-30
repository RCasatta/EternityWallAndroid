package it.eternitywall.eternitywall.wallet;

import org.bitcoinj.core.Coin;

import java.util.Observable;

/**
 * Created by Riccardo Casatta @RCasatta on 30/11/15.
 */
public class WalletObservable extends Observable {
    private Coin walletBalance = Coin.ZERO;
    private State state= State.NOT_STARTED;

    public enum State
    {
        NOT_STARTED, STARTED, NULL_PASSPHRASE, SYNCING, SYNCED
    }

    public Coin getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(Coin walletBalance) {
        this.walletBalance = walletBalance;
        setChanged();
        notifyObservers();
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
        setChanged();
        notifyObservers();
    }

    @Override
    public String toString() {
        return "WalletObservable{" +
                "state=" + state +
                ", walletBalance=" + walletBalance +
                '}';
    }
}
