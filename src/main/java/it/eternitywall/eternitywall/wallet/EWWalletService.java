package it.eternitywall.eternitywall.wallet;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.CheckpointManager;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionBroadcast;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.WalletTransaction;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
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
import it.eternitywall.eternitywall.bitcoin.BitcoinNetwork;


/**
 * Created by Riccardo Casatta @RCasatta on 26/11/15.
 */
public class EWWalletService extends Service implements Runnable {
    private static final String TAG = "EWWalletService";

    private static String REGISTER_ALIAS_TAG = "EWA ";
    private static String EW_MESSAGE_TAG     = "EW ";

    private final static NetworkParameters PARAMS= BitcoinNetwork.getInstance().get().getParams();
    private final static int EPOCH     = 1447891200;  //19 Novembre 2015 00:00 first EWA 5f362444d23dd258ae1c2b60b1d79cb2c5231fc50df50713a415e13502fc1da9
    private final static int PER_CHUNK = 100;
    public static final Long DUST      = 5000L ;
    public static final Long DONATION  = 10000L ;
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
    private EWDerivation ewDerivation;

    private MyBlockchainListener chainListener;
    private MyDownloadListener downloadListener;
    private StoredBlock chainHead;
    private BlockStore blockStore;
    private BlockChain blockChain;
    private PeerGroup peerGroup;
    private Wallet wallet;

    private WalletObservable walletObservable = new WalletObservable();
    private EWBinder mBinder= new EWBinder(this, walletObservable);


    public Integer getNextMessageId() {
        return nextMessageId;
    }

    public Integer getNextChange() {
        return nextChange;
    }

    private Address getAlias() {  //watch observable
        return changes.get( 0 ).toAddress(PARAMS);
    }

    private Address getCurrent() throws NotSyncedException, NotEnoughAddressException {  //watch observable
        if(!walletObservable.isSynced() || nextChange==0 || nextChange>99) { //TODO manage nextChange>99
            if(nextChange>99)
                throw new NotEnoughAddressException();
            throw new NotSyncedException("notSynced " + walletObservable.isSynced() + " " + nextChange);
        }
        return changes.get( nextChange-1 ).toAddress(PARAMS);
    }

    private Address getNext() throws NotSyncedException, NotEnoughAddressException  {  //watch observable
        if(!walletObservable.isSynced() || nextChange>99) { //TODO manage nextChange>99
            if(nextChange>99)
                throw new NotEnoughAddressException();
            throw new NotSyncedException("notSynced " + walletObservable.isSynced() + " " + nextChange);
        }
        return changes.get( nextChange ).toAddress(PARAMS);
    }

    private EWMessageData getNextMessageData() throws NotSyncedException, NotEnoughAddressException  {
        if(!walletObservable.isSynced() || nextChange ==0 || nextChange>99) { //TODO manage nextChange>99
            final String msg = "getNextMessageData returning null " + walletObservable.isSynced() + " " + nextChange;
            Log.i(TAG, msg);
            if(nextChange>99)
                throw new NotEnoughAddressException();
            throw new NotSyncedException(msg);
        }
        EWMessageData ewMessageData = new EWMessageData();
        ewMessageData.setMessageId(messagesId.get(nextMessageId).toAddress(PARAMS));
        ewMessageData.setChange(changes.get(nextChange).toAddress(PARAMS));
        ewMessageData.setInput(changes.get(nextChange - 1));
        nextChange++;
        nextMessageId++;
        return ewMessageData;
    }

