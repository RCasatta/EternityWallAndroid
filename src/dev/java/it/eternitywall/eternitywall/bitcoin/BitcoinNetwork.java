package it.eternitywall.eternitywall.bitcoin;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;

import java.io.InputStream;

import it.eternitywall.eternitywall.wallet.Checkpoints;

/**
 * Created by Riccardo Casatta @RCasatta on 25/01/16.
 */
public class BitcoinNetwork {
    private static BitcoinNetwork ourInstance = new BitcoinNetwork();

    public static BitcoinNetwork getInstance() {
        return ourInstance;
    }

    private BitcoinNetwork() {
        context = new Context(MainNetParams.get());
    }

    Context context;

    public Context get() {
        return context;
    }


    public InputStream getCheckPointsStream() {
        return Checkpoints.getAsStream(Checkpoints.MAINNET);
    }
}
