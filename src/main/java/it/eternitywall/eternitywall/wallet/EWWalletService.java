package it.eternitywall.eternitywall.wallet;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
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
import org.bitcoinj.wallet.WalletTransaction;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import it.eternitywall.eternitywall.bitcoin.Bitcoin;


/**
 * Created by Riccardo Casatta @RCasatta on 26/11/15.
 */
public class EWWalletService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener, Runnable {
    private static final String TAG = "EWWalletService";

    private final static NetworkParameters PARAMS=MainNetParams.get();
    private final static int EPOCH     = 1447891200;  //19 Novembre 2015 00:00 first EWA 5f362444d23dd258ae1c2b60b1d79cb2c5231fc50df50713a415e13502fc1da9
    private final static int PER_CHUNK = 100;
    public static final Long DUST      =  1000L ;
    public static final Long FEE       =  11000L ;
    public static final String BLOCKCHAIN_FILE = "blockchain";
    public static final String WALLET_FILE     = "wallet";
    public static final String BITCOIN_PATH    = "bitcoin";

    private Set<Address> used      = new HashSet<>();
    private Integer nextMessageId = 0 ;
    private Integer nextChange = 0 ;

    private Set<Address> all       = new HashSet<>();
    private List<ECKey> messagesId = new ArrayList<>();
    private List<ECKey> changes    = new ArrayList<>();

    private BlockStore blockStore;
    private BlockChain blockChain;
    private PeerGroup peerGroup;
    private Wallet wallet;
    private boolean isSynced = false;

    public Address getCurrent() {
        if(!isSynced)
            return null;
        return changes.get( nextMessageId ).toAddress(PARAMS);
    }

    private EWMessageData getNextMessageData() {
        if(!isSynced || nextChange ==0)
            return null;
        isSynced=false;  //TODO
        EWMessageData ewMessageData = new EWMessageData();
        ewMessageData.setMessageId(messagesId.get(nextMessageId).toAddress(PARAMS));
        ewMessageData.setChange(changes.get(nextChange).toAddress(PARAMS));
        ewMessageData.setInput(changes.get(nextChange - 1));
        nextChange++;
        nextMessageId++;
        return ewMessageData;
    }

    public Sha256Hash sendMessage(String message) {
        if(!isSynced && nextChange!=0)
            return null;
        isSynced=false;  //TODO

        EWMessageData ewMessageData= getNextMessageData();
        final ECKey input = ewMessageData.getInput();
        Address inputAddress = input.toAddress(PARAMS);
        Transaction newTx = new Transaction(PARAMS);

        List<TransactionOutput> transactionOutputList = getMines(inputAddress);
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

        Wallet.SendRequest req = Wallet.SendRequest.forTx(newTx);
        wallet.signTransaction(req);
        final String newTxHex = Bitcoin.transactionToHex(newTx);
        Log.i(TAG, newTxHex);

        return  newTx.getHash();
    }

    public Sha256Hash registerAlias(String aliasName) {
        if(!isSynced && nextChange !=0)
            return null;
        isSynced=false;  //TODO

        ECKey aliasKey = changes.get(nextChange);
        nextChange++;
        ECKey fistChange = changes.get(nextChange);
        nextChange++;

        Address aliasAddress = aliasKey.toAddress(PARAMS);
        Transaction newTx = new Transaction(PARAMS);




        List<TransactionOutput> transactionOutputList = getMines(aliasAddress);
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

        Wallet.SendRequest req = Wallet.SendRequest.forTx(newTx);
        wallet.signTransaction(req);

        final String newTxHex = Bitcoin.transactionToHex(newTx);

        Log.i(TAG, newTxHex);

        return  newTx.getHash();
    }

    private List<TransactionOutput> getMines(Address address) {
        Map<Sha256Hash, Transaction> unspent = wallet.getTransactionPool(WalletTransaction.Pool.UNSPENT);
        List<TransactionOutput> transactionOutputList= new ArrayList<>();
        for (Map.Entry<Sha256Hash, Transaction> entry : unspent.entrySet()) {
            Transaction current = entry.getValue();
            List<TransactionOutput> outputs = current.getOutputs();
            for (TransactionOutput o : outputs) {
                Address a = o.getAddressFromP2PKHScript(PARAMS);
                if(address.equals(a))
                    transactionOutputList.add(o);
            }
        }
        return transactionOutputList;
    }