    public Transaction createMessageTx(String message, String answerTo) throws NotSyncedException, NotEnoughAddressException, InsufficientMoneyException, AddressFormatException {
        Log.i(TAG,"createMessageTx " +message + " " + answerTo );

        if(!walletObservable.isSynced() || nextChange==0){
            String msg = "sendMessage returning null " + walletObservable.isSynced() + " " + nextChange + " " + nextMessageId;
            Log.i(TAG, msg);   //TODO manage nextChange>99 or nextMessageId>0
            throw new NotSyncedException(msg);
        } else if (nextChange>99 || nextMessageId>99) {
            String msg = "sendMessage returning null " + walletObservable.isSynced() + " " + nextChange + " " + nextMessageId;
            Log.i(TAG, msg);   //TODO manage nextChange>99 or nextMessageId>0
            throw new NotEnoughAddressException();
        }
        EWMessageData ewMessageData= getNextMessageData();
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean donation = sharedPref.getBoolean(Preferences.DONATION, true);

        final ECKey input = ewMessageData.getInput();
        Address inputAddress = input.toAddress(PARAMS);
        Transaction newTx = new Transaction(PARAMS);

        List<TransactionOutput> transactionOutputList = getMines(inputAddress);
        Long totalAvailable = 0L;
        for (TransactionOutput to  : transactionOutputList) {
            //0.00001
            long value = to.getValue().getValue();
            Log.i(TAG,to.getParentTransactionHash().toString() + ":" + to.getIndex() + " outputvalue:" + value);
            totalAvailable += value;
            newTx.addInput(to);

        }

        Log.i(TAG,"total available " + totalAvailable);
        Long toSend = totalAvailable-FEE-DUST;
        if(donation)
            toSend = toSend-DONATION;

        message = EW_MESSAGE_TAG + message;
        final byte[] toWrite = message.getBytes();

        newTx.addOutput(Coin.valueOf(DUST), ewMessageData.getMessageId());

        if(answerTo!=null) {
                try {
                    final Address addressTo = new Address(PARAMS, answerTo);
                    newTx.addOutput(Coin.valueOf(DUST), addressTo);
                    toSend-=DUST;
                } catch (AddressFormatException e) {
                    String msg = "answerTo is not a valid bitcoin address" + e.getMessage();
                    Log.e(TAG, msg);
                    throw new AddressFormatException(msg);
                }
        }

        if(toSend < DUST) {
            String msg = "toSend is less than dust";
            Log.i(TAG, msg);
            throw new InsufficientMoneyException(Coin.valueOf(toSend));
        }

        newTx.addOutput(Coin.ZERO,
                new ScriptBuilder()
                        .op(ScriptOpCodes.OP_RETURN)
                        .data(toWrite)
                        .build());
        newTx.addOutput(Coin.valueOf(toSend), ewMessageData.getChange());

        if(donation) {
            Log.i(TAG, "Has donation, adding output");
            DeterministicKey todayDonation = ewDerivation.getTodayDonation();
            newTx.addOutput(Coin.valueOf(DONATION), todayDonation.toAddress(PARAMS));
        }

        Wallet.SendRequest req = Wallet.SendRequest.forTx(newTx);
        wallet.signTransaction(req);
        final String newTxHex = Bitcoin.transactionToHex(newTx);

        Log.i(TAG, "newTxHash: " + newTx.getHash());
        Log.i(TAG, "newTxHex: " + newTxHex);
        return newTx;
    }

    public TransactionBroadcast broadcastTransaction(Transaction tx) {
        Log.i(TAG,"sendTransaction " +tx );
        TransactionBroadcast transactionBroadcast = peerGroup.broadcastTransaction(tx);
        walletObservable.setState(WalletObservable.State.PENDING);
        walletObservable.notifyObservers();
        return transactionBroadcast;
    }

