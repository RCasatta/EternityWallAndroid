package it.eternitywall.eternitywall.wallet;

import android.util.Log;

import org.bitcoinj.core.AbstractBlockChain;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.BlockChainListener;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.VerificationException;
import org.bitcoinj.params.MainNetParams;

import java.util.List;
import java.util.Set;

/**
 * Created by Riccardo Casatta @RCasatta on 26/11/15.
 */
public class MyBlockchainListener implements BlockChainListener {
    private static final String TAG = "MyBlockchainListener";
    private Set<Address> all;
    private int bloomMatches=0;
    private WalletObservable walletObservable;

    public MyBlockchainListener( Set<Address> all , WalletObservable walletObservable ) {
        this.all = all;
        this.walletObservable = walletObservable;
    }

    @Override
    public void notifyNewBestBlock(StoredBlock block) throws VerificationException {
        Log.i(TAG, "notifyNewBestBlock");
        walletObservable.setHeight(block.getHeight());
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
        Log.i(TAG, "starting isTransactionRelevant?" + tx.getHashAsString());
        bloomMatches++;
        boolean isRelevant=false;

        final List<TransactionInput> inputs = tx.getInputs();
        for (TransactionInput input : inputs) {
            Address current = input.getScriptSig().getFromAddress(MainNetParams.get());
            if(all.contains(current)) {
                isRelevant = true;
            }
        }

        final List<TransactionOutput> outputs = tx.getOutputs();
        for (TransactionOutput output : outputs) {
            Address current = output.getAddressFromP2PKHScript(MainNetParams.get());
            if(all.contains(current)) {
                isRelevant = true;
            }
        }

        Log.i(TAG, "ending isTransactionRelevant?" + tx.getHashAsString() + " returns " + isRelevant);

        return isRelevant;
    }

    @Override
    public void receiveFromBlock(Transaction tx, StoredBlock block, AbstractBlockChain.NewBlockType blockType, int relativityOffset) throws VerificationException {
        Log.i(TAG, "receiveFromBlock " + tx.getHashAsString() + " in block " + block);
        EWWalletService.checkAlias(tx,walletObservable);
    }



    @Override
    public boolean notifyTransactionIsInBlock(Sha256Hash txHash, StoredBlock block, AbstractBlockChain.NewBlockType blockType, int relativityOffset) throws VerificationException {
        Log.i(TAG,"notifyTransactionIsInBlock " + txHash.toString());

        return false;
    }
}
