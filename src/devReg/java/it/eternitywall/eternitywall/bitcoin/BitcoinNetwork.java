package it.eternitywall.eternitywall.bitcoin;

import org.bitcoinj.core.Context;
import org.bitcoinj.params.RegTestParams;

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
        context = new Context(RegTestParams.get());
    }

    Context context;

    public Context get() {
        return context;
    }

    public InputStream getCheckPointsStream() {
        return Checkpoints.getAsStream("");
    }
}
