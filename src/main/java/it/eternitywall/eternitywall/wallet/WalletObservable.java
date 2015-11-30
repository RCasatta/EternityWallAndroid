package it.eternitywall.eternitywall.wallet;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;

import java.util.Observable;

/**
 * Created by Riccardo Casatta @RCasatta on 30/11/15.
 */
public class WalletObservable extends Observable {
    private Coin walletBalance = Coin.ZERO;
    private State state= State.NOT_STARTED;
    private Address current;

    public enum State
    {
        NOT_STARTED, STARTED, NULL_PASSPHRASE, SYNCING, DONWLOADED, SYNCED
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

    public Address getCurrent() {
        return current;
    }

    public void setCurrent(Address current) {
        this.current = current;
        setChanged();
        notifyObservers();
    }

    @Override
    public String toString() {
        return "WalletObservable{" +
                "current=" + current +
                ", walletBalance=" + walletBalance +
                ", state=" + state +
                '}';
    }
}
