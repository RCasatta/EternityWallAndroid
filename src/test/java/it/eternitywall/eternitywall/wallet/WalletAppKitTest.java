package it.eternitywall.eternitywall.wallet;

import com.google.common.util.concurrent.Service;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.junit.Test;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Riccardo Casatta @RCasatta on 17/12/15.
 */
public class WalletAppKitTest {

    @Test
    public void testWalletApp() throws UnknownHostException {


        WalletAppKit kit = new WalletAppKit(MainNetParams.get(), new File("/tmp"), "" + System.currentTimeMillis()) {
            @Override
            protected void onSetupCompleted() {
                // This is called in a background thread after startAndWait is called, as setting up various objects
                // can do disk and network IO that may cause UI jank/stuttering in wallet apps if it were to be done
                // on the main thread.
                System.out.println("keyChainSize=" + wallet().getKeychainSize());

                if (wallet().getKeychainSize() < 1) {
                    System.out.println("<1");
                    wallet().importKey(new ECKey());
                } else {
                    System.out.println(">1");
                }
            }
        };


        kit.setPeerNodes(new PeerAddress(InetAddress.getByName("10.106.137.73")));


        Service service = kit.startAsync();

        service.awaitRunning();
        kit.peerGroup().addPeerFilterProvider(new EWFilterProvider());
        EWChainListener listener = new EWChainListener();
        kit.chain().addListener(listener);

        Address address = kit.wallet().freshReceiveAddress();
        kit.wallet().addEventListener(new AbstractWalletEventListener() {
            @Override
            public void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance) {
                System.out.println("onCoinsReceived");

                Coin value = tx.getValueSentToMe(w);
                System.out.println("Received tx for " + value.toFriendlyString() + ": " + tx);
                System.out.println("Transaction will be forwarded after it confirms.");
                // Wait until it's made it into the block chain (may run immediately if it's already there).
                //
                // For this dummy app of course, we could just forward the unconfirmed transaction. If it were
                // to be double spent, no harm done. Wallet.allowSpendingUnconfirmedTransactions() would have to
                // be called in onSetupCompleted() above. But we don't do that here to demonstrate the more common
                // case of waiting for a block.
                /*
                Futures.addCallback(tx.getConfidence().getDepthFuture(1), new FutureCallback<Transaction>() {
                    @Override
                    public void onSuccess(Transaction result) {
                        // "result" here is the same as "tx" above, but we use it anyway for clarity.
                        //forwardCoins(result);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                    }
                });
                */
            }
        });
        System.out.println("address= " + address.toString());
        System.out.println("counter=" + listener.counter);

        service.awaitTerminated();

    }


}
