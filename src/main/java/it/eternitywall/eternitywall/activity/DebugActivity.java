package it.eternitywall.eternitywall.activity;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

import it.eternitywall.eternitywall.Debug;
import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.Message;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.adapters.DebugListAdapter;
import it.eternitywall.eternitywall.adapters.MessageListAdapter;
import it.eternitywall.eternitywall.wallet.WalletObservable;

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

        WalletObservable walletObservable = ((EWApplication) getApplication()).getWalletObservable();
        if(walletObservable!=null) {
            if (walletObservable.getCurrent()!=null)
                debugListAdapter.add( new Debug( "Address" , walletObservable.getCurrent().toString() ) );
            else
                debugListAdapter.add( new Debug( "Address" , "" ) );

            if (walletObservable.getHeight()!=null)
                debugListAdapter.add( new Debug( "Height" , walletObservable.getHeight().toString() ) );
            else

                debugListAdapter.add( new Debug( "Height" , "" ) );
        }
        debugListAdapter.notifyDataSetChanged();


    }
}