    public Transaction createExitTransaction(String bitcoinAddress) throws NotSyncedException, NotEnoughAddressException, InsufficientMoneyException, AddressFormatException {
        Log.i(TAG,"createExitTransaction to " +bitcoinAddress );

        if(!walletObservable.isSynced() || nextChange==0 || nextChange>99 ) {
            final String msg = "createExitTransaction returning null " + walletObservable.isSynced() + " " + nextChange;
            Log.w(TAG, msg);   //TODO manage nextChange>99 or nextMessageId>0
            if(nextChange>99)
                throw new NotEnoughAddressException();
            throw new NotSyncedException(msg);
        }

        Address destination = new Address(PARAMS, bitcoinAddress);

        final Transaction newTx = new Transaction(PARAMS);

        Long totalAvailable = 0L;
        Map<Sha256Hash, Transaction> unspent = wallet.getTransactionPool(WalletTransaction.Pool.UNSPENT);
        for (Map.Entry<Sha256Hash, Transaction> entry : unspent.entrySet()) {
            Transaction current = entry.getValue();
            List<TransactionOutput> outputs = current.getOutputs();

            for (TransactionOutput to : outputs) {
                long value = to.getValue().getValue();

                if(to.isAvailableForSpending() && value>0 && to.isMine(wallet)) {
                    Log.i(TAG, "adding spendable output " + to);
                    newTx.addInput(to);
                    totalAvailable += value;
                }
            }

        }

        Log.i(TAG,"total available " + totalAvailable);
        Long toSend = totalAvailable-FEE;
        if(toSend < DUST) {
            final String msg = "toSend is less than dust, exiting " + toSend;
            Log.i(TAG, msg);
            throw new InsufficientMoneyException( Coin.valueOf(DUST-toSend) );
        }

        newTx.addOutput(Coin.valueOf(toSend), destination );

        Wallet.SendRequest req = Wallet.SendRequest.forTx(newTx);
        wallet.signTransaction(req);
        final String newTxHex = Bitcoin.transactionToHex(newTx);

        Log.i(TAG, "new tx hash: " + newTx.getHash());
        Log.i(TAG, "new tx hex: " + newTxHex);
        return newTx;
    }

    public Transaction registerAlias(String aliasName) throws NotSyncedException, InsufficientMoneyException {
        if(!walletObservable.isSynced() || nextChange != 1) {
            final String msg = "not synced or next change different from zero " + walletObservable.isSynced() + " " + nextChange;
            Log.i(TAG, msg);
            throw new NotSyncedException(msg);
        }

        ECKey aliasKey = changes.get(0);
        ECKey firstChange = changes.get(1);
        nextChange=2;

        Address aliasAddress = aliasKey.toAddress(PARAMS);
        Log.i(TAG,"aliasAddress is = " + aliasAddress.toString() );
        Transaction newTx = new Transaction(PARAMS);

        List<TransactionOutput> transactionOutputList = getMines(aliasAddress);  //adfgsdg
        Long totalAvailable = 0L;
        for (TransactionOutput to  : transactionOutputList) {
            totalAvailable += to.getValue().getValue();
            newTx.addInput(to);
        }
        Long toSend = totalAvailable-FEE-DUST;
        if(toSend < DUST) {
            final String msg = "toSend is less than dust " + toSend;
            Log.i(TAG, msg);
            throw new InsufficientMoneyException( Coin.valueOf(DUST-toSend) );
        }

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

        return  newTx;
    }

    private List<TransactionOutput> getMines(Address address) {

        Map<Sha256Hash, Transaction> unspent = wallet.getTransactionPool(WalletTransaction.Pool.UNSPENT);
        List<TransactionOutput> transactionOutputList= new ArrayList<>();
        for (Map.Entry<Sha256Hash, Transaction> entry : unspent.entrySet()) {
            Transaction current = entry.getValue();
            List<TransactionOutput> outputs = current.getOutputs();
            for (TransactionOutput o : outputs) {
                if(o.isAvailableForSpending()) {
                    Address a = o.getAddressFromP2PKHScript(PARAMS);
                    if(address.equals(a)) {
                        transactionOutputList.add(o);
                    }
                }
            }
        }
        return transactionOutputList;
    }

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
            Log.i(TAG,"my network is " + BitcoinNetwork.getInstance().get().getParams().getId() );
            final boolean isRegtest = BitcoinNetwork.getInstance().get().getParams().getId().equals(RegTestParams.get().getId());

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
            ewDerivation = new EWDerivation(seed);
            final String alias = Bitcoin.keyToStringAddress( ewDerivation.getAlias() );
            String aliasNamePref = sharedPref.getString(Preferences.ALIAS_NAME, null);
            if(aliasNamePref!=null) {
                Log.i(TAG,"aliasNamePref=" + aliasNamePref);
                walletObservable.setAliasName(aliasNamePref);
            }
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
            wallet.setAcceptRiskyTransactions(true);
            //wallet.cleanup();
            final long l = System.currentTimeMillis() - start;
            Log.i(TAG, "My messages id are " + messagesId);
            Log.i(TAG, "My changes are " + changes);
            Log.i(TAG, "Derivation takes " + l);

