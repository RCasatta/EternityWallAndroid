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
    private Set<Address> used;

    private int bloomMatches=0;

    public MyBlockchainListener(Set<Address> all, Set<Address> used) {
        this.all = all;
        this.used = used;
    }

    @Override
    public void notifyNewBestBlock(StoredBlock block) throws VerificationException {
        //System.out.println("notifyNewBestBlock");
    }

    @Override
    public void reorganize(StoredBlock splitPoint, List<StoredBlock> oldBlocks, List<StoredBlock> newBlocks) throws VerificationException {
        System.out.println("reorganize");
    }

    public int getBloomMatches() {
        return bloomMatches;
    }

    @Override
    public boolean isTransactionRelevant(Transaction tx) throws ScriptException {
        Log.i(TAG, "isTransactionRelevant?" + tx.getHashAsString());
        bloomMatches++;
        boolean isRelevant=false;

        final List<TransactionInput> inputs = tx.getInputs();
        for (TransactionInput input : inputs) {
            Address current = input.getScriptSig().getFromAddress(MainNetParams.get());
            if(all.contains(current)) {
                used.add(current);
                isRelevant = true;
            }

        }

        final List<TransactionOutput> outputs = tx.getOutputs();
        for (TransactionOutput output : outputs) {
            Address current = output.getAddressFromP2PKHScript(MainNetParams.get());
            if(all.contains(current)) {
                used.add(current);
                isRelevant = true;
            }
        }

        return isRelevant;
    }

    @Override
    public void receiveFromBlock(Transaction tx, StoredBlock block, AbstractBlockChain.NewBlockType blockType, int relativityOffset) throws VerificationException {
        Log.i(TAG, "receiveFromBlock " + tx.getHashAsString() + " in block " + block);
    }

    @Override
    public boolean notifyTransactionIsInBlock(Sha256Hash txHash, StoredBlock block, AbstractBlockChain.NewBlockType blockType, int relativityOffset) throws VerificationException {
        Log.i(TAG,"notifyTransactionIsInBlock");

        return false;
    }
}
