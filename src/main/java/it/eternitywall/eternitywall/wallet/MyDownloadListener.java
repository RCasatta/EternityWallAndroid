package it.eternitywall.eternitywall.wallet;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.DownloadProgressTracker;
import org.bitcoinj.core.FilteredBlock;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerAddress;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Riccardo Casatta @RCasatta on 26/11/15.
 */
public class MyDownloadListener extends DownloadProgressTracker {
    private int size=0;
    private long end;
    private long start;

    private CountDownLatch latch;

    public MyDownloadListener(CountDownLatch latch) {
        this.latch = latch;
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
        latch.countDown();
    }

    public long getDownloadTime() {
        return end-start;
    }

    @Override
    public void onBlocksDownloaded(Peer peer, Block block, FilteredBlock filteredBlock, int blocksLeft) {
        super.onBlocksDownloaded(peer, block, filteredBlock, blocksLeft);
         size+=block.getMessageSize();
    }

    public int getSize() {
        return size;
    }

    @Override
    public void onPeersDiscovered(Set<PeerAddress> peerAddresses) {

    }

}
