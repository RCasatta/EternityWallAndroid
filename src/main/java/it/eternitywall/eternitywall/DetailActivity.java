package it.eternitywall.eternitywall;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

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

        return super.onOptionsItemSelected(item);
    }


    private Bitmap generateQRCode(String data) {
        Bitmap mBitmap=null;
        com.google.zxing.Writer writer = new QRCodeWriter();
        String finaldata =Uri.encode(data, "ISO-8859-1");
        try {
            BitMatrix bm = writer.encode(finaldata,BarcodeFormat.QR_CODE, 200, 200);
            mBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
            for (int i = 0; i < 200; i++) {
                for (int j = 0; j < 200; j++) {
                    mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK: Color.WHITE);
                }
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return mBitmap;
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
                    btnProof.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Proof website:
                            // examples:
                            //https://blockchain.info/tx/2db207f008822f90c6e67a21179a2da44b043ebef3d3854f26efb9ffde6aeef8
                            //https://www.blocktrail.com/BTC/tx/2db207f008822f90c6e67a21179a2da44b043ebef3d3854f26efb9ffde6aeef8
                            //http://chainflyer.bitflyer.jp/Transaction/2db207f008822f90c6e67a21179a2da44b043ebef3d3854f26efb9ffde6aeef8
                            //https://www.smartbit.com.au/tx/2db207f008822f90c6e67a21179a2da44b043ebef3d3854f26efb9ffde6aeef8

                            String []sites=new String[] { "Blockchain.info","Blocktrail","chainFlyer","Smartbit"};
                            AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
                            builder.setItems(sites, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://blockchain.info/tx/" + mMessage.getTxHash())));
                                            break;
                                        case 1:
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.blocktrail.com/BTC/tx/" + mMessage.getTxHash())));
                                            break;
                                        case 2:
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://chainflyer.bitflyer.jp/Transaction/" + mMessage.getTxHash())));
                                            break;
                                        case 3:
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.smartbit.com.au/tx/" + mMessage.getTxHash())));
                                            break;
                                    }

                                }
                            });
                            builder.setTitle("Proof");
                            //builder.setIcon(android.R.drawable.ic_dialog_alert);
                            builder.show();

                        }
                    });
                    btnRanking.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DetailActivity.this);
                            LayoutInflater inflater = DetailActivity.this.getLayoutInflater();
                            View dialogView = inflater.inflate(R.layout.dialog_ranking, null);
                            dialogBuilder.setView(dialogView);
                            dialogBuilder.setTitle("Message ranking");

                            // set the custom dialog components - text, image and button
                            TextView txtRank = (TextView) dialogView.findViewById(R.id.txtRank);
                            TextView txtValue = (TextView) dialogView.findViewById(R.id.txtValue);
                            TextView txtText = (TextView) dialogView.findViewById(R.id.txtText);
                            txtText.setText("This message has been viewed " + mMessage.getView() + " times of which " + mMessage.getWeekView() + " in the last seven days.");

                            long integer= Math.round(mMessage.getValue() * 1000);
                            Double doubled = Double.valueOf(integer)/1000;

                            txtValue.setText(doubled.toString());
                            if (mMessage.getRank()==1)
                                txtRank.setText("top");
                            else if (mMessage.getRank()==2)
                                txtRank.setText("middle");
                            else if (mMessage.getRank()==3)
                                txtRank.setText("low");
                            dialogBuilder.setCancelable(true);
                            dialogBuilder.show();

                        }
                    });
                    btnLikes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DetailActivity.this);
                            LayoutInflater inflater = DetailActivity.this.getLayoutInflater();
                            View dialogView = inflater.inflate(R.layout.dialog_likes, null);
                            dialogBuilder.setView(dialogView);
                            dialogBuilder.setTitle("Like");

                            // set the custom dialog components - text, image and button
                            ImageView imgQr = (ImageView) dialogView.findViewById(R.id.imgQr);
                            TextView txtQr = (TextView) dialogView.findViewById(R.id.txtQr);
                            txtQr.setText(mMessage.getMessageId());

                            Bitmap bitmap=generateQRCode(mMessage.getMessageId());
                            if(bitmap!=null)
                                imgQr.setImageBitmap(bitmap);

                            dialogBuilder.setCancelable(true);
                            dialogBuilder.show();
                        }
                    });
                    btnReplies.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(DetailActivity.this, WriteActivity.class);
                            i.putExtra("replyFrom", mMessage.getMessageId());
                            startActivity(i);
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
