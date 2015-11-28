package it.eternitywall.eternitywall.wallet;

import org.bitcoinj.wallet.WalletFiles;

import java.io.File;

/**
 * Created by Riccardo Casatta @RCasatta on 28/11/15.
 */
public class WalletSaveListener implements WalletFiles.Listener {
    @Override
    public void onBeforeAutoSave(File tempFile) {
        System.out.println("onBeforeAutoSave");
    }

    @Override
    public void onAfterAutoSave(File newlySavedFile) {
        System.out.println("onAfterAutoSave");
    }
}
