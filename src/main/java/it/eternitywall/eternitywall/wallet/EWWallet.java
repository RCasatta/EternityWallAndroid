package it.eternitywall.eternitywall.wallet;


import android.content.Context;
import android.util.Log;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.CheckpointManager;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.SPVBlockStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import it.eternitywall.eternitywall.bitcoin.Bitcoin;


/**
 * Created by Riccardo Casatta @RCasatta on 26/11/15.
 */
public class EWWallet implements Runnable {
    private static final String TAG = "EWWallet";

    private final static NetworkParameters PARAMS=MainNetParams.get();
    private final static int EPOCH     = 1447891200;  //19 Novembre 2015 00:00 first EWA 5f362444d23dd258ae1c2b60b1d79cb2c5231fc50df50713a415e13502fc1da9
    private final static int PER_CHUNK = 100;
    public static final Long DUST      =  1000L ;
    public static final Long FEE       =  11000L ;
    public static final String BLOCKCHAIN_FILE = "blockchain";
    public static final String WALLET_FILE     = "wallet";
    public static final String BITCOIN_PATH    = "bitcoin";

    final CountDownLatch downloadLatch =new CountDownLatch(1);

    private String passphrase;
    private Set<Address> all       = new HashSet<>();
    private List<ECKey> messagesId = new ArrayList<>();
    private List<ECKey> changes    = new ArrayList<>();

    private Persisted persisted;
    private boolean isSynced = false;
    private Context context;

    public EWWallet(String passphrase, Context context) {
        this.passphrase = passphrase;
        this.context = context;
        loadWalletData();
    }

    private void loadWalletData() {
        FileInputStream fis = null;
        try {
            final File path = context.getDir(EWWallet.BITCOIN_PATH, Context.MODE_PRIVATE);
            final File walletFile = new File(path, EWWallet.WALLET_FILE );
            fis = new FileInputStream(walletFile);
            ObjectInputStream is = new ObjectInputStream(fis);
            persisted = (Persisted) is.readObject();
            is.close();
            fis.close();
            Log.i(TAG, "WalletLoaded");
        } catch (Exception e) {
            Log.i(TAG, "WalletNotLoaded" + e.getMessage());
        }
        if(persisted==null)
            persisted=new Persisted();
    }

