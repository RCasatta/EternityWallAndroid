package it.eternitywall.eternitywall.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Wallet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.eternitywall.eternitywall.components.Debug;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.Preferences;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.adapters.DebugListAdapter;
import it.eternitywall.eternitywall.bitcoin.BitcoinNetwork;
import it.eternitywall.eternitywall.wallet.EWWalletService;
import it.eternitywall.eternitywall.wallet.WalletObservable;

public class DebugActivity extends AppCompatActivity implements DebugListAdapter.MessageListAdapterManager {
    private static final String TAG = "DebugActivity";

    private ListView lstDebug;
    DebugListAdapter debugListAdapter;
    private SwipeRefreshLayout swipe;
    private List<Debug> debugs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        Iconify.with(new FontAwesomeModule());

        lstDebug = (ListView) findViewById(R.id.listView);
        swipe = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);


        debugs = new ArrayList<Debug>();
        debugListAdapter = new DebugListAdapter(DebugActivity.this, R.layout.item_debug, debugs, DebugActivity.this);
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
        Log.i(TAG, "loadMoreData");

        final DebugListAdapter debugListAdapter = (DebugListAdapter) lstDebug.getAdapter();
        debugListAdapter.clear();

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String passphrase = sharedPref.getString(Preferences.PASSPHRASE, null);

        if(passphrase==null) {  //you never come here, dialog from preferences prevent this
            debugListAdapter.add(new Debug("Passphrase not set", "set your account to see debug info"));
        } else {
            final EWApplication application = (EWApplication) getApplication();
            final EWWalletService ewWalletService = application.getEwWalletService();
            final WalletObservable walletObservable = application.getWalletObservable();

            if(ewWalletService!=null) {
                PeerGroup peerGroup = ewWalletService.getPeerGroup();
                if (peerGroup != null) {
                    debugListAdapter.add(new Debug("Peer height", "" + peerGroup.getMostCommonChainHeight()));
                    List<Peer> connectedPeers = peerGroup.getConnectedPeers();
                    int size = connectedPeers.size();
                    debugListAdapter.add(new Debug("Connected peers", "" + size));
                    if(size>0) {
                        debugListAdapter.add(new Debug("First connected peer", "" + connectedPeers.get(0).getAddress().toString() ));
                    }
                }

                Wallet wallet = ewWalletService.getWallet();
                if (wallet != null) {
                    debugListAdapter.add(new Debug("Wallet height", "" + wallet.getLastBlockSeenHeight()));
                    debugListAdapter.add(new Debug("Txs in wallet", "" + wallet.getTransactionsByTime().size(), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final EWApplication application = (EWApplication) getApplication();
                            final EWWalletService ewWalletService = application.getEwWalletService();
                            Wallet wallet = ewWalletService.getWallet();
                            final String [] strings= new String[wallet.getTransactionsByTime().size()];
                            for ( int i=0;i<wallet.getTransactionsByTime().size();i++)
                                strings[i]=wallet.getTransactionsByTime().get(0).getHashAsString();

                            android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(DebugActivity.this);
                            alertDialog.setTitle("Txs in wallet");
                            alertDialog.setItems(strings, null);

                            alertDialog.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    clipboard.setText(strings[position]);
                                    Toast.makeText(DebugActivity.this, "Tx hash copied", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                    Toast.makeText(DebugActivity.this, "Tx hash copied", Toast.LENGTH_LONG).show();

                                }
                            });
                            alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ;
                                }
                            });
                            android.support.v7.app.AlertDialog alert = alertDialog.create();
                            alert.show();
                        }
                    }));
                    String bloomString = wallet.getBloomFilter(1E-5).toString();
                    debugListAdapter.add(new Debug("Bloom filter", "" + bloomString.substring(bloomString.indexOf("size"))));
                }

                debugListAdapter.add(new Debug("Next message id", "" + ewWalletService.getNextMessageId()));
                debugListAdapter.add(new Debug("Next change id", "" + ewWalletService.getNextChange()));

                Set<String> stringSet = sharedPref.getStringSet(Preferences.TO_NOTIFY, new HashSet<String>());
                debugListAdapter.add(new Debug("Txs to notify", "" + stringSet.size()));

                debugListAdapter.add(new Debug("State", "" + walletObservable.getState() ));

                try {
                    PackageManager manager = getPackageManager();
                    PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
                    final String version = info.versionName;
                    debugListAdapter.add(new Debug("App version", version ));
                } catch (PackageManager.NameNotFoundException e) {
                    Log.i(TAG,"NameNotFoundException version exception! " + e.getMessage());
                    e.printStackTrace();
                }


                debugListAdapter.add(new Debug("Network",  BitcoinNetwork.getInstance().get().getParams().getId() ));




            }
        }

        debugListAdapter.notifyDataSetChanged();
        swipe.setRefreshing(false);


    }

    @Override
    protected void onPause() {
        super.onPause();
        ((EWApplication)getApplication()).onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((EWApplication)getApplication()).onResume();
    }
}
