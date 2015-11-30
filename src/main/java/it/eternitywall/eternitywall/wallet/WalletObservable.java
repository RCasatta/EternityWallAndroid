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
    private Integer height;
    private Integer percSync;

    private Long heightNotified;

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

    public Address getCurrent() {
        return current;
    }

    public void setCurrent(Address current) {
        this.current = current;
        setChanged();
        notifyObservers();
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
        Long now = System.currentTimeMillis();
        if( heightNotified==null ) {
            heightNotified = now;
            setChanged();
            notifyObservers();
        } else if( now - heightNotified > 1000 ) {  //never notify more than once a second
            heightNotified = now;
            setChanged();
            notifyObservers();
        }

    }

    public void setAll(Coin walletBalance, State state, Address current ) {
        this.current= current;
        this.walletBalance = walletBalance;
        this.state =state;
        setChanged();
        notifyObservers();
    }

    public Integer getPercSync() {
        return percSync;
    }

    public void setPercSync(Integer percSync) {
        this.percSync = percSync;
        setChanged();
        notifyObservers();
    }

    @Override
    public String toString() {
        return "WalletObservable{" +
                "current=" + current +
                ", walletBalance=" + walletBalance +
                ", state=" + state +
                ", height=" + height +
                '}';
    }
}
