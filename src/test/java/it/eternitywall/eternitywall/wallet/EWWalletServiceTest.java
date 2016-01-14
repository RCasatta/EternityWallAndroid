package it.eternitywall.eternitywall.wallet;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.BloomFilter;
import org.bitcoinj.core.CheckpointManager;
import org.bitcoinj.core.DownloadProgressTracker;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.SPVBlockStore;
import org.junit.Test;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.eternitywall.eternitywall.bitcoin.Bitcoin;

import static java.lang.Math.log;
import static java.lang.Math.pow;

/**
 * Created by Riccardo Casatta @RCasatta on 28/11/15.
 */
public class EWWalletServiceTest {

    @Test
    public void testWallet() {
        String passphrase = Bitcoin.getNewMnemonicPassphrase();

        EWWalletService ewWalletService = new EWWalletService();
        //ewWalletService.startSync();
    }

    @Test
    public void testHashCode() {
        List<String> ciao = new ArrayList<>();
        System.out.println(ciao.hashCode());
        ciao.add("ciao");
        System.out.println(ciao.hashCode());
    }


    @Test
    public void testInetAddress() throws UnknownHostException {

        InetAddress address = InetAddress.getByName("1.1.1");
        //InetAddress address2 = InetAddress.getByName("46e467sfgh&&&");
        InetAddress address3 = InetAddress.getByName("21.1.1");


    }

    @Test
    public void testBloomNumber() {
        int dataLength= 18271;
        int elements = 6000;

        final int i = dataLength * 8;
        System.out.println(i);
        final double v = i / (double) elements;
        System.out.println(v);
        final double v1 = v * log(2);
        int hashFuncs = (int) v1;
        System.out.println(hashFuncs);

        System.out.println();

        final double pow = pow(log(2), 2);
        System.out.println(pow);

        final double log = log(1.0E-5);
        System.out.println(log);

        int size = (int)(-1  / pow * elements * log);
        System.out.println(size);


    }


    @Test
    public void testBloomFilter() {
        final List<Integer> integers = Arrays.asList(10, 100, 1000, 2000, 5000, 10000);
        for (Integer i : integers) {
            final BloomFilter res = new BloomFilter(i, 1.0E-5, System.currentTimeMillis());
            System.out.println("" + res );

        }
    }

    private int EPOCH = 1448150400;  //22 Novembre 2015 00:00 first EW signed message
    private NetworkParameters PARAMS = MainNetParams.get();
    @Test
    public void btcWalletTestWithCheckpoint() throws Exception {
        final byte[] seed = Bitcoin.getEntropyFromPassphrase("sweet field bless symptom play cherry fault curious mechanic cross gift thunder");
        EWDerivation ewDerivation = new EWDerivation(seed);

        final MainNetParams params = MainNetParams.get();
        final String blockpath  = "./spvblockstore"  + System.currentTimeMillis();
        //final File blockChainFile = new File(getDir("blockstore_" + receivingId, Context.MODE_PRIVATE), "blockchain.spvchain");
        //final String pathname = "./spvblockstore" + System.currentTimeMillis();
        //final String pathname = "./spvblockstore1448448596283";

        File blockFile = new File(blockpath);

        BlockStore blockStore = new SPVBlockStore(params, blockFile);
        StoredBlock chainHead = blockStore.getChainHead();
        if(chainHead.getHeight()==0) {  //first run
            CheckpointManager.checkpoint(PARAMS, Checkpoints.getAsStream(), blockStore, EPOCH);
        }
        //final StoredBlock checkpointBefore = manager.getCheckpointBefore(startFrom);
        //blockStore.setChainHead(checkpointBefore);

        Wallet wallet = new Wallet(PARAMS);

        BlockChain chain = new BlockChain(params, wallet, blockStore);
        //BlockChain chain = new BlockChain(params, blockStore);
        PeerGroup peerGroup = new PeerGroup(params, chain);
        peerGroup.addAddress(InetAddress.getByName("10.106.137.73"));
        peerGroup.setMaxConnections(1);
        peerGroup.setDownloadTxDependencies(false);

        //peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        //EWFilterProvider provider = new EWFilterProvider( );
        //System.out.println("provider " + provider);

        //peerGroup.addPeerFilterProvider(provider);

        DownloadProgressTracker downloadProgressTracker = new DownloadProgressTracker();


        EWChainListener listener = new EWChainListener();
        chain.addListener(listener);
        //peerGroup.setMaxConnections(10);

        peerGroup.setFastCatchupTimeSecs(EPOCH);  //24 Giugno 2015 12:00
        peerGroup.startBlockChainDownload(downloadProgressTracker);


        long startDownload = System.currentTimeMillis();
        peerGroup.downloadBlockChain();
        final long endDownload = System.currentTimeMillis() - startDownload;
        System.out.println("Download run for " + endDownload);

        System.out.println("---------FINISH----------");
        System.out.println("peerGroup.getConnectedPeers()=" + peerGroup.getConnectedPeers());
        System.out.println("blockpath " + blockpath);
        System.out.println("chain.getBestChainHeight()=" + chain.getBestChainHeight() );
        System.out.println("chainHeadHeightAtBeginning=" + chainHead.getHeight() );

        System.out.println("TX# " + listener.counter);


        //System.out.println("checkpointBefore.getHeight()=" + checkpointBefore.getHeight() );
        //System.out.println("Derivation took " + l);

        blockStore.close();
        //wallet.addFollowingAccountKeys(a,2);

    }


}