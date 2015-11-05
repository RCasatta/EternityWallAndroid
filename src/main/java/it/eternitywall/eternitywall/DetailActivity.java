package it.eternitywall.eternitywall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import it.eternitywall.eternitywall.adapters.MessageListAdapter;


public class DetailActivity extends ActionBarActivity  {

    private static final Integer MAX_LENGTH = 72;

    private static final String TAG = "WriteActivity";
    private static final int REQ_CODE = 100;

    private TextView txtMessage;
    private TextView txtDate;
    private ProgressBar progress;
    private String hash=null;
    private ListView repliesMessages;
    private ListView answersMessages;

    private List<Message> replies;
    private List<Message> answers;


    Button btnShare,btnLikes,btnProof,btnRanking,btnReplies,btnTranslate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        btnShare = (Button)findViewById(R.id.btnShare);
        btnLikes = (Button)findViewById(R.id.btnLikes);
        btnProof = (Button)findViewById(R.id.btnProof);
        btnRanking = (Button)findViewById(R.id.btnRanking);
        btnReplies = (Button)findViewById(R.id.btnReply);
        btnTranslate = (Button)findViewById(R.id.btnTranslate);
        btnShare.setTypeface(font);
        btnLikes.setTypeface(font);
        btnProof.setTypeface(font);
        btnRanking.setTypeface(font);
        btnReplies.setTypeface(font);
        btnTranslate.setTypeface(font);

        txtMessage = (TextView)findViewById(R.id.txtMessage);
        txtDate = (TextView)findViewById(R.id.txtDate);
        progress = (ProgressBar) findViewById(R.id.progress);
        repliesMessages=(ListView) findViewById(R.id.repliesMessages);
        answersMessages=(ListView) findViewById(R.id.answersMessages);

        replies = new ArrayList<Message>();
        answers = new ArrayList<Message>();

        try {
            hash = getIntent().getStringExtra("hash");
        }catch (Exception e){
            //succhia!
            Toast.makeText(DetailActivity.this, getString(R.string.err_check_internet), Toast.LENGTH_SHORT).show();
        }
        loadMessage();
/*
        txtMessage = (EditText) findViewById(R.id.txtMessage);
        if (getIntent().getExtras() != null && getIntent().getStringExtra("sharedText") != null)
            txtMessage.setText(getIntent().getStringExtra("sharedText"));

        txtMessage.addTextChangedListener(new TextWatcher() {

            String oldText;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                oldText = curmsg;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                curmsg = s.toString();
                if (calcRemainingBytes() < 0) {
                    curmsg = oldText;
                    txtMessage.setText(curmsg);
                    txtMessage.setSelection(curmsg.length());
                }
                txtCounter.setText(calcRemainingBytes() + " " + "characters available");
            }
        });
        txtCounter = (TextView) findViewById(R.id.txtCounter);

        progress = (ProgressBar) findViewById(R.id.progress);

        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (curmsg.isEmpty()) {
                    Toast.makeText(DetailActivity.this, getString(R.string.err_empty_message), Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("bitcoin:17uPJEkDU3WtQp83oDuiQbnMnneA3Yfksc"));
                if (!isAvailable(i)) {
                    AlertDialog d = new AlertDialog.Builder(DetailActivity.this)
                            .setTitle(getString(R.string.app_name))
                            .setMessage("There are no bitcoin wallet app installed, do you want to install GreenBits?")
                            .setNegativeButton("No, thanks", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton("Yes" + "!", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.greenaddress.greenbits_android_wallet")));
                                }
                            })
                            .create();
                    d.show();
                    return;
                }

                AsyncTask t = new AsyncTask() {

                    private String value;
                    private boolean ok = false;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();

                        btnSend.setVisibility(View.INVISIBLE);
                        progress.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        progress.setVisibility(View.INVISIBLE);
                        btnSend.setVisibility(View.VISIBLE);

                        if (ok) {
                            final String uriString = "bitcoin:" + address + "?amount=" + value/*+"&message=Payment&label=Satoshi&extra=other-param";
                            Log.i(TAG, "uriString=(" + uriString + ")");
                            final Uri uri = Uri.parse(uriString);
                            Intent i = new Intent(Intent.ACTION_VIEW, uri);
                            startActivityForResult(Intent.createChooser(i, getString(R.string.ask_choose_wallet)), REQ_CODE);
                        } else {
                            Toast.makeText(DetailActivity.this, getString(R.string.err_check_internet), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    protected Object doInBackground(Object[] params) {
                        Optional<String> json = null;
                        try {
                            json = Http.get("http://eternitywall.it/bitcoinform?format=json&text=" + URLEncoder.encode(curmsg, "UTF-8") + "&source=" + getApplicationContext().getPackageName());
                            if (json.isPresent()) {
                                String jstring = json.get();
                                JSONObject jo = new JSONObject(jstring);

                                address = jo.getString("address");
                                value = jo.getString("btc");

                                Log.i(TAG, "" + value);

                                ok = true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                };
                t.execute();
            }
        });
*/
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

