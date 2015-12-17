package it.eternitywall.eternitywall.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Wallet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.eternitywall.eternitywall.Debug;
import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.Preferences;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.adapters.DebugListAdapter;
import it.eternitywall.eternitywall.wallet.EWWalletService;

public class DebugActivity extends AppCompatActivity implements DebugListAdapter.MessageListAdapterManager {


    private ListView lstDebug;
    DebugListAdapter debugListAdapter;
    private SwipeRefreshLayout swipe;
    private List<Debug> debugs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        lstDebug = (ListView) findViewById(R.id.listView);
        swipe = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);

        debugs = new ArrayList<Debug>();
        debugListAdapter = new DebugListAdapter(DebugActivity.this, R.layout.item_message, debugs, DebugActivity.this);
        lstDebug.setAdapter(debugListAdapter);
        loadMoreData();

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                debugs = new ArrayList<Debug>();
                loadMoreData();
            }


        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_debug, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void loadMoreData() {

        DebugListAdapter debugListAdapter = (DebugListAdapter) lstDebug.getAdapter();
        debugListAdapter.clear();

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String passphrase = sharedPref.getString(Preferences.PASSPHRASE, null);

        if(passphrase==null) {  //you never come here, dialog from preferences prevent this
            debugListAdapter.add(new Debug("Passphrase not set", "set your account to see debug info"));
        } else {
            final EWApplication application = (EWApplication) getApplication();
            final EWWalletService ewWalletService = application.getEwWalletService();
            //final WalletObservable walletObservable = application.getWalletObservable();

            if(ewWalletService!=null) {
                PeerGroup peerGroup = ewWalletService.getPeerGroup();
                if (peerGroup != null) {
                    debugListAdapter.add(new Debug("Peer height", "" + peerGroup.getMostCommonChainHeight()));
                    debugListAdapter.add(new Debug("Connected peers", "" + peerGroup.getConnectedPeers().size()));

                }

                Wallet wallet = ewWalletService.getWallet();
                if (wallet != null) {
                    debugListAdapter.add(new Debug("Wallet height", "" + wallet.getLastBlockSeenHeight()));
                    debugListAdapter.add(new Debug("Txs in wallet", "" + wallet.getTransactionsByTime().size()));
                    String bloomString = wallet.getBloomFilter(1E-5).toString();
                    debugListAdapter.add(new Debug("Bloom filter", "" + bloomString.substring(bloomString.indexOf("size"))));
                }

                debugListAdapter.add(new Debug("Next message id", "" + ewWalletService.getNextMessageId()));
                debugListAdapter.add(new Debug("Next change id", "" + ewWalletService.getNextChange()));

                Set<String> stringSet = sharedPref.getStringSet(Preferences.TO_NOTIFY, new HashSet<String>());
                debugListAdapter.add(new Debug("Txs to notify", "" + stringSet.size()));

            }
        }








        debugListAdapter.notifyDataSetChanged();
        swipe.setRefreshing(false);

    }
}
