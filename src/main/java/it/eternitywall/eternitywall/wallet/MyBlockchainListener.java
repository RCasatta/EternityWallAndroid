package it.eternitywall.eternitywall.wallet;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Riccardo Casatta @RCasatta on 26/11/15.
 */
public class MyBlockchainListener implements BlockChainListener {
    private Set<Address> all;
    private Persisted persisted;
    private int bloomMatches=0;

    public MyBlockchainListener(Set<Address> all, Persisted persisted) {
        this.all = all;
        this.persisted = persisted;
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
        System.out.println("isTransactionRelevant?" + tx.getHashAsString());
        bloomMatches++;
        boolean isRelevant=false;
        Set<Address> used = persisted.getUsed();

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
        System.out.println("receiveFromBlock " + tx.getHashAsString() + " in block " + block);

        final Map<Address, List<TransactionOutput>> utxo = persisted.getUtxo();
        final Map<Sha256Hash, Transaction> txMap = persisted.getTxMap();
        txMap.put(tx.getHash(), tx);

        final List<TransactionInput> inputs = tx.getInputs();
        for (TransactionInput input : inputs) {
            input.connect(txMap, TransactionInput.ConnectMode.ABORT_ON_CONFLICT);
            Address current = input.getScriptSig().getFromAddress(MainNetParams.get());
            if(all.contains(current)) {
                List<TransactionOutput> currentUtxo = utxo.get(current);
                if(currentUtxo!=null) {
                    currentUtxo.remove(input.getConnectedOutput());
                }
            }
        }

        final List<TransactionOutput> outputs = tx.getOutputs();
        for (TransactionOutput output : outputs) {
            Address current = output.getAddressFromP2PKHScript(MainNetParams.get());
            if(all.contains(current)) {
                List<TransactionOutput> currentUtxo = utxo.get(current);
                if(currentUtxo==null)
                    currentUtxo=new ArrayList<>();
                currentUtxo.add(output);
                utxo.put(current,currentUtxo);
            }
        }
    }

    @Override
    public boolean notifyTransactionIsInBlock(Sha256Hash txHash, StoredBlock block, AbstractBlockChain.NewBlockType blockType, int relativityOffset) throws VerificationException {
        System.out.println("notifyTransactionIsInBlock");

        return false;
    }
}
