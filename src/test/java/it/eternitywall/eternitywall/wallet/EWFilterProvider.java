package it.eternitywall.eternitywall.wallet;

import org.bitcoinj.core.BloomFilter;
import org.bitcoinj.core.PeerFilterProvider;
import org.spongycastle.util.encoders.Hex;

import it.eternitywall.eternitywall.bitcoin.Bitcoin;


/**
 * Created by Riccardo Casatta @RCasatta on 21/12/15.
 */
public class EWFilterProvider implements PeerFilterProvider {
    final int EPOCH = 1448150400;  //22 Novembre 2015 00:00 first EW signed message
    private int counter=0;

    @Override
    public long getEarliestKeyCreationTime() {
        return EPOCH;
    }

    @Override
    public void beginBloomFilterCalculation() {

    }

    @Override
    public int getBloomFilterElementCount() {
        return counter;
    }

    @Override
    public BloomFilter getBloomFilter(int size, double falsePositiveRate, long nTweak) {
        final BloomFilter res = new BloomFilter(size, falsePositiveRate, nTweak);
        byte[] object = Bitcoin.fromHex("455720");
        System.out.println("hex:" + Hex.toHexString(object));
        res.insert(object);
        counter++;
        return res;
    }

    @Override
    public boolean isRequiringUpdateAllBloomFilter() {
        return true;
    }

    @Override
    public void endBloomFilterCalculation() {

    }

}
