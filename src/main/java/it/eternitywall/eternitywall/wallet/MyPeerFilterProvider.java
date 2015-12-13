package it.eternitywall.eternitywall.wallet;

import org.bitcoinj.core.BloomFilter;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.PeerFilterProvider;

import java.util.List;

/**
 * Created by Riccardo Casatta @RCasatta on 25/11/15.
 */
public class MyPeerFilterProvider  implements PeerFilterProvider{
    final int EPOCH = 1448150400;  //22 Novembre 2015 00:00 first EW signed message

    private List<ECKey> changes;
    private List<ECKey> messagesId;
    private int bloomSize;

    public MyPeerFilterProvider(List<ECKey> changes, List<ECKey> messagesId) {
        this.changes = changes;
        this.messagesId = messagesId;
        this.bloomSize = changes.size() + messagesId.size();
    }

    @Override
    public long getEarliestKeyCreationTime() {
        return EPOCH;
    }

    @Override
    public void beginBloomFilterCalculation() {

    }

    @Override
    public int getBloomFilterElementCount() {
        return bloomSize;
    }

    @Override
    public BloomFilter getBloomFilter(int size, double falsePositiveRate, long nTweak) {
        System.out.println("size " + size + " falsePositiveRate " + falsePositiveRate + " nTweak " + nTweak);
        final BloomFilter res = new BloomFilter(size, falsePositiveRate, nTweak);
        System.out.println("BFData" + res );
        for (ECKey current : messagesId) {
            res.insert(current);  //this way add also the public key, which I am not using to create tx
            //res.insert(current.getPubKeyHash());
        }
        for (ECKey current : changes) {
            res.insert(current);  //this way add also the public key, which I am not using to create tx
            //res.insert(current.getPubKeyHash());
        }
        return res;
    }

    @Override
    public boolean isRequiringUpdateAllBloomFilter() {
        return false;
    }

    @Override
    public void endBloomFilterCalculation() {

    }

}