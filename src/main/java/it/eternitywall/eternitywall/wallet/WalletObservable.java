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
    private Integer percSync;
    private Integer height;
    private String alias;
    private String aliasName;
    private Bitmap currentQrCode;
    private Bitmap currentIdenticon;

    private String currentQrCodeSource;     //which data has been used to create the qrcode bitmap
    private String currentIdenticonSource;  //which data has been used to create the identicon bitmap

    public enum State
    {
        NOT_STARTED, STARTED, NULL_PASSPHRASE, SYNCING, DOWNLOADED, SYNCED
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
        setChanged();
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
        setChanged();
    }

    public Address getCurrent() {
        return current;
    }

    public void setCurrent(Address current) {
        this.current = current;
        setChanged();
    }

    public Bitmap getCurrentIdenticon() {
        return currentIdenticon;
    }

    public void setCurrentIdenticon(Bitmap currentIdenticon) {
        this.currentIdenticon = currentIdenticon;
        setChanged();
    }

    public String getCurrentIdenticonSource() {
        return currentIdenticonSource;
    }

    public void setCurrentIdenticonSource(String currentIdenticonSource) {
        this.currentIdenticonSource = currentIdenticonSource;
        setChanged();
    }

    public Bitmap getCurrentQrCode() {
        return currentQrCode;
    }

    public void setCurrentQrCode(Bitmap currentQrCode) {
        this.currentQrCode = currentQrCode;
        setChanged();
    }

    public String getCurrentQrCodeSource() {
        return currentQrCodeSource;
    }

    public void setCurrentQrCodeSource(String currentQrCodeSource) {
        this.currentQrCodeSource = currentQrCodeSource;
        setChanged();
    }

    public Integer getPercSync() {
        return percSync;
    }

    public void setPercSync(Integer percSync) {
        this.percSync = percSync;
        setChanged();
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
        setChanged();
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
        setChanged();
    }

    public Coin getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(Coin walletBalance) {
        this.walletBalance = walletBalance;
        setChanged();
    }

    @Override
    public String toString() {
        return "WalletObservable{" +
                "alias='" + alias + '\'' +
                ", walletBalance=" + walletBalance +
                ", state=" + state +
                ", current=" + current +
                ", percSync=" + percSync +
                ", aliasName='" + aliasName + '\'' +
                ", currentQrCodeSource='" + currentQrCodeSource + '\'' +
                ", currentIdenticonSource='" + currentIdenticonSource + '\'' +
                '}';
    }
}