            blockStore = new SPVBlockStore(PARAMS, blockFile);
            chainHead = blockStore.getChainHead();
            Log.i(TAG, "BlockStoreAtStartHeight " + chainHead.getHeight());

            if ( chainHead.getHeight() == 0 && !isRegtest ) {  //first run
                Log.i(TAG, "First run");
                CheckpointManager.checkpoint(PARAMS, BitcoinNetwork.getInstance().getCheckPointsStream() , blockStore, EPOCH);
            }
            wallet.addEventListener(new EWWalletEventListener(walletObservable), Threading.SAME_THREAD);
            //wallet.setRiskAnalyzer(new DefaultRiskAnalysis.Analyzer());

            blockChain = new BlockChain(PARAMS, wallet, blockStore);
            chainListener = new MyBlockchainListener( all, (EWApplication) getApplication() );
            blockChain.addListener(chainListener);
            peerGroup = new PeerGroup(PARAMS, blockChain);
            peerGroup.addWallet(wallet);  //Unconfirmed wasn't seen because there wasn't this line -> a day spent on this line. Fuck
            //peerGroup.setMaxConnections(40);

            if(isRegtest) {
                Log.i(TAG,"I am in regtest");
                peerGroup.addAddress( InetAddress.getByName( "192.168.1.233" ) );
            } else {
                final Set<String> stringSet = sharedPref.getStringSet(Preferences.NODES, new HashSet<String>());
                if(stringSet.size()>0) {
                    Log.i(TAG,"There are personal nodes " + stringSet.size() );
                    for (String current : stringSet) {
                        boolean portOpen = isPortOpen(current, 8333, 1000);
                        if(portOpen) {
                            Log.i(TAG,"port is reachable! " + current + ":8333" );
                            peerGroup.addAddress(InetAddress.getByName(current));
                        } else {
                            Log.i(TAG,"port is unreachable! " + current + ":8333" );
                        }
                    }
                } else {
                    Log.i(TAG,"There are no personal nodes");
                }
                peerGroup.addPeerDiscovery(new DnsDiscovery(PARAMS));
                peerGroup.setFastCatchupTimeSecs(EPOCH);

            }

            //peerGroup.setMaxConnections(1);
            //peerGroup.addWallet(wallet);
            peerGroup.addPeerFilterProvider(new MyPeerFilterProvider(changes, messagesId));
            peerGroup.setDownloadTxDependencies(false);
            downloadListener = new MyDownloadListener(walletObservable);
            peerGroup.startAsync();
            walletObservable.setState(WalletObservable.State.SYNCING);

            walletObservable.notifyObservers();

