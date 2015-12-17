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
import org.bitcoinj.core.TransactionBroadcast;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.WalletTransaction;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.Preferences;
import it.eternitywall.eternitywall.bitcoin.Bitcoin;


/**
 * Created by Riccardo Casatta @RCasatta on 26/11/15.
 */
public class EWWalletService extends Service implements Runnable {
    private static final String TAG = "EWWalletService";

    private static String REGISTER_ALIAS_TAG = "TEWA ";
    private static String EW_MESSAGE_TAG     = "TEW ";

    private final static NetworkParameters PARAMS=MainNetParams.get();
    private final static int EPOCH     = 1447891200;  //19 Novembre 2015 00:00 first EWA 5f362444d23dd258ae1c2b60b1d79cb2c5231fc50df50713a415e13502fc1da9
    private final static int PER_CHUNK = 100;
    public static final Long DUST      = 1000L ;
    public static final Long FEE       = 11000L ;
    public static final String BLOCKCHAIN_FILE = "blockchain";
    public static final String WALLET_FILE     = "wallet";
    public static final String BITCOIN_PATH    = "bitcoin";

    private Set<Address> used     = new HashSet<>();
    private Integer nextMessageId = 0 ;
    private Integer nextChange    = 0 ;

    private Set<Address> all       = new HashSet<>();
    private List<ECKey> messagesId = new ArrayList<>();
    private List<ECKey> changes    = new ArrayList<>();

    private MyBlockchainListener chainListener;
    private MyDownloadListener downloadListener;
    private StoredBlock chainHead;
    private BlockStore blockStore;
    private BlockChain blockChain;
    private PeerGroup peerGroup;
    private Wallet wallet;
    private boolean isSynced = false;

    public Integer getNextMessageId() {
        return nextMessageId;
    }

    public Integer getNextChange() {
        return nextChange;
    }

    private Address getAlias() {  //watch observable
        return changes.get( 0 ).toAddress(PARAMS);
    }

    private Address getCurrent() {  //watch observable
        if(!isSynced || nextChange==0 || nextChange>99) //TODO manage nextChange>99
            return null;
        return changes.get( nextChange-1 ).toAddress(PARAMS);
    }

    private Address getNext() {  //watch observable
        if(!isSynced || nextChange>99)  //TODO manage nextChange>99
            return null;
        return changes.get( nextChange ).toAddress(PARAMS);
    }

    private EWMessageData getNextMessageData() {
        if(!isSynced || nextChange ==0 || nextChange>99) { //TODO manage nextChange>99
            Log.i(TAG, "getNextMessageData returning null " + isSynced + " " + nextChange);
            return null;
        }
        isSynced=false;  //TODO
        EWMessageData ewMessageData = new EWMessageData();
        ewMessageData.setMessageId(messagesId.get(nextMessageId).toAddress(PARAMS));
        ewMessageData.setChange(changes.get(nextChange).toAddress(PARAMS));
        ewMessageData.setInput(changes.get(nextChange - 1));
        nextChange++;
        nextMessageId++;
        return ewMessageData;
    }

    public Transaction createMessageTx(String message) {
        Log.i(TAG,"createMessageTx " +message );

        if(!isSynced || nextChange==0 || nextChange>99 || nextMessageId>99) {
            Log.i(TAG, "sendMessage returning null " + isSynced + " " + nextChange + " " + nextMessageId);   //TODO manage nextChange>99 or nextMessageId>0
            return null;
        }
        EWMessageData ewMessageData= getNextMessageData();
        isSynced=false;  //TODO

        final ECKey input = ewMessageData.getInput();
        Address inputAddress = input.toAddress(PARAMS);
        Transaction newTx = new Transaction(PARAMS);

        List<TransactionOutput> transactionOutputList = getMines(inputAddress);
        Long totalAvailable = 0L;
        for (TransactionOutput to  : transactionOutputList) {
            totalAvailable += to.getValue().getValue();

            newTx.addInput(to);
            Log.i(TAG,"adding input minNonDust=" + to.getMinNonDustValue().longValue() ) ;
        }
        Log.i(TAG,"total available " + totalAvailable);
        Long toSend = totalAvailable-FEE-DUST;
        if(toSend < DUST) {
            Log.i(TAG, "toSend is less than dust");
            return null;
        }


        message = EW_MESSAGE_TAG + message;
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

        Log.i(TAG, "new tx hash: " + newTx.getHash());
        Log.i(TAG, "new tx hex: " + newTxHex);
        return newTx;
    }

