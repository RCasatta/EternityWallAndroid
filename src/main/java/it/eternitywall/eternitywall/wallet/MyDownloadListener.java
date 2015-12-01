package it.eternitywall.eternitywall.wallet;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.DownloadProgressTracker;
import org.bitcoinj.core.FilteredBlock;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerAddress;

import java.util.Set;

/**
 * Created by Riccardo Casatta @RCasatta on 26/11/15.
 */
public class MyDownloadListener extends DownloadProgressTracker {
    private int size=0;
    private long end;
    private long start;
    private int originalBlocksLeft = -1;
    private int lastPercent = 0;

    private WalletObservable walletObservable;

    public MyDownloadListener(WalletObservable walletObservable) {
        this.walletObservable = walletObservable;
    }

    @Override
    public void onChainDownloadStarted(Peer peer, int blocksLeft) {
        super.onChainDownloadStarted(peer, blocksLeft);

        System.out.println("onChainDownloadStarted blocksLeft=" + blocksLeft);

        if (originalBlocksLeft == -1)
            originalBlocksLeft = blocksLeft;
        if(blocksLeft==0)
            doneDownload();
    }

    @Override
    protected void startDownload(int blocks) {
        super.startDownload(blocks);
        start= System.currentTimeMillis();

    }

    @Override
    protected void doneDownload() {
        super.doneDownload();
        end= System.currentTimeMillis();
        System.out.println("Done download, it tooks " + (end - start));
        walletObservable.setState(WalletObservable.State.DOWNLOADED);

    }

    public long getDownloadTime() {
        return end-start;
    }


    @Override
    public void onBlocksDownloaded(Peer peer, Block block, FilteredBlock filteredBlock, int blocksLeft) {
        super.onBlocksDownloaded(peer, block, filteredBlock, blocksLeft);
         size+=block.getMessageSize();

        double pct = 100.0 - (100.0 * (blocksLeft / (double) originalBlocksLeft));
        if ((int) pct != lastPercent) {
            lastPercent = (int) pct;
            walletObservable.setPercSync(lastPercent);
        }
    }


    public int getSize() {
        return size;
    }

    public int getLastPercent() {
        return lastPercent;
    }

    @Override
    public void onPeersDiscovered(Set<PeerAddress> peerAddresses) {

    }

}
