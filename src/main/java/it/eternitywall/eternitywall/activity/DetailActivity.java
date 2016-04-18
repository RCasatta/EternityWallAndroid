package it.eternitywall.eternitywall.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Optional;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.Http;
import it.eternitywall.eternitywall.IdenticonGenerator;
import it.eternitywall.eternitywall.Message;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.adapters.MessageRecyclerViewAdapter;
import it.eternitywall.eternitywall.components.MessageView;
import it.eternitywall.eternitywall.dialogfragments.RankingDialogFragment;

public class DetailActivity extends ActionBarActivity {

    private static final Integer MAX_LENGTH = 72;

    private static final String TAG = "WriteActivity";
    private static final int REQ_CODE = 100;

    private ProgressBar progress;
    private String hash = null;
    private RecyclerView recyclerView;
    //private RecyclerView  singleRecyclerView;

    private List<Message> replies= new ArrayList<Message>();
    private List<Message> answers=new ArrayList<Message>();

    private MessageView answerMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Iconify.with(new FontAwesomeModule());

        progress = (ProgressBar) findViewById(R.id.progress);

        //clear variables
        replies = new ArrayList<Message>();
        answers = new ArrayList<Message>();

        // message view component for answer message
        answerMessageView = (MessageView)findViewById(R.id.answerMessageView);

        // recyclerview on replies Messages
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        final it.eternitywall.eternitywall.components.LinearLayoutManager layoutManager = new it.eternitywall.eternitywall.components.LinearLayoutManager(this, it.eternitywall.eternitywall.components.LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        final LruCache<String, Bitmap> bitmapCache = ((EWApplication) getApplication()).getBitmapCache();
        recyclerView.setAdapter(new MessageRecyclerViewAdapter(replies, 0, null,bitmapCache));

        try {
            hash = getIntent().getStringExtra("hash");
        } catch (Exception e) {
            //succhia!
            Toast.makeText(DetailActivity.this, getString(R.string.err_check_internet), Toast.LENGTH_SHORT).show();
        }

        //load messages at startup
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

                // current message
                parseCurrentMessage(findViewById(R.id.detailmessage),DetailActivity.this,mMessage);

                // answer component single message
                if (mAnswers != null && !mAnswers.isEmpty()) {
                    answerMessageView.setVisibility(View.VISIBLE);
                    answerMessageView.set(mAnswers.get(0));
                    answerMessageView.setTextMessage(android.R.style.TextAppearance_Small);
                } else {
                    answerMessageView.setVisibility(View.GONE);
                }

                // list of replies messages
                if (replies != null && !replies.isEmpty()) {
                    replies.addAll(mReplies);
                    final MessageRecyclerViewAdapter messageListAdapter = (MessageRecyclerViewAdapter) recyclerView.getAdapter();
                    messageListAdapter.notifyDataSetChanged();
                } else {
                    replies.addAll(mReplies);
                    final LruCache<String, Bitmap> bitmapCache = ((EWApplication) getApplication()).getBitmapCache();
                    recyclerView.setAdapter(new MessageRecyclerViewAdapter(mReplies, 0, null,bitmapCache));
                }
            }

            @Override
            protected Object doInBackground(Object[] params) {
                Optional<String> json = Http.get("http://eternitywall.it/m/" + hash + "?format=json");
                if (json.isPresent()) {
                    try {
                        String jstring = json.get();
                        JSONObject jo = new JSONObject(jstring);
                        mMessage = Message.buildFromJson(jo.getJSONObject("current"));
                        Log.i(TAG, mMessage.toString());

                        // build list of replies messages
                        try {
                            JSONArray jReplies = jo.getJSONArray("replies");
                            mReplies = new ArrayList<Message>();
                            for (int i = 0; i < jReplies.length(); i++) {
                                Message reply = Message.buildFromJson(jReplies.getJSONObject(i));
                                mReplies.add(reply);
                            }
                        } catch (JSONException e) {
                            mReplies = new ArrayList<Message>();
                        } //optional

                        // build list of answers messages
                        try {
                            JSONObject jAnswer = jo.getJSONObject("replyFrom");
                            mAnswers = new ArrayList<Message>();
                            Message answer = Message.buildFromJson(jAnswer);
                            mAnswers.add(answer);
                        } catch (JSONException e) {
                            mAnswers = new ArrayList<Message>();
                        } //optional


                        ok = true;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                return null;
            }
        };
        t.execute();
    }