    public void loadMessage() {
        AsyncTask t = new AsyncTask() {

            private boolean ok = false;
            private Message mMessage = null;
            private List<Message> mReplies = null;
            private List<Message> mAnswers = null;


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progress.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if (DetailActivity.this.isFinishing())
                    return;
                progress.setVisibility(View.INVISIBLE);

                if(ok) {
                    txtDate.setText(new SimpleDateFormat("dd MMM yyyy HH.mm").format(new Date(mMessage.getTimestamp())));
                    txtMessage.setText(mMessage.getMessage());
                    // TO DO
                    //if (mMessage.getAnswer()==true)
                    if (mMessage.getLikes()>0)
                        btnLikes.setText( getResources().getString(R.string.icon_likes) + " ("+String.valueOf(mMessage.getLikes())+")" );
                    if (mMessage.getReplies()>0)
                        btnReplies.setText( getResources().getString(R.string.icon_commenting) + " ("+String.valueOf(mMessage.getReplies())+")" );

                    btnShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT, "http://eternitywall.it/m/"+mMessage.getTxHash());
                            intent.setType("text/plain");
                            startActivity(intent);
                        }
                    });
                    btnRanking.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            /*Message ranking
                            0.123 middle

                            This page has been viewed 22 times of which 22 in the last seven days. It has 1 like.
                           */
                            new AlertDialog.Builder(DetailActivity.this)
                                    .setTitle("Message ranking")
                                    .setMessage("")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // continue with delete
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // do nothing
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    });

                    if(replies != null && !replies.isEmpty()) {
                        replies.addAll(mReplies);
                        final MessageListAdapter messageListAdapter = (MessageListAdapter) repliesMessages.getAdapter();
                        messageListAdapter.notifyDataSetChanged();
                    }
                    else {
                        replies.addAll(mReplies);
                        repliesMessages.setAdapter(new MessageListAdapter(DetailActivity.this, R.layout.item_message, mReplies, 0, null));
                    }
                    if(answers != null && !answers.isEmpty()) {
                        answers.addAll(mAnswers);
                        final MessageListAdapter messageListAdapter = (MessageListAdapter) answersMessages.getAdapter();
                        messageListAdapter.notifyDataSetChanged();
                    }
                    else {
                        answers.addAll(mAnswers);
                        answersMessages.setAdapter(new MessageListAdapter(DetailActivity.this, R.layout.item_message, mAnswers, 0, null));
                    }
                }
                else {
                    //succhia!
                    Toast.makeText(DetailActivity.this, getString(R.string.err_check_internet), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            protected Object doInBackground(Object[] params) {
                Optional<String> json = Http.get("http://eternitywall.it/m/"+hash+"?format=json");
                if(json.isPresent()) {
                    try {
                        String jstring = json.get();
                        JSONObject jo = new JSONObject(jstring);
                        mMessage = Message.buildFromJson(jo.getJSONObject("current"));
                        Log.i(TAG, mMessage.toString());

                        // build list of replies messages
                        try {
                            JSONArray jReplies = jo.getJSONArray("replies");
                            mReplies=new ArrayList<Message>();
                            for (int i=0;i<jReplies.length();i++){
                                Message reply = Message.buildFromJson(jReplies.getJSONObject(i));
                                mReplies.add( reply );
                            }
                        } catch (JSONException e) {
                            mReplies=new ArrayList<Message>();
                        } //optional

                        // build list of answers messages
                        try {
                            JSONObject jAnswer = jo.getJSONObject("replyFrom");
                            mAnswers=new ArrayList<Message>();
                            Message answer = Message.buildFromJson(jAnswer);
                            mAnswers.add( answer );
                        } catch (JSONException e) {
                            mAnswers=new ArrayList<Message>();
                        } //optional


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
