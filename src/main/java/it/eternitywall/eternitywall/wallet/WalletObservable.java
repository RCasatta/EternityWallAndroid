package it.eternitywall.eternitywall.wallet;

import android.graphics.Bitmap;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;

import java.util.Observable;

/**
 * Created by Riccardo Casatta @RCasatta on 30/11/15.
 */
public class WalletObservable extends Observable {
    private Coin walletBalance = Coin.ZERO;
    private Coin walletUnconfirmedBalance = Coin.ZERO;
    private State state= State.NOT_STARTED;
    private Address current;
    private Integer percSync;
    private String alias;
    private String aliasName;
    private String unconfirmedAliasName;
    private Bitmap currentQrCode;
    private Bitmap currentIdenticon;
    private Integer messagePending=0;

    private String currentQrCodeSource;     //which data has been used to create the qrcode bitmap
    private String currentIdenticonSource;  //which data has been used to create the identicon bitmap

    public boolean isSyncedOrPending() {
        return isSynced() || isPending();
    }

    public boolean isSynced() {
        return state==State.SYNCED ;
    }

    public boolean isPending() {
        return state==State.PENDING ;
    }

    public enum State
    {
        NOT_STARTED, STARTED, NULL_PASSPHRASE, SYNCING, DOWNLOADED, SYNCED, PENDING
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        if(!alias.equals(this.alias)) {
            this.alias = alias;
            setChanged();
        }
    }

    public Integer getMessagePending() {
        return messagePending;
    }

    public void setMessagePending(Integer messagePending) {
        if( messagePending!=null && !messagePending.equals(this.messagePending) ) {
            this.messagePending = messagePending;
            setChanged();
        }
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        if(!aliasName.equals(this.aliasName)) {
            this.aliasName = aliasName;
            setChanged();
        }
    }

    public String getUnconfirmedAliasName() {
        return unconfirmedAliasName;
    }

    public void setUnconfirmedAliasName(String unconfirmedAliasName) {
        if(!unconfirmedAliasName.equals(this.unconfirmedAliasName)) {
            this.unconfirmedAliasName = unconfirmedAliasName;
            setChanged();
        }
    }

    public Address getCurrent() {
        return current;
    }

    public void setCurrent(Address current) {
        if(!current.equals(this.current)) {
            this.current = current;
            setChanged();
        }
    }

    public Bitmap getCurrentIdenticon() {
        return currentIdenticon;
    }

    public void setCurrentIdenticon(Bitmap currentIdenticon) {
        if(!currentIdenticon.equals(this.currentIdenticon)) {
            this.currentIdenticon = currentIdenticon;
            setChanged();
        }

    }

    public String getCurrentIdenticonSource() {
        return currentIdenticonSource;
    }

    public void setCurrentIdenticonSource(String currentIdenticonSource) {
        if(!currentIdenticonSource.equals(this.currentIdenticonSource)) {
            this.currentIdenticonSource = currentIdenticonSource;
            setChanged();
        }
    }

    public Bitmap getCurrentQrCode() {
        return currentQrCode;
    }

    public void setCurrentQrCode(Bitmap currentQrCode) {
        if(!currentQrCode.equals(this.currentQrCode)) {
            this.currentQrCode = currentQrCode;
            setChanged();
        }
    }

    public String getCurrentQrCodeSource() {
        return currentQrCodeSource;
    }

    public void setCurrentQrCodeSource(String currentQrCodeSource) {
        if(!currentQrCodeSource.equals(this.currentQrCodeSource)) {
            this.currentQrCodeSource = currentQrCodeSource;
            setChanged();
        }
    }

    public Integer getPercSync() {
        return percSync;
    }

    public void setPercSync(Integer percSync) {
        if(!percSync.equals(this.percSync)) {
            this.percSync = percSync;
            setChanged();
        }
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        if(!state.equals(this.state)) {
            this.state = state;
            setChanged();
        }
    }

    public Coin getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(Coin walletBalance) {
        if(!walletBalance.equals(this.walletBalance) ) {
            this.walletBalance = walletBalance;
            setChanged();
        }
    }

    public Coin getWalletUnconfirmedBalance() {
        return walletUnconfirmedBalance;
    }

    public void setWalletUnconfirmedBalance(Coin walletUnconfirmedBalance) {
        if(!walletUnconfirmedBalance.equals(this.walletUnconfirmedBalance) ) {
            this.walletUnconfirmedBalance = walletUnconfirmedBalance;
            setChanged();
        }
    }

    public void reset() {
        aliasName=null;
        alias=null;
        walletUnconfirmedBalance=Coin.ZERO;
        walletBalance=Coin.ZERO;
        currentQrCodeSource=null;
        current=null;
        currentQrCode=null;
        percSync=null;
        currentIdenticon=null;
        currentIdenticonSource=null;
        state=WalletObservable.State.NOT_STARTED;
        setChanged();
    }

    @Override
    public String toString() {
        return "WalletObservable{" +
                "alias='" + alias + '\'' +
                ", walletBalance=" + walletBalance +
                ", walletUnconfirmedBalance=" + walletUnconfirmedBalance +
                ", state=" + state +
                ", current=" + current +
                ", messagePending=" + messagePending +
                ", percSync=" + percSync +
                ", aliasName='" + aliasName + '\'' +
                ", currentQrCodeSource='" + currentQrCodeSource + '\'' +
                ", currentIdenticonSource='" + currentIdenticonSource + '\'' +
                '}';
    }
}
