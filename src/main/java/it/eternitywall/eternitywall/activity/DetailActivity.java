package it.eternitywall.eternitywall.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Optional;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.eternitywall.eternitywall.Http;
import it.eternitywall.eternitywall.IdenticonGenerator;
import it.eternitywall.eternitywall.Message;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.adapters.MessageListAdapter;
import it.eternitywall.eternitywall.dialogfragments.RankingDialogFragment;

public class DetailActivity extends ActionBarActivity {

    private static final Integer MAX_LENGTH = 72;

    private static final String TAG = "WriteActivity";
    private static final int REQ_CODE = 100;

    private TextView txtMessage;
    private TextView txtDate;
    private TextView txtStatus;
    private ProgressBar progress;
    private String hash = null;
    private ListView repliesMessages;
    private ListView answersMessages;

    private List<Message> replies;
    private List<Message> answers;


    Button btnShare, btnLikes, btnProof, btnRanking, btnReplies, btnTranslate;
    protected ImageView identicon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        View viewDetailMessage = findViewById(R.id.detailmessage);
        btnShare = (Button) viewDetailMessage.findViewById(R.id.btnShare);
        btnLikes = (Button) viewDetailMessage.findViewById(R.id.btnLikes);
        btnProof = (Button) viewDetailMessage.findViewById(R.id.btnProof);
        btnRanking = (Button) viewDetailMessage.findViewById(R.id.btnRanking);
        btnReplies = (Button) viewDetailMessage.findViewById(R.id.btnReply);
        btnTranslate = (Button) viewDetailMessage.findViewById(R.id.btnTranslate);
        btnShare.setTypeface(font);
        btnLikes.setTypeface(font);
        btnProof.setTypeface(font);
        btnRanking.setTypeface(font);
        btnReplies.setTypeface(font);
        btnTranslate.setTypeface(font);
        txtMessage = (TextView) viewDetailMessage.findViewById(R.id.txtMessage);
        txtDate = (TextView) viewDetailMessage.findViewById(R.id.txtDate);
        txtStatus = (TextView) viewDetailMessage.findViewById(R.id.txtStatus);
        identicon = (ImageView) viewDetailMessage.findViewById(R.id.identicon);

        progress = (ProgressBar) findViewById(R.id.progress);
        repliesMessages = (ListView) findViewById(R.id.repliesMessages);
        answersMessages = (ListView) findViewById(R.id.answersMessages);

        replies = new ArrayList<Message>();
        answers = new ArrayList<Message>();

        try {
            hash = getIntent().getStringExtra("hash");
        } catch (Exception e) {
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
        Bitmap mBitmap = null;
        com.google.zxing.Writer writer = new QRCodeWriter();
        String finaldata = Uri.encode(data, "ISO-8859-1");
        try {
            BitMatrix bm = writer.encode(finaldata, BarcodeFormat.QR_CODE, 200, 200);
            mBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
            for (int i = 0; i < 200; i++) {
                for (int j = 0; j < 200; j++) {
                    mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
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

                if (ok) {
                    String dateFormatted = new SimpleDateFormat("dd MMM yyyy HH.mm").format(new Date(mMessage.getTimestamp()));
                    if(mMessage.getAliasName()!=null) {
                        txtDate.setText(mMessage.getAliasName() + " - " + dateFormatted);
                    } else {
                        txtDate.setText(dateFormatted);
                    }
                    txtMessage.setText(mMessage.getMessage());
                    // TO DO
                    //if (mMessage.getAnswer()==true)
                    if (mMessage.getLikes() > 0)
                        btnLikes.setText(getResources().getString(R.string.icon_likes) + " (" + String.valueOf(mMessage.getLikes()) + ")");
                    if (mMessage.getReplies() > 0)
                        btnReplies.setText(getResources().getString(R.string.icon_commenting) + " (" + String.valueOf(mMessage.getReplies()) + ")");

                    if(mMessage.getAlias()!=null) {
                        Bitmap bitmap= IdenticonGenerator.generate(mMessage.getAlias());
                        identicon.setImageBitmap(bitmap);
                        identicon.setVisibility(View.VISIBLE);
                    } else {
                        identicon.setVisibility(View.GONE);

                    }
                    btnShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT, "http://eternitywall.it/m/" + mMessage.getTxHash());
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

                            String[] sites = new String[]{"Blockchain.info", "Blocktrail", "chainFlyer", "Smartbit", "SoChain"};
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
                                        case 4:
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://chain.so/tx/BTC/" + mMessage.getTxHash())));
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

                            RankingDialogFragment rankingDialogFragment = new RankingDialogFragment();
                            rankingDialogFragment.setMessage(mMessage);
                            rankingDialogFragment.show(getSupportFragmentManager(),RankingDialogFragment.class.toString());

                        }
                    });
                    btnLikes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            /*AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DetailActivity.this);
                            LayoutInflater inflater = DetailActivity.this.getLayoutInflater();
                            View dialogView = inflater.inflate(R.layout.dialog_likes, null);
                            dialogBuilder.setView(dialogView);
                            dialogBuilder.setTitle("Like");

                            // set the custom dialog components - text, image and button
                            ImageView imgQr = (ImageView) dialogView.findViewById(R.id.imgQr);
                            TextView txtQr = (TextView) dialogView.findViewById(R.id.txtQr);
                            txtQr.setText(mMessage.getMessageId());

                            Bitmap bitmap = generateQRCode(mMessage.getMessageId());
                            if (bitmap != null)
                                imgQr.setImageBitmap(bitmap);

                            dialogBuilder.setCancelable(true);
                            dialogBuilder.show();*/
                            address=mMessage.getMessageId();
                            SendLike();
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

                    if (replies != null && !replies.isEmpty()) {
                        replies.addAll(mReplies);
                        final MessageListAdapter messageListAdapter = (MessageListAdapter) repliesMessages.getAdapter();
                        messageListAdapter.notifyDataSetChanged();
                    } else {
                        replies.addAll(mReplies);
                        Log.i(TAG, "2 DetailActivity=" + DetailActivity.this);
                        repliesMessages.setAdapter(new MessageListAdapter(DetailActivity.this, R.layout.item_message, mReplies, 0, null));
                    }
                    if (answers != null && !answers.isEmpty()) {
                        answers.addAll(mAnswers);
                        final MessageListAdapter messageListAdapter = (MessageListAdapter) answersMessages.getAdapter();
                        messageListAdapter.notifyDataSetChanged();
                    } else {
                        answers.addAll(mAnswers);
                        Log.i(TAG, "3 DetailActivity=" + DetailActivity.this);
                        answersMessages.setAdapter(new MessageListAdapter(DetailActivity.this, R.layout.item_message, mAnswers, 0, null));
                    }
                } else {
                    //succhia!
                    Toast.makeText(DetailActivity.this, getString(R.string.err_check_internet), Toast.LENGTH_SHORT).show();

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

    void SendLike() {
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
}