    public TransactionBroadcast sendTransaction(Transaction tx) {
        Log.i(TAG,"sendTransaction " +tx );

        TransactionBroadcast transactionBroadcast = peerGroup.broadcastTransaction(tx);
        return transactionBroadcast;
    }
    

    public TransactionBroadcast sendMessage(String message) {
        Log.i(TAG,"sendMessage " +message );
        Transaction newTx = createMessageTx(message);

        TransactionBroadcast transactionBroadcast = peerGroup.broadcastTransaction(newTx);
        Log.i(TAG, "TxBroadcasted " + newTx.getHashAsString() );

        return transactionBroadcast;
    }


    public TransactionBroadcast registerAlias(String aliasName) {
        if(!isSynced || nextChange !=1) {
            Log.i(TAG,"not synced or next change different from zero");
            return null;
        }
        isSynced=false;  //TODO

        ECKey aliasKey = changes.get(0);
        ECKey firstChange = changes.get(1);
        nextChange=2;

        Address aliasAddress = aliasKey.toAddress(PARAMS);
        Log.i(TAG,"aliasAddress is = " + aliasAddress.toString() );
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

        final Address change = firstChange.toAddress(PARAMS);
        Log.i(TAG, "Sending to change " + change.toString() + " " + toSend + " satoshis");


        aliasName = REGISTER_ALIAS_TAG + aliasName;

        final byte[] toWrite = aliasName.getBytes();
        newTx.addOutput(Coin.ZERO,
                new ScriptBuilder()
                        .op(ScriptOpCodes.OP_RETURN)
                        .data(toWrite)
                        .build());
        newTx.addOutput(Coin.valueOf(toSend), change);

        Wallet.SendRequest req = Wallet.SendRequest.forTx(newTx);
        wallet.signTransaction(req);

        final String newTxHex = Bitcoin.transactionToHex(newTx);

        Log.i(TAG, newTxHex);
        TransactionBroadcast transactionBroadcast = peerGroup.broadcastTransaction(newTx);
        Log.i(TAG, "TxBroadcasted " + newTx.getHash().toString());

        return  transactionBroadcast;
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

    private WalletObservable walletObservable = new WalletObservable();
    private EWBinder mBinder= new EWBinder(this, walletObservable);
    @Override
    public IBinder onBind(final Intent intent) {
        Log.i(TAG, ".onBind() " + intent);
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
        startSync();

    }

    public void startSync() {
        new Thread(this).start();
    }



    @Override
    public void run() {
        try {
            org.bitcoinj.core.Context.getOrCreate(PARAMS);
            walletObservable.setState(WalletObservable.State.STARTED);
            walletObservable.notifyObservers();

            Log.i(TAG,".run()");
            final Context context = getApplicationContext();
            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            final String passphrase = sharedPref.getString(Preferences.PASSPHRASE, null);
            if(passphrase==null) {
                Log.i(TAG,"PassphraseIsNull, cannot sync");
                walletObservable.setState(WalletObservable.State.NULL_PASSPHRASE);
                walletObservable.notifyObservers();
                return;
            }
            final byte[] seed = Bitcoin.getEntropyFromPassphrase(passphrase);
            final EWDerivation ewDerivation = new EWDerivation(seed);
            final String alias = Bitcoin.keyToStringAddress( ewDerivation.getAlias() );
            walletObservable.setAlias(alias);
            walletObservable.notifyObservers();

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
                Log.i(TAG,"WalletHeightAtStart " + wallet.getLastBlockSeenHeight() );
                Log.i(TAG, "WalletTxNumberAtStart " + wallet.getTransactionsByTime());
            }
            else {
                Log.i(TAG, "Wallet not exist, creating new");
                List<ECKey> ecKeyList = new ArrayList<>();
                ecKeyList.addAll(messagesId);
                ecKeyList.addAll(changes);
                wallet = Wallet.fromKeys(PARAMS,ecKeyList);
            }


            Log.i(TAG, "wallet key size " + wallet.getKeychainSize());

            Log.i(TAG, "wallet bloom " + wallet.getBloomFilter(1E-5));
            wallet.autosaveToFile(walletFile, 30, TimeUnit.SECONDS, new WalletSaveListener());
            wallet.cleanup();

            final long l = System.currentTimeMillis() - start;
            Log.i(TAG, "My messages id are " + messagesId);
            Log.i(TAG, "My changes are " + changes);
            Log.i(TAG, "Derivation takes " + l);

            blockStore = new SPVBlockStore(PARAMS, blockFile);
            chainHead = blockStore.getChainHead();
            Log.i(TAG, "BlockStoreAtStartHeight " + chainHead.getHeight());

            if (chainHead.getHeight() == 0) {  //first run
                Log.i(TAG, "First run");
                CheckpointManager.checkpoint(PARAMS, Checkpoints.getAsStream(), blockStore, EPOCH);
            }
            wallet.addEventListener(new EWWalletEventListener(walletObservable), Threading.SAME_THREAD);

            blockChain = new BlockChain(PARAMS, wallet, blockStore);
            chainListener = new MyBlockchainListener( all, (EWApplication) getApplication() );
            blockChain.addListener(chainListener);
            peerGroup = new PeerGroup(PARAMS, blockChain);
            peerGroup.addWallet(wallet);  //Unconfirmed wasn't seen because there wasn't this line -> a day spent on this line. Fuck
            //peerGroup.setMaxConnections(40);
            peerGroup.addAddress(InetAddress.getByName("10.106.137.73"));  //DEBUG
            //peerGroup.setMaxConnections(1);
            //peerGroup.addWallet(wallet);
            peerGroup.addPeerDiscovery(new DnsDiscovery(PARAMS));
            peerGroup.addPeerFilterProvider(new MyPeerFilterProvider(changes, messagesId));
            peerGroup.setFastCatchupTimeSecs(EPOCH);
            peerGroup.setDownloadTxDependencies(false);
            downloadListener = new MyDownloadListener(walletObservable);
            peerGroup.startAsync();
            walletObservable.setState(WalletObservable.State.SYNCING);
            walletObservable.notifyObservers();

            Log.i(TAG, "starting download");
            peerGroup.startBlockChainDownload(downloadListener);
            Log.i(TAG, "download started");

        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }

    }

