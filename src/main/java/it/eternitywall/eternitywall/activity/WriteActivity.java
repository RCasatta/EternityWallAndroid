package it.eternitywall.eternitywall.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Optional;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionBroadcast;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.Http;
import it.eternitywall.eternitywall.Preferences;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.wallet.EWWalletService;
import it.eternitywall.eternitywall.wallet.WalletObservable;


public class WriteActivity extends ActionBarActivity {

    private static final Integer MAX_LENGTH = 72;

    private static final String TAG = "WriteActivity";
    private static final int REQ_CODE = 100;
    private static String ANONYMOUS = "Anonymous";


    private EditText txtMessage;
    private TextView txtCounter;
    private ProgressBar progress;
    private Spinner spnrSender;
    private LinearLayout lytSender;

    private String curmsg = "";
    private Button btnSend;

    private String address=null;
    private String replyFrom=null;
    private String aliasName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        txtMessage = (EditText) findViewById(R.id.txtMessage);
        if(getIntent().getExtras() != null && getIntent().getStringExtra("sharedText") != null)
            txtMessage.setText(getIntent().getStringExtra("sharedText"));

        if(getIntent().getExtras() != null && getIntent().getStringExtra("replyFrom") != null) {
            txtMessage.setHint("reply from message id " + getIntent().getStringExtra("replyFrom"));
            replyFrom=getIntent().getStringExtra("replyFrom");
        }

        spnrSender = (Spinner) findViewById(R.id.spnrSender);
        lytSender = (LinearLayout) findViewById(R.id.lytSender);

        final WalletObservable walletObservable = ((EWApplication) getApplication()).getWalletObservable();
        if(walletObservable!=null) {
            aliasName = walletObservable.getAliasName();
            if(aliasName!=null) {
                List<String> list = new ArrayList<>();
                list.add(aliasName);
                list.add(ANONYMOUS);
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, list);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnrSender.setAdapter(dataAdapter);
                lytSender.setVisibility(View.VISIBLE);
            }
        }

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
                if(calcRemainingBytes() < 0) {
                    curmsg = oldText;
                    txtMessage.setText(curmsg);
                    txtMessage.setSelection(curmsg.length());
                }
                txtCounter.setText(calcRemainingBytes()+" "+"characters available");
            }
        });
        txtCounter = (TextView) findViewById(R.id.txtCounter);

        progress = (ProgressBar) findViewById(R.id.progress);

        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (curmsg.isEmpty()) {
                    Toast.makeText(WriteActivity.this, getString(R.string.err_empty_message), Toast.LENGTH_SHORT).show();
                    return;
                }

                if(aliasName==null) {
                    sendAnonymous();
                } else {
                    if (ANONYMOUS.equals(spnrSender.getSelectedItem().toString())) {
                        sendAnonymous();
                    } else {
                        sendFromWallet();
                    }
                }


            }


        });

        /**
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

         http://eternitywall.it/m/[hash]?format=json
         returns a json with Eternity Wall message M with tx hash equal to [hash].
         If M is an answer also the answer is returned
         If M has replies, a list of replies is returned
         [hash] - the hash of the tx that contains the OP_RETURN with the message


         *
         */
    }

    private Transaction curTx;
    private void sendFromWallet() {
        Log.i(TAG,"sendFromWallet " + curmsg);
        if(curmsg!=null && !curmsg.isEmpty()) {
            final EWWalletService ewWalletService = ((EWApplication) getApplication()).getEwWalletService();
            curTx= ewWalletService.createMessageTx(curmsg);

            final TransactionBroadcast transactionBroadcast = ewWalletService.broadcastTransaction(curTx);

            try {
                Transaction transaction = transactionBroadcast.future().get();
                if (transaction == null) {
                    Toast.makeText(this, "Error broadcasting transaction!", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(this, "Message sent! You'll be notified when written", Toast.LENGTH_LONG).show();

                    final String hash = transaction.getHashAsString();
                    final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                    Set<String> stringSet = new HashSet<>( sharedPref.getStringSet(Preferences.TO_NOTIFY, new HashSet<String>()) );
                    final SharedPreferences.Editor edit = sharedPref.edit();
                    stringSet.add(hash);
                    edit.putStringSet(Preferences.TO_NOTIFY,stringSet);
                    edit.commit();

                    Handler handler = new Handler();
                    Runnable r = new Runnable() {
                        public void run() {
                            Optional<String> stringOptional = Http.get("http://eternitywall.it/v1/countmessagesinqueue");
                            Log.i(TAG,"count messages in queue returns " + stringOptional.isPresent() );
                        }
                    };
                    handler.postDelayed(r, 3000);
                }

            } catch (ExecutionException | InterruptedException e ) {
                Toast.makeText(this, "Error broadcasting message! Please retry", Toast.LENGTH_LONG).show();
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        }

    }

    private void sendAnonymous() {
        Log.i(TAG,"sendAnonymous " + curmsg);

        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("bitcoin:17uPJEkDU3WtQp83oDuiQbnMnneA3Yfksc"));
        if (!isAvailable(i)) {
            Log.i(TAG,"bitcoin app not available");
            AlertDialog d = new AlertDialog.Builder(WriteActivity.this)
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
                    final String uriString = "bitcoin:" + address + "?amount=" + value/*+"&message=Payment&label=Satoshi&extra=other-param"*/;
                    Log.i(TAG, "uriString=(" + uriString + ")");
                    final Uri uri = Uri.parse(uriString);
                    Intent i = new Intent(Intent.ACTION_VIEW, uri);
                    startActivityForResult(Intent.createChooser(i, getString(R.string.ask_choose_wallet)), REQ_CODE);
                } else {
                    Toast.makeText(WriteActivity.this, getString(R.string.err_check_internet), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected Object doInBackground(Object[] params) {
                Optional<String> json = null;
                try {
                    String reply="";
                    if (replyFrom!=null)
                        reply="&replyid="+replyFrom;
                    json = Http.get("http://eternitywall.it/bitcoinform?format=json&text=" + URLEncoder.encode(curmsg, "UTF-8") + "&source=" + getApplicationContext().getPackageName() + reply);
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

    private Integer calcRemainingBytes() {
        try {
            int length = curmsg.getBytes("UTF-8").length;
            return MAX_LENGTH-length;
        }
        catch (Exception ex) {
            return MAX_LENGTH;
        }
    }

    private boolean isAvailable(Intent intent) {
        final PackageManager mgr = getPackageManager();
        List<ResolveInfo> list =
                mgr.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}
