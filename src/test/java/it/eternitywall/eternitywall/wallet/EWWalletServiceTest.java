package it.eternitywall.eternitywall.wallet;

import org.bitcoinj.core.BloomFilter;
import org.junit.Test;

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
        ewWalletService.startSync();
    }

    @Test
    public void testHashCode() {
        List<String> ciao = new ArrayList<>();
        System.out.println(ciao.hashCode());
        ciao.add("ciao");
        System.out.println(ciao.hashCode());

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

}