package it.eternitywall.eternitywall.wallet;

import org.bitcoinj.core.AbstractBlockChain;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.VerificationException;
import org.bitcoinj.core.listeners.BlockChainListener;

import java.util.List;

/**
 * Created by Riccardo Casatta @RCasatta on 21/12/15.
 */
public class EWChainListener implements BlockChainListener {
    int counter=0;

    @Override
    public void notifyNewBestBlock(StoredBlock block) throws VerificationException {

    }

    @Override
    public void reorganize(StoredBlock splitPoint, List<StoredBlock> oldBlocks, List<StoredBlock> newBlocks) throws VerificationException {

    }
/*
    @Override
    public boolean isTransactionRelevant(Transaction tx) throws ScriptException {
        System.out.println("tx " +tx.toString());
        counter++;
        return false;
    }
*/
    @Override
    public void receiveFromBlock(Transaction tx, StoredBlock block, AbstractBlockChain.NewBlockType blockType, int relativityOffset) throws VerificationException {

    }

    @Override
    public boolean notifyTransactionIsInBlock(Sha256Hash txHash, StoredBlock block, AbstractBlockChain.NewBlockType blockType, int relativityOffset) throws VerificationException {
        return false;
    }
}