    public void onSynced() {

        List<Transaction> allTx = wallet.getTransactionsByTime();
        Log.i(TAG, "allTx.size()=" + allTx.size());
        for (Transaction tx : allTx) {
            Log.i(TAG,"tx=" + tx);
            final List<TransactionInput> inputs = tx.getInputs();
            for (TransactionInput input : inputs) {
                Address current = input.getScriptSig().getFromAddress(MainNetParams.get());
                if(all.contains(current)) {
                    used.add(current);
                }
            }

            final List<TransactionOutput> outputs = tx.getOutputs();
            for (TransactionOutput output : outputs) {
                Address current = output.getAddressFromP2PKHScript(MainNetParams.get());
                if(all.contains(current)) {
                    used.add(current);
                }
            }

            checkAlias(tx,walletObservable);
        }

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

        //Log.i(TAG, "wallet=" + wallet);
        walletObservable.setState(WalletObservable.State.SYNCED);
        Log.i(TAG, "Changed state");
        walletObservable.setWalletBalance(wallet.getBalance());
        walletObservable.setWalletUnconfirmedBalance(wallet.getBalance(Wallet.BalanceType.ESTIMATED));
        Log.i(TAG, "Changed wallet balance");
        final Address current = nextChange==0 ? getAlias() : getCurrent();
        walletObservable.setCurrent(current);
        Log.i(TAG, "Changed current");

        Log.i(TAG, "Notifying");
        walletObservable.notifyObservers();
        Log.i(TAG, "Ending...");

    }


    private static String EWA_PREFIX = "455741";
    public static void checkAlias(Transaction tx, WalletObservable walletObservable) {
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

    public BlockStore getBlockStore() {
        return blockStore;
    }

    public StoredBlock getChainHead() {
        return chainHead;
    }

    public PeerGroup getPeerGroup() {
        return peerGroup;
    }

    public BlockChain getBlockChain() {
        return blockChain;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void stopSync() {
        blockChain.removeListener(chainListener);
        blockChain.removeWallet(wallet);
        peerGroup.stopAsync();
        wallet.cleanup();
        wallet.reset();

        walletObservable.setState(WalletObservable.State.NOT_STARTED);
        walletObservable.notifyObservers();

        messagesId.clear();
        changes.clear();

        final Context context = getApplicationContext();
        File path;
        if(context!=null)
            path= context.getDir(BITCOIN_PATH, Context.MODE_PRIVATE);
        else
            path = new File("./data/");
        final File blockFile = new File(path, BLOCKCHAIN_FILE );
        final File walletFile = new File(path, WALLET_FILE );
        blockFile.delete();
        walletFile.delete();
    }

}
