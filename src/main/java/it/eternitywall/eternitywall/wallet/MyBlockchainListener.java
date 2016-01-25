package it.eternitywall.eternitywall.wallet;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.bitcoinj.core.AbstractBlockChain;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.BlockChainListener;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.VerificationException;
import org.bitcoinj.core.Wallet;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.Preferences;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.activity.DetailActivity;
import it.eternitywall.eternitywall.bitcoin.BitcoinNetwork;

/**
 * Created by Riccardo Casatta @RCasatta on 26/11/15.
 */
public class MyBlockchainListener implements BlockChainListener {
    private static final String TAG = "MyBlockchainListener";
    private Set<Address> all;
    private int bloomMatches=0;
    private EWApplication ewApplication;
    private final static NetworkParameters PARAMS= BitcoinNetwork.getInstance().get().getParams();


    public MyBlockchainListener( Set<Address> all , EWApplication ewApplication) {
        this.all = all;
        this.ewApplication = ewApplication;

    }

    @Override
    public void notifyNewBestBlock(StoredBlock block) throws VerificationException {
        WalletObservable walletObservable = ewApplication.getWalletObservable();
        //Log.d(TAG, "notifyNewBestBlock " + block.getHeight());
        EWWalletService ewWalletService = ewApplication.getEwWalletService();
        if(ewWalletService!=null) {
            Wallet wallet = ewWalletService.getWallet();
            if(wallet.getPendingTransactions().size()==0) {
                walletObservable.setState(WalletObservable.State.SYNCED);
            }
        }

    }

    @Override
    public void reorganize(StoredBlock splitPoint, List<StoredBlock> oldBlocks, List<StoredBlock> newBlocks) throws VerificationException {
        Log.i(TAG, "reorganize");
    }

    public int getBloomMatches() {
        return bloomMatches;
    }

    @Override
    public boolean isTransactionRelevant(Transaction tx) throws ScriptException {
        //Log.i(TAG, "starting isTransactionRelevant?" + tx.getHashAsString());
        bloomMatches++;
        boolean isRelevant=false;

        final List<TransactionInput> inputs = tx.getInputs();
        for (TransactionInput input : inputs) {
            Address current = input.getScriptSig().getFromAddress(PARAMS);
            if(all.contains(current)) {
                isRelevant = true;
            }
        }

        final List<TransactionOutput> outputs = tx.getOutputs();
        for (TransactionOutput output : outputs) {
            Address current = output.getAddressFromP2PKHScript(PARAMS);
            if(all.contains(current)) {
                isRelevant = true;
            }
        }

        Log.i(TAG, "isTransactionRelevant?" + tx.getHashAsString() + " returns " + isRelevant);

        return isRelevant;
    }

    @Override
    public void receiveFromBlock(Transaction tx, StoredBlock block, AbstractBlockChain.NewBlockType blockType, int relativityOffset) throws VerificationException {
        Log.i(TAG, "receiveFromBlock " + tx.getHashAsString() + " in block " + block);
        WalletObservable walletObservable = ewApplication.getWalletObservable();
        EWWalletService.checkAlias(tx, walletObservable);
        createNotification(tx);
        //TODO add listener to confidence change
    }

    private void createNotification(Transaction tx) {
        if(tx.getConfidence().getConfidenceType() == TransactionConfidence.ConfidenceType.PENDING) {
            Log.i(TAG,"createNotification result pending");
            return;
        }

        final String hash = tx.getHashAsString();
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ewApplication);
        final Set<String> stringSet = sharedPref.getStringSet(Preferences.TO_NOTIFY, new HashSet<String>());
        if(stringSet.size()>0) {
            if(stringSet.contains(hash)) {  //NOTIFY
                stringSet.remove(hash);
                SharedPreferences.Editor edit = sharedPref.edit();
                edit.putStringSet(Preferences.TO_NOTIFY, new HashSet<String>(stringSet));
                edit.apply();

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(ewApplication)
                                .setSmallIcon(R.drawable.ic_send_white_24dp)
                                .setContentTitle("Message written")
                                .setContentText("Your message has been written on the wall!");

                Intent resultIntent = new Intent(ewApplication, DetailActivity.class);
                resultIntent.putExtra("hash", hash);

                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                ewApplication,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);

                NotificationManager mNotifyMgr =
                        (NotificationManager) ewApplication.getSystemService(ewApplication.NOTIFICATION_SERVICE);

                Integer mNotificationId = new Random().nextInt();
                mNotifyMgr.notify(mNotificationId, mBuilder.build());

            }

        }

    }


    @Override
    public boolean notifyTransactionIsInBlock(Sha256Hash txHash, StoredBlock block, AbstractBlockChain.NewBlockType blockType, int relativityOffset) throws VerificationException {
        Log.i(TAG,"notifyTransactionIsInBlock " + txHash.toString());
        //TODO write notification code here

        return false;
    }
}
