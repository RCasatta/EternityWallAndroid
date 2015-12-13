package it.eternitywall.eternitywall.wallet;

import android.util.Log;

import org.bitcoinj.wallet.WalletFiles;

import java.io.File;

/**
 * Created by Riccardo Casatta @RCasatta on 28/11/15.
 */
public class WalletSaveListener implements WalletFiles.Listener {
    private static final String TAG = "WalletSaveListener";


    @Override
    public void onBeforeAutoSave(File tempFile) {
        Log.i(TAG, "onBeforeAutoSave");
    }

    @Override
    public void onAfterAutoSave(File newlySavedFile) {
        Log.i(TAG, "onAfterAutoSave");
    }
}
