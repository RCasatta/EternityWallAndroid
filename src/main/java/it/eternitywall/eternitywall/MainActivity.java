package it.eternitywall.eternitywall;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.eternitywall.eternitywall.adapters.MessageListAdapter;


public class MainActivity extends ActionBarActivity implements MessageListAdapter.MessageListAdapterManager {

    private static final int REQUEST_CODE = 8274;
    private static final String TAG = "MainActivity";

    private ListView lstMessages;
    private ProgressBar progress;
    private SwipeRefreshLayout swipe;

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
        inQueue = null;
        loadMoreData();

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                messages = new ArrayList<Message>();
                cursor = null;
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
                Optional<String> json = cursor == null ? Http.get("http://eternitywall.it/?format=json") : Http.get("http://eternitywall.it/?format=json&cursor="+cursor);
                if(json.isPresent()) {
                    try {
                        String jstring = json.get();
                        JSONObject jo = new JSONObject(jstring);

                        cursor = jo.getString("next");
                        if(jo.has("messagesInQueue")) {
                            inQueue = jo.getInt("messagesInQueue");
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
}
