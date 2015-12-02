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
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

import it.eternitywall.eternitywall.bitcoin.Bitcoin;

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
        //System.out.println("notifyNewBestBlock");
        walletObservable.setHeight(block.getHeight());
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

        return isRelevant;
    }

    @Override
    public void receiveFromBlock(Transaction tx, StoredBlock block, AbstractBlockChain.NewBlockType blockType, int relativityOffset) throws VerificationException {
        Log.i(TAG, "receiveFromBlock " + tx.getHashAsString() + " in block " + block);
        checkAlias(tx);
    }

    private static String EWA_PREFIX = "455741";
    private void checkAlias(Transaction tx) {
        final List<TransactionOutput> outputs = tx.getOutputs();
        for (TransactionOutput to : outputs) {
            byte[] script = to.getScriptBytes();
            if(script.length>0) {
                String hexString = Hex.toHexString(script);
                Log.i(TAG, "outputHEX: " + hexString);
                if (hexString.startsWith("6a")) {
                    Log.i(TAG, "outputHEX is OP_RETURN! ");

                    hexString = hexString.substring(2);
                    String aliasNameHex=null;
                    if(hexString.startsWith(EWA_PREFIX)  ) {  //NON_STANDARD
                        Log.i(TAG, "outputHEX is EWA NON STANDARD");
                        aliasNameHex = hexString.substring(6);
                    } else if (hexString.length()>2 && hexString.substring(2).startsWith(EWA_PREFIX)) {
                        Log.i(TAG, "outputHEX is EWA WITH LENGTH");
                        aliasNameHex = hexString.substring(8);
                    }
                    if(aliasNameHex!=null) {
                        String aliasName = new String( Bitcoin.fromHex(aliasNameHex) , Charset.forName("utf-8") );

                        if(aliasName!=null && aliasName.length()>0) {
                            aliasName= aliasName.trim();
                            if(aliasName.length()>20)
                                aliasName=aliasName.substring(0,20);
                            Log.i(TAG, "Found alias name!! " + aliasName);
                            walletObservable.setAliasName(aliasName);
                            walletObservable.notifyObservers();
                        }
                    }

                }
            }
        }
    }

    @Override
    public boolean notifyTransactionIsInBlock(Sha256Hash txHash, StoredBlock block, AbstractBlockChain.NewBlockType blockType, int relativityOffset) throws VerificationException {
        Log.i(TAG,"notifyTransactionIsInBlock");

        return false;
    }
}