    // Send a like
    String address;
    void SendLike(String address) {
        this.address=address;
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

        if (address!=null) {
            String value="0.0001";
            final String uriString = "bitcoin:" + address + "?amount=" + value/*+"&message=Payment&label=Satoshi&extra=other-param"*/;
            Log.i(TAG, "uriString=(" + uriString + ")");
            final Uri uri = Uri.parse(uriString);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.ask_choose_wallet)), REQ_CODE);
        } else {
            Toast.makeText(DetailActivity.this, getString(R.string.err_check_internet), Toast.LENGTH_SHORT).show();
        }

    }


    private boolean isAvailable(Intent intent) {
        final PackageManager mgr = getPackageManager();
        List<ResolveInfo> list =
                mgr.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQ_CODE && address != null  /*&& data != null /*&& resultCode != RESULT_CANCELED*/) {

            //TODO: debug RESULT_CODE on wallet application

            Log.d(TAG, "requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);

            Intent i = new Intent(this, ThxActivity.class);
            i.putExtra("address", address);
            startActivity(i);
            finish();
        }
    }


    private void parseCurrentMessage(View v, Context context, final Message message) {

        LinearLayout llShare, llLikes, llProof, llRanking, llReplies, llTranslate;
        TextView tvLikesText, tvReplyText;
        ImageView identicon;
        TextView txtMessage, txtDate;

        View viewDetailMessage = findViewById(R.id.detailmessage);
        llShare = (LinearLayout) viewDetailMessage.findViewById(R.id.llSharing);
        llLikes = (LinearLayout) viewDetailMessage.findViewById(R.id.llLikes);
        llProof = (LinearLayout) viewDetailMessage.findViewById(R.id.llProof);
        llRanking = (LinearLayout) viewDetailMessage.findViewById(R.id.llRanking);
        llReplies = (LinearLayout) viewDetailMessage.findViewById(R.id.llReply);
        llTranslate = (LinearLayout) viewDetailMessage.findViewById(R.id.llTranslate);


        tvLikesText = (TextView) viewDetailMessage.findViewById(R.id.tvLikesText);
        tvReplyText = (TextView) viewDetailMessage.findViewById(R.id.tvReplyText);

        txtMessage = (TextView) viewDetailMessage.findViewById(R.id.txtMessage);
        txtDate = (TextView) viewDetailMessage.findViewById(R.id.txtDate);
        identicon = (ImageView) viewDetailMessage.findViewById(R.id.identicon);

// date               if (ok) {
        String dateFormatted = new SimpleDateFormat("dd MMM yyyy HH.mm").format(new Date(message.getTimestamp()));
        if (message.getAliasName() != null) {
            txtDate.setText(message.getAliasName() + " - " + dateFormatted);
        } else {
            txtDate.setText(dateFormatted);
        }

        // link on message
        if (message.getLink() != null) {
            String link = message.getLink();
            String linkreplace = "";
            if (link.startsWith("@"))
                linkreplace = "<a href='http://twitter.com/" + link + "'>" + link + "</a>";
            else if (link.contains("http"))
                linkreplace = "<a href='" + link + "'>" + link + "</a>";
            else if (link.contains("https"))
                linkreplace = "<a href='" + link + "'>" + link + "</a>";
            else
                linkreplace = "<a href='http://" + link + "'>" + link + "</a>";
            String text = message.getMessage();
            text = text.replace(link, linkreplace);
            txtMessage.setText(Html.fromHtml(text));
            txtMessage.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            txtMessage.setText(message.getMessage());
        }

        // TO DO
        //if (mMessage.getAnswer()==true)
        if (message.getLikes() > 0)
            tvLikesText.setText(" (" + String.valueOf(message.getLikes()) + ")");
        if (message.getReplies() > 0)
            tvReplyText.setText(" (" + String.valueOf(message.getReplies()) + ")");

        if (message.getAlias() != null) {
            Bitmap bitmap = IdenticonGenerator.generate(message.getAlias());
            identicon.setImageBitmap(bitmap);
            identicon.setVisibility(View.VISIBLE);
            identicon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DetailActivity.this, ProfileActivity.class);
                    intent.putExtra("accountId", String.valueOf(message.getAlias()));
                    DetailActivity.this.startActivity(intent);
                }
            });
        } else {
            identicon.setVisibility(View.GONE);

        }
        llShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, "http://eternitywall.it/m/" + message.getTxHash());
                intent.setType("text/plain");
                startActivity(intent);
            }
        });
        llProof.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Proof website:
                // examples:
                //https://blockchain.info/tx/2db207f008822f90c6e67a21179a2da44b043ebef3d3854f26efb9ffde6aeef8
                //https://www.blocktrail.com/BTC/tx/2db207f008822f90c6e67a21179a2da44b043ebef3d3854f26efb9ffde6aeef8
                //http://chainflyer.bitflyer.jp/Transaction/2db207f008822f90c6e67a21179a2da44b043ebef3d3854f26efb9ffde6aeef8
                //https://www.smartbit.com.au/tx/2db207f008822f90c6e67a21179a2da44b043ebef3d3854f26efb9ffde6aeef8

                String[] sites = new String[]{"Blockchain.info", "Blocktrail", "chainFlyer", "Smartbit", "SoChain"};
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
                builder.setItems(sites, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://blockchain.info/tx/" + message.getTxHash())));
                                break;
                            case 1:
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.blocktrail.com/BTC/tx/" + message.getTxHash())));
                                break;
                            case 2:
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://chainflyer.bitflyer.jp/Transaction/" + message.getTxHash())));
                                break;
                            case 3:
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.smartbit.com.au/tx/" + message.getTxHash())));
                                break;
                            case 4:
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://chain.so/tx/BTC/" + message.getTxHash())));
                                break;
                        }

                    }
                });
                builder.setTitle("Proof");
                //builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.show();

            }
        });
        llRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RankingDialogFragment rankingDialogFragment = new RankingDialogFragment();
                rankingDialogFragment.setMessage(message);
                rankingDialogFragment.show(getSupportFragmentManager(), RankingDialogFragment.class.toString());

            }
        });
        llLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendLike(message.getMessageId());
            }
        });
        llReplies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DetailActivity.this, WriteActivity.class);
                i.putExtra("replyFrom", message.getMessageId());
                startActivity(i);
            }
        });
    }


}
