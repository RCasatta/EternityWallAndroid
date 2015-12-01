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
    private State state= State.NOT_STARTED;
    private Address current;
    private Integer height;
    private Integer percSync;
    private String alias;
    private String aliasName;
    private Bitmap currentQrCode;
    private Bitmap currentIdenticon;

    private String currentQrCodeSource;     //which data has been used to create the qrcode bitmap
    private String currentIdenticonSource;  //which data has been used to create the identicon bitmap
    private Long heightNotified;

    public enum State
    {
        NOT_STARTED, STARTED, NULL_PASSPHRASE, SYNCING, DOWNLOADED, SYNCED
    }

    public Coin getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(Coin walletBalance) {
        this.walletBalance = walletBalance;
        setChangedAndNotify();

    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
        setChangedAndNotify();

    }

    public Address getCurrent() {
        return current;
    }

    public void setCurrent(Address current) {
        this.current = current;
        setChangedAndNotify();

    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
        Long now = System.currentTimeMillis();
        if( heightNotified==null || (now - heightNotified > 1000)  ) { //never notify more than once a second
            heightNotified = now;
            setChangedAndNotify();
        }
    }

    public void setAll(Coin walletBalance, State state, Address current, Integer height ) {
        this.current= current;
        this.walletBalance = walletBalance;
        this.state =state;
        this.height = height;
        setChangedAndNotify();
    }

    private void setChangedAndNotify() {
        setChanged();
        notifyObservers();
    }

    public Integer getPercSync() {
        return percSync;
    }

    public void setPercSync(Integer percSync) {
        this.percSync = percSync;
        setChangedAndNotify();
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
        setChangedAndNotify();
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
        setChangedAndNotify();
    }

    public String getCurrentQrCodeSource() {
        return currentQrCodeSource;
    }

    public Bitmap getCurrentIdenticon() {
        return currentIdenticon;
    }

    public void setCurrentIdenticon(Bitmap currentIdenticon, String currentIdenticonSource) {
        this.currentIdenticon = currentIdenticon;
        this.currentIdenticonSource = currentIdenticonSource;
        setChangedAndNotify();
    }

    public String getCurrentIdenticonSource() {
        return currentIdenticonSource;
    }

    public Bitmap getCurrentQrCode() {
        return currentQrCode;
    }

    public void setCurrentQrCode(Bitmap currentQrCode, String currentQrCodeSource) {
        this.currentQrCode = currentQrCode;
        this.currentQrCodeSource = currentQrCodeSource;
        setChangedAndNotify();
    }

    @Override
    public String toString() {
        return "WalletObservable{" +
                "alias='" + alias + '\'' +
                ", walletBalance=" + walletBalance +
                ", state=" + state +
                ", current=" + current +
                ", height=" + height +
                ", percSync=" + percSync +
                ", aliasName='" + aliasName + '\'' +
                ", currentQrCodeSource='" + currentQrCodeSource + '\'' +
                ", currentIdenticonSource='" + currentIdenticonSource + '\'' +
                '}';
    }
}