    private EWBinder mBinder= new EWBinder(this);
    @Override
    public IBinder onBind(final Intent intent)
    {
        Log.i(TAG, ".onBind()");
        return mBinder;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        Log.i(TAG, ".onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, ".onCreate()");
        new Thread(this).start();

    }

    @Override
    public void run() {
        try {
            final Context context = getApplicationContext();
            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            final String passphrase = sharedPref.getString("passphrase", null);
            if(passphrase==null) {
                Log.i(TAG,"PassphraseIsNull, cannot sync");
                return;
            }
            final byte[] seed = Bitcoin.getEntropyFromPassphrase(passphrase);
            final EWDerivation ewDerivation = new EWDerivation(seed);
            File path;
            if(context!=null)
                path= context.getDir(BITCOIN_PATH, Context.MODE_PRIVATE);
            else
                path = new File("./data/");
            final File blockFile = new File(path, BLOCKCHAIN_FILE );
            final File walletFile = new File(path, WALLET_FILE );

            long start = System.currentTimeMillis();
            for (int i = 0; i < PER_CHUNK; i++) {
                final ECKey messageKey = ECKey.fromPrivate(ewDerivation.getMessages(i).getPrivKey());
                final ECKey changeKey  = ECKey.fromPrivate(ewDerivation.getChanges(i).getPrivKey());
                messagesId.add(messageKey);
                changes.add(changeKey);
                all.add(messageKey.toAddress(PARAMS));
                all.add(changeKey.toAddress(PARAMS));
            }


            if(walletFile.exists()) {
                Log.i(TAG, "Wallet exist, loading from file");
                wallet = Wallet.loadFromFile(walletFile);
            }
            else {
                Log.i(TAG, "Wallet not exist, creating new");
                List<ECKey> ecKeyList = new ArrayList<>();
                ecKeyList.addAll(messagesId);
                ecKeyList.addAll(changes);
                wallet = Wallet.fromKeys(PARAMS,ecKeyList);
            }

            Log.i(TAG, "wallet bloom " + wallet.getBloomFilter(1E-5));
            wallet.autosaveToFile(walletFile, 30, TimeUnit.SECONDS, new WalletSaveListener());

            final long l = System.currentTimeMillis() - start;
            Log.i(TAG, "My messages id are " + messagesId);
            Log.i(TAG, "My changes are " + changes);
            Log.i(TAG, "Derivation takes " + l);

            blockStore = new SPVBlockStore(PARAMS, blockFile);
            final StoredBlock chainHead = blockStore.getChainHead();

            if (chainHead.getHeight() == 0) {  //first run
                Log.i(TAG, "First run");
                CheckpointManager.checkpoint(PARAMS, Checkpoints.getAsStream(), blockStore, EPOCH);
            }

            blockChain = new BlockChain(PARAMS, wallet, blockStore);
            final MyBlockchainListener chainListener = new MyBlockchainListener(all,used );
            blockChain.addListener(chainListener);
            peerGroup = new PeerGroup(PARAMS, blockChain);
            //peerGroup.addAddress(InetAddress.getByName("10.106.137.73"));
            //peerGroup.setMaxConnections(1);
            //peerGroup.addWallet(wallet);
            peerGroup.addPeerDiscovery(new DnsDiscovery(PARAMS));
            peerGroup.addPeerFilterProvider(new MyPeerFilterProvider(changes, messagesId));
            peerGroup.setFastCatchupTimeSecs(EPOCH);
            peerGroup.setDownloadTxDependencies(false);
            final MyDownloadListener downloadListener = new MyDownloadListener();
            peerGroup.startAsync();
            peerGroup.startBlockChainDownload(downloadListener);

            //downloadLatch.await();

            nextMessageId=0;
            for (ECKey current : messagesId) {
                Address a = current.toAddress(PARAMS);
                if (used.contains(a))
                    nextMessageId++;
                else
                    break;
            }
            nextChange=0;
            for (ECKey current : changes) {
                Address a = current.toAddress(PARAMS);
                if (used.contains(a))
                    nextChange++;
                else
                    break;
            }
            isSynced = true;
            Log.i(TAG, "peerGroup.getConnectedPeers()=" + peerGroup.getConnectedPeers());
            Log.i(TAG, "chain.getBestChainHeight()=" + blockChain.getBestChainHeight());
            Log.i(TAG, "chainHeadHeightAtBeginning=" + chainHead.getHeight());
            Log.i(TAG, "bloom matches=" + chainListener.getBloomMatches());
            Log.i(TAG, "downloaded size=" + downloadListener.getSize() + " bytes");
            Log.i(TAG, "used address=" + used);
            Log.i(TAG, "nextMessageId=" + nextMessageId);
            Log.i(TAG, "nextChange=" + nextChange);
            Log.i(TAG, "wallet=" + wallet);


            //Toast.makeText(getApplication(),"Blockchain synced",Toast.LENGTH_LONG).show();
            Log.i(TAG,"BlockchainSynced");

        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(TAG, ".onSharedPreferenceChanged(" + key + ")" );
        if("passphrase".equals(key)) {
            Log.i(TAG,"PassphraseChanged starting sync");
            //DELETE WALLET FILE AND BLOCKCHAIN
            new Thread(this).start();

        }
    }

}
