package it.eternitywall.eternitywall;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.eternitywall.eternitywall.adapters.MessageListAdapter;


public class MainActivity extends ActionBarActivity implements MessageListAdapter.MessageListAdapterManager, SearchView.OnQueryTextListener, SearchView.OnCloseListener, PopupMenu.OnMenuItemClickListener {

    private static final int REQUEST_CODE = 8274;
    private static final String TAG = "MainActivity";

    private ListView lstMessages;
    private ProgressBar progress;
    private SwipeRefreshLayout swipe;
    private SearchView searchView;

    private String search;
    private String sortby;
    private String cursor;
    private List<Message> messages;
    private Integer inQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lstMessages = (ListView) findViewById(R.id.lstMessages);
        progress = (ProgressBar) findViewById(R.id.progress);
        swipe = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);

        findViewById(R.id.payButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, WriteActivity.class);
                startActivity(i);
            }
        });

        /**
         * EW PROTOCOL
         *
         * input pau the fee
         * EWAxenoky set nick "xenoky" to first input of transaction
         *
         *
         * first input is the sender
         * other input are ignored
         * first output is the message id (DUST value)
         * second output (optional) if the message id answering to
         * OP_RETURN EW message
         * other outputs are ignored
         *
         * The following API endpoints are available:

         http://eternitywall.it/?format=json&cursor=[cursor]
         returns a json with Eternity Wall messages.
         [cursor] - Optional, if provided the message returned start from cursor (the value of cursor is provided in the json)

         http://eternitywall.it/bitcoinform?format=json&text=[text]&reply=[replyid]&source=[source]
         returns a json with a bitcoin address to pay to write on the wall.
         [text] - The text of the message
         [replyid] - Optional, in case of replying to a message.
         [source] - Optional, specify source of message (your app name).

         https://eternitywall.appspot.com/v1/notify?email=[email]&hash=[hash]&address=[address]&subscribe=[subscribe]&notifyreply=[notifyreply]
         if called, user will be notified with an email when the message has been written (block confirmed) and optionally to receive notification when there are replies
         [email] - us
         [hash] - Not required if [address] is specified. hash of the tx containing the message
         [address] - Not required if [hash] is specified. bitcoin address paid to write the message
         [subscribe] - if equal to true, it will also register user to the mailing list
         [notifyreply] - if equal to true, email receive notification also of reply to message

         http://eternitywall.it/m/c09e8df0df522c0d7ffe027a5d8b8d94f8bc237410e8b1242b32c9967070f4e3?format=json
         return detail info on specified message (with father message (if any), current message and answer list (if any)

         *
         */

        messages = new ArrayList<Message>();
        cursor = null;
        search = null;
        sortby = null;
        inQueue = null;
        loadMoreData();

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                messages = new ArrayList<Message>();
                cursor = null;
                search = null;
                sortby = null;
                inQueue = null;
                loadMoreData();
            }


        });
        Intent intent = getIntent();
        if(intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEND) && intent.getType().equals("text/plain")) {

            intent.setAction(null);

            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            Intent i = new Intent(MainActivity.this, WriteActivity.class);
            i.putExtra("sharedText", sharedText);
            startActivity(i);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Get the root inflator.
        LayoutInflater baseInflater = (LayoutInflater)getBaseContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Inflate your custom view.

        Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");

        TextView txtOrder=new TextView(MainActivity.this);
        txtOrder.setPadding(0, 0, (int) getResources().getDimension(R.dimen.activity_horizontal_margin), 0);
        txtOrder.setText(getResources().getString(R.string.action_order));
        txtOrder.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Large);
        txtOrder.setTypeface(font);
        menu.findItem(R.id.action_order).setActionView(txtOrder);
        menu.findItem(R.id.action_order).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, item.getActionView());
                popupMenu.setOnMenuItemClickListener(MainActivity.this);
                popupMenu.inflate(R.menu.menu_order);
                popupMenu.show();
                return true;
            }
        });
        txtOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                popupMenu.setOnMenuItemClickListener(MainActivity.this);
                popupMenu.inflate(R.menu.menu_order);
                popupMenu.show();
            }
        });

        /*TextView txtCloud=new TextView(MainActivity.this);
        txtCloud.setPadding(0,0,(int) getResources().getDimension(R.dimen.activity_horizontal_margin),0);
        txtCloud.setText(getResources().getString(R.string.action_cloud));
        txtCloud.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Large);
        txtCloud.setTypeface(font);
        menu.findItem(R.id.action_cloud).setActionView(txtCloud);
*/

        //SearchManager searchManager = (SearchManager)         getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

        //searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //searchView.setSubmitButtonEnabled(true);
        //searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_order) {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, item.getActionView());
            //popupMenu.setOnMenuItemClickListener(MainActivity.this);
            popupMenu.inflate(R.menu.menu_order);
            popupMenu.show();
            return true;
        }else if (id == R.id.action_search) {
            //searchable element
            return true;
        }/*else if (id == R.id.action_cloud) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast.makeText(this, "requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data , Toast.LENGTH_LONG).show();
    }

    @Override
    public void loadMoreData() {
        AsyncTask t = new AsyncTask() {

            private boolean ok = false;
            private List<Message> mMessages = new ArrayList<>();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if(!swipe.isRefreshing())
                    progress.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                progress.setVisibility(View.INVISIBLE);
                swipe.setRefreshing(false);
                if(ok) {
                    if(messages != null && !messages.isEmpty()) {
                        MessageListAdapter messageListAdapter = (MessageListAdapter) lstMessages.getAdapter();
                        for (int i=0;i<mMessages.size();i++) {
                            messageListAdapter.add(mMessages.get(i));
                            messageListAdapter.notifyDataSetChanged();
                        }
                    }
                    else {
                        MessageListAdapter messageListAdapter = new MessageListAdapter(MainActivity.this, R.layout.item_message, messages, inQueue, MainActivity.this);
                        lstMessages.setAdapter(messageListAdapter);
                        for (int i=0;i<mMessages.size();i++) {
                            messageListAdapter.add(mMessages.get(i));
                            messageListAdapter.notifyDataSetChanged();
                        }

                    }
                }
                else {
                    //succhia!
                    Toast.makeText(MainActivity.this, getString(R.string.err_check_internet), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected Object doInBackground(Object[] params) {
                Optional<String> json=null;
                if (search!=null) {
                    if(messages==null || messages.isEmpty())
                        json = cursor == null ? Http.get("http://eternitywall.it/search?format=json&q=" + search) : Http.get("http://eternitywall.it/?format=json&cursor=" + cursor + "&q=" + search);
                    else
                        ok = true;
                }else if (sortby!=null){
                    if(messages==null || messages.isEmpty())
                        json = Http.get("http://eternitywall.it/sortby/"+sortby+"?format=json");
                    else
                        ok = true;
                } else
                    json = cursor == null ? Http.get("http://eternitywall.it/?format=json") : Http.get("http://eternitywall.it/?format=json&cursor=" + cursor);


                if(json!=null && json.isPresent()) {
                    try {
                        String jstring = json.get();
                        JSONObject jo = new JSONObject(jstring);

                        try {
                            cursor = jo.getString("next");
                            if (jo.has("messagesInQueue")) {
                                inQueue = jo.getInt("messagesInQueue");
                            }
                        } catch (Exception ex){
                            cursor=null;
                            ex.printStackTrace();
                        }

                        JSONArray ja = jo.getJSONArray("messages");

                        for(int m=0; m<ja.length(); m++) {
                            Message message = Message.buildFromJson(ja.getJSONObject(m));
                            mMessages.add(message);
                            Log.i(TAG, message.toString());
                        }

                        //sort by reverse timestamp (optional?)
                        Collections.sort(mMessages);
                        ok = true;
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                return null;
            }
        };
        t.execute();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // OnQueryTextSubmit was called twice, so clean
        searchView.setIconified(true);
        searchView.clearFocus();
        search = query;
        sortby=null;
        cursor=null;
        inQueue = null;
        messages.clear();
        loadMoreData();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onClose() {
        search=null;
        sortby=null;
        cursor=null;
        inQueue = null;
        messages.clear();
        loadMoreData();
        return false;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_all:
                sortby="alltimeviews";
                break;
            case R.id.item_ranking:
                sortby="ranking";
                break;
            case R.id.item_likes:
                sortby="likes";
                break;
            case R.id.item_replies:
                sortby="replies";
                break;
            case R.id.item_last7days:
                sortby="last7daysviews";
                break;
        }
        search=null;
        cursor=null;
        inQueue = null;
        messages.clear();
        loadMoreData();
        return false;
    }
}