    public void saveWalletData() {
        FileOutputStream fos = null;
        System.out.println("Saving " + persisted);
        try {
            final File path = context.getDir(EWWallet.BITCOIN_PATH, Context.MODE_PRIVATE);
            final File walletFile = new File(path, EWWallet.WALLET_FILE );
            fos = new FileOutputStream(walletFile);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            //os.writeObject(persisted);
            os.writeObject(persisted.getNextChange());
            os.writeObject(persisted.getNextMessageId());
            os.writeObject(persisted.getUsed());
            os.writeObject(persisted.getUtxo());
            os.writeObject(persisted.getTxMap());


            os.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Address getCurrent() {
        if(!isSynced)
            return null;
        return changes.get( persisted.getNextMessageId() ).toAddress(PARAMS);
    }

    private EWMessageData getNextMessageData() {
        final Integer nextChange = persisted.getNextChange();
        final Integer nextMessageId = persisted.getNextMessageId();
        if(!isSynced || nextChange ==0)
            return null;
        isSynced=false;  //TODO
        EWMessageData ewMessageData = new EWMessageData();
        ewMessageData.setMessageId(messagesId.get(nextMessageId).toAddress(PARAMS));
        ewMessageData.setChange(changes.get(nextChange).toAddress(PARAMS));
        ewMessageData.setInput(changes.get(nextChange - 1));
        persisted.incrementChange();
        persisted.incrementNextMessageId();
        return ewMessageData;
    }

    public Sha256Hash sendMessage(String message) {
        if(!isSynced && persisted.getNextChange()!=0)
            return null;
        isSynced=false;  //TODO

        EWMessageData ewMessageData= getNextMessageData();
        final ECKey input = ewMessageData.getInput();
        Address inputAddress = input.toAddress(PARAMS);
        Transaction newTx = new Transaction(PARAMS);

        List<TransactionOutput> transactionOutputList = persisted.getUtxo().get(inputAddress);
        Long totalAvailable = 0L;
        for (TransactionOutput to  : transactionOutputList) {
            totalAvailable += to.getValue().getValue();
            newTx.addInput(to);
        }
        Long toSend = totalAvailable-FEE-DUST;
        if(toSend < DUST)
            return null;

        final byte[] toWrite = message.getBytes();

        newTx.addOutput(Coin.valueOf(DUST), ewMessageData.getMessageId());
        newTx.addOutput(Coin.ZERO,
                new ScriptBuilder()
                        .op(ScriptOpCodes.OP_RETURN)
                        .data(toWrite)
                        .build());
        newTx.addOutput(Coin.valueOf(toSend), ewMessageData.getChange());

        final Wallet wallet = new Wallet(MainNetParams.get());
        wallet.importKey(input);
        Wallet.SendRequest req = Wallet.SendRequest.forTx(newTx);
        wallet.signTransaction(req);
        final String newTxHex = Bitcoin.transactionToHex(newTx);
        System.out.println(newTxHex);
        saveWalletData();

        return  newTx.getHash();
    }

    public Sha256Hash registerAlias(String aliasName) {
        final Integer nextChange = persisted.getNextChange();
        if(!isSynced && nextChange !=0)
            return null;
        isSynced=false;  //TODO

        ECKey aliasKey = changes.get(nextChange);
        ECKey fistChange = changes.get(nextChange+1);
        persisted.incrementChange();
        persisted.incrementChange();
        Address aliasAddress = aliasKey.toAddress(PARAMS);
        Transaction newTx = new Transaction(PARAMS);

        List<TransactionOutput> transactionOutputList = persisted.getUtxo().get(aliasAddress);
        Long totalAvailable = 0L;
        for (TransactionOutput to  : transactionOutputList) {
            totalAvailable += to.getValue().getValue();
            newTx.addInput(to);
        }
        Long toSend = totalAvailable-FEE-DUST;
        if(toSend < DUST)
            return null;

        final byte[] toWrite = aliasName.getBytes();
        newTx.addOutput(Coin.ZERO,
                new ScriptBuilder()
                        .op(ScriptOpCodes.OP_RETURN)
                        .data(toWrite)
                        .build());
        newTx.addOutput(Coin.valueOf(toSend), fistChange);

        final Wallet wallet = new Wallet(MainNetParams.get());
        wallet.importKey(aliasKey);
        Wallet.SendRequest req = Wallet.SendRequest.forTx(newTx);
        wallet.signTransaction(req);

        final String newTxHex = Bitcoin.transactionToHex(newTx);

        System.out.println(newTxHex);
        saveWalletData();

        return  newTx.getHash();
    }

    @Override
    public void run() {
        try {
            final byte[] seed = Bitcoin.getEntropyFromPassphrase(passphrase);
            final EWDerivation ewDerivation = new EWDerivation(seed);
            final File path = context.getDir(BITCOIN_PATH, Context.MODE_PRIVATE);
            final File blockFile = new File(path, BLOCKCHAIN_FILE );

            long start = System.currentTimeMillis();
            for (int i = 0; i < PER_CHUNK; i++) {
                final ECKey messageKey = ECKey.fromPrivate(ewDerivation.getMessages(i).getPrivKey());
                final ECKey changeKey  = ECKey.fromPrivate(ewDerivation.getChanges(i).getPrivKey());
                messagesId.add(messageKey);
                changes.add(changeKey);
                all.add(messageKey.toAddress(PARAMS));
                all.add(changeKey.toAddress(PARAMS));
            }
            final long l = System.currentTimeMillis() - start;
            System.out.println("My messages id are " + messagesId);
            System.out.println("My changes are " + changes);
            System.out.println("Derivation takes " + l);

            //Wallet wallet = Wallet.fromWatchingKey(params, ewDerivation.getAccount(0).getPubOnly() );
            final BlockStore blockStore = new SPVBlockStore(PARAMS, blockFile);
            final StoredBlock chainHead = blockStore.getChainHead();

            if (chainHead.getHeight() == 0) {  //first run
                CheckpointManager.checkpoint(PARAMS, Checkpoints.getAsStream(), blockStore, EPOCH);
            }

            final BlockChain chain = new BlockChain(PARAMS, blockStore);
            final MyBlockchainListener chainListener = new MyBlockchainListener(all,persisted);
            chain.addListener(chainListener);
            final PeerGroup peerGroup = new PeerGroup(PARAMS, chain);
            //peerGroup.addAddress(InetAddress.getByName("10.106.137.73"));
            //peerGroup.setMaxConnections(1);
            peerGroup.addPeerDiscovery(new DnsDiscovery(PARAMS));
            peerGroup.addPeerFilterProvider(new MyPeerFilterProvider(changes, messagesId));
            peerGroup.setFastCatchupTimeSecs(EPOCH);
            peerGroup.setDownloadTxDependencies(false);
            final MyDownloadListener downloadListener = new MyDownloadListener(downloadLatch, this);
            peerGroup.startAsync();
            peerGroup.startBlockChainDownload(downloadListener);

            downloadLatch.await();
            Set<Address> used = persisted.getUsed();

            for (ECKey current : messagesId) {
                Address a = current.toAddress(PARAMS);
                if (used.contains(a))
                    persisted.incrementNextMessageId();
                else
                    break;
            }
            for (ECKey current : changes) {
                Address a = current.toAddress(PARAMS);
                if (used.contains(a))
                    persisted.incrementChange();
                else
                    break;
            }
            isSynced = true;
            Log.i(TAG,"peerGroup.getConnectedPeers()=" + peerGroup.getConnectedPeers());
            Log.i(TAG,"chain.getBestChainHeight()=" + chain.getBestChainHeight());
            Log.i(TAG,"chainHeadHeightAtBeginning=" + chainHead.getHeight());
            Log.i(TAG,"txMap=" + persisted.getTxMap());
            Log.i(TAG,"txMap.size()=" + persisted.getTxMap().size());
            Log.i(TAG,"bloom matches=" + chainListener.getBloomMatches());
            Log.i(TAG,"bloom efficencies=" + (persisted.getTxMap().size() * 1.0) / chainListener.getBloomMatches());
            Log.i(TAG,"downloaded size=" + downloadListener.getSize() + " bytes");
            Log.i(TAG,"used address=" + used);
            Log.i(TAG,"utxo=" + persisted.getUtxo() );
            Log.i(TAG,"nextMessageId=" + persisted.getNextMessageId());
            Log.i(TAG, "nextChange=" + persisted.getNextChange());

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

}