            Log.i(TAG, "starting download");
            peerGroup.startBlockChainDownload(downloadListener);
            Log.i(TAG, "download started");

        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
            e.printStackTrace();
        }

    }


    public static boolean isPortOpen(final String ip, final int port, final int timeout) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.close();
            return true;
        }

        catch(ConnectException ce){
            ce.printStackTrace();
            return false;
        }

        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void onSynced() {

        refreshNextsAndAlias();

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
        Address current;
        try {
            current = nextChange == 0 ? getAlias() : getCurrent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        walletObservable.setCurrent(current);

        //walletObservable.setAliasName("Test");  //TODO DEBUG

        Log.i(TAG, "Changed current");

        Log.i(TAG, "Notifying");
        walletObservable.notifyObservers();
        Log.i(TAG, "Ending...");

    }

    private Set<Integer> processed = new HashSet<>();
    public synchronized void refreshNextsAndAlias() {
        List<Transaction> allTx = wallet.getTransactionsByTime();
        final int size = allTx.size();
        if(processed.contains(size))
            return;
        Log.i(TAG, "refreshNextsAndAlias allTx.size()=" + size);

        for (Transaction tx : allTx) {
            //Log.i(TAG,"tx=" + tx);
            final List<TransactionInput> inputs = tx.getInputs();
            for (TransactionInput input : inputs) {
                try {
                    Address current = input.getScriptSig().getFromAddress(PARAMS);
                    if (all.contains(current)) {
                        used.add(current);
                    }
                } catch (ScriptException e) {   //needed for p2sh input, eg:
                    Log.i(TAG,"p2sh input");

                }
            }

            final List<TransactionOutput> outputs = tx.getOutputs();
            for (TransactionOutput output : outputs) {
                Address current = output.getAddressFromP2PKHScript(PARAMS);
                if(all.contains(current)) {
                    used.add(current);
                }
            }

            String aliasName = checkAlias(tx,walletObservable);
            if(aliasName!=null) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                sharedPref.edit().putString(Preferences.ALIAS_NAME,aliasName).apply();
            }
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
        processed.add(size);
    }


    public static boolean isEWMessage(Transaction tx) {
        List<TransactionOutput> outputs = tx.getOutputs();
        for (TransactionOutput out : outputs) {
            byte[] scriptBytes = out.getScriptBytes();
            if(scriptBytes.length>0) {
                String hexString = Hex.toHexString(scriptBytes);
                if(hexString.startsWith("6a")) {
                    hexString=hexString.substring(2);
                    if( hexString.startsWith("455720") || hexString.substring(2).startsWith("455720") ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String EWA_PREFIX = "455741";
    public String checkAlias(Transaction tx, WalletObservable walletObservable) {
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

                            if(tx.getConfidence().getConfidenceType() == TransactionConfidence.ConfidenceType.PENDING ) {
                                Log.i(TAG, "alias tx is pending");
                                walletObservable.setUnconfirmedAliasName(aliasName);
                            } else {
                                walletObservable.setUnconfirmedAliasName(null);
                                walletObservable.setAliasName(aliasName);
                                Log.i(TAG, "alias tx is confirmed!");
                            }
                            walletObservable.notifyObservers();

                            return aliasName;
                        }
                    }
                }
            }
        }

        return null;
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
        if (blockChain!=null) {
            blockChain.removeListener(chainListener);
            blockChain.removeWallet(wallet);
        }
        if (peerGroup!=null) {
            if (peerGroup.isRunning())
                peerGroup.stop();
            peerGroup.removeWallet(wallet);
        }
        if (wallet!=null) {
            wallet.cleanup();
            wallet.reset();
            wallet.clearTransactions(0);
        }
        if (walletObservable!=null) {
            walletObservable.reset();
            walletObservable.notifyObservers();
        }

        // Clear global variables
        if(all!=null)
            all.clear();
        if (messagesId!=null)
            messagesId.clear();
        if(changes!=null)
            changes.clear();
        ewDerivation=null;
        used     = new HashSet<>();
        nextMessageId = 0 ;
        nextChange    = 0 ;

        // Clear files
        final Context context = getApplicationContext();
        File path;
        if(context!=null)
            path= context.getDir(BITCOIN_PATH, Context.MODE_PRIVATE);
        else
            path = new File("./data/");
        final File blockFile = new File(path, BLOCKCHAIN_FILE );
        final File walletFile = new File(path, WALLET_FILE );
        boolean blockDeleted  = blockFile.delete();
        boolean walletDeleted =  walletFile.delete();
        Log.i(TAG, "blockDeleted: "  + blockDeleted);
        Log.i(TAG, "walletDeleted: " + walletDeleted);

        try {
            blockDeleted  = blockFile.getCanonicalFile().delete();
            walletDeleted =  walletFile.getCanonicalFile().delete();
            Log.i(TAG, "blockDeleted: "+ blockDeleted+" - length: "+ blockFile.getCanonicalFile().length() );
            Log.i(TAG, "walletDeleted: " + walletDeleted+" - length: "+ walletFile.getCanonicalFile().length() );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removePasshrase() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.edit().remove(Preferences.PASSPHRASE).remove(Preferences.PIN).remove(Preferences.TO_NOTIFY).remove(Preferences.ALIAS_NAME).commit();
    }

}
