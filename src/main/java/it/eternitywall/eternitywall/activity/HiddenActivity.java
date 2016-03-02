package it.eternitywall.eternitywall.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Optional;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionBroadcast;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.Http;
import it.eternitywall.eternitywall.Message;
import it.eternitywall.eternitywall.Preferences;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.adapters.MessageRecyclerViewAdapter;
import it.eternitywall.eternitywall.bitcoin.Bitcoin;
import it.eternitywall.eternitywall.dialogfragments.PinAlertDialogFragment;
import it.eternitywall.eternitywall.wallet.EWDerivation;
import it.eternitywall.eternitywall.wallet.EWWalletService;
import it.eternitywall.eternitywall.wallet.WalletObservable;

public class HiddenActivity extends AppCompatActivity {

    private String TAG= getClass().toString();
    private static final Integer MAX_LENGTH = 72;
    private static final int REQ_CODE = 100;

    TextView txtCounter,txtHeader,txtBtc,txtHash,txtDate;
    LinearLayout llDate,llBtc;
    EditText txtMessage;

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
        setContentView(R.layout.activity_hidden);

        Iconify.with(new FontAwesomeModule());

        txtMessage = (EditText) findViewById(R.id.txtMessage);
        txtCounter = (TextView) findViewById(R.id.txtCounter);
        txtHeader = (TextView) findViewById(R.id.txtHeader);

        txtHash = (TextView) findViewById(R.id.txtHash);
        txtBtc = (TextView) findViewById(R.id.txtBtc);
        txtDate = (TextView) findViewById(R.id.txtDate);

        llDate = (LinearLayout) findViewById(R.id.llDate);
        llDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance(TimeZone.getDefault());
                DatePickerDialog datePicker = new DatePickerDialog(HiddenActivity.this,
                        R.style.AppTheme,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String year1 = String.valueOf(year);
                                String month1 = String.valueOf(monthOfYear + 1);
                                String day1 = String.valueOf(dayOfMonth);
                                txtDate.setText(day1 + "/" + month1 + "/" + year1);
                            }
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH));
                datePicker.setCancelable(false);
                datePicker.setTitle("Hidden until");
                datePicker.show();

            }

        });

        llBtc = (LinearLayout) findViewById(R.id.llBtc);
        llBtc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(HiddenActivity.this);
                LayoutInflater inflater = HiddenActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dlg_pricereveal, null);
                dialogBuilder.setView(dialogView);

                final EditText editText = (EditText) dialogView.findViewById(R.id.txtPrice);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);

                dialogBuilder.setTitle("Price to Reveal");
                dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        txtBtc.setText(editText.getText().toString());
                    }
                });
                dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialogBuilder.show();

            }
        });

        spnrSender = (Spinner) findViewById(R.id.spnrSender);
        lytSender = (LinearLayout) findViewById(R.id.lytSender);

        final WalletObservable walletObservable = ((EWApplication) getApplication()).getWalletObservable();
        if(walletObservable!=null) {
            aliasName = walletObservable.getAliasName();
            List<String> list = new ArrayList<>();
            if (aliasName != null) {
                list.add(aliasName);
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnrSender.setAdapter(dataAdapter);
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

                    if(calcRemainingBytes(oldText) < 0) {
                        txtMessage.setText(curmsg);
                        txtMessage.setSelection(curmsg.length());
                        txtCounter.setText(Math.abs(calcRemainingBytes()) +" "+"characters exceeds");
                    } else {
                        curmsg = oldText;
                        txtMessage.setText(curmsg);
                        txtMessage.setSelection(curmsg.length());
                        txtCounter.setText(Math.abs(calcRemainingBytes()) +" "+"characters exceeds");
                    }
                }
                txtCounter.setText(calcRemainingBytes()+" "+"characters available");

                try {
                    MessageDigest digest = null;
                    digest = MessageDigest.getInstance("SHA-256");
                    digest.update(txtMessage.getText().toString().getBytes());
                    String hash = Hex.toHexString(digest.digest());
                    txtHash.setText(hash.toString());

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        });


        progress = (ProgressBar) findViewById(R.id.progress);

        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (curmsg.isEmpty()) {
                    Toast.makeText(HiddenActivity.this, getString(R.string.err_empty_message), Toast.LENGTH_SHORT).show();
                    return;
                } else if (calcRemainingBytes() < 0) {
                    Toast.makeText(HiddenActivity.this, "Message too long", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ask for pin
                final PinAlertDialogFragment pinAlertDialogFragment = PinAlertDialogFragment.newInstance(R.string.confirm_pin);
                pinAlertDialogFragment.setPositive(new Runnable() {
                    @Override
                    public void run() {
                        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                        try {
                            Date date = format.parse(txtDate.getText().toString());
                            long timestamp=date.getTime();
                            int btc=Integer.parseInt(txtBtc.getText().toString());

                            sendMessage(curmsg,btc,timestamp);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                pinAlertDialogFragment.show(getSupportFragmentManager(), PinAlertDialogFragment.class.toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_hidden, menu);
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


    private Integer calcRemainingBytes() {
        try {
            int length = curmsg.getBytes("UTF-8").length;
            return MAX_LENGTH-length;
        }
        catch (Exception ex) {
            return MAX_LENGTH;
        }
    }
    private Integer calcRemainingBytes(String string) {
        try {
            int length = string.getBytes("UTF-8").length;
            return MAX_LENGTH-length;
        }
        catch (Exception ex) {
            return MAX_LENGTH;
        }
    }



    public void sendMessage(final String message, final int satoshi, final long timestamp) {

        AsyncTask t = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progress.setVisibility(View.VISIBLE);

            }

            @Override
            protected void onPostExecute(Object o) {
                createTx(message);
                if (curTx!=null) {
                    registerMessage(message, satoshi, timestamp);
                    sendBroadcast();
                }

                if (HiddenActivity.this.isFinishing())  //exception will null pointer happened here, checking getActivity is null or use isAdded()????
                    return;
                super.onPostExecute(o);
                progress.setVisibility(View.INVISIBLE);

            }

            @Override
            protected Object doInBackground(Object[] params) {



                return null;
            }
        };
        t.execute();
    }





    public void registerMessage(String message, int satoshi, long timestamp){
        String passphrase = "kiss clap snap wear alter desk rally dance donate lava adult notice";
        byte[] mySeed = Bitcoin.getEntropyFromPassphrase(passphrase);
        EWDerivation ewDerivation = new EWDerivation(mySeed);

        final DeterministicKey alias = ewDerivation.getAlias();
        final String aliasString = Bitcoin.keyToStringAddress(alias);
        final String challenge = System.currentTimeMillis() + "";
        final String signature=alias.signMessage(challenge);

        final String url = "https://eternitywall.appspot.com/v1/auth/commitandreveal";
        final Map map = new HashMap<>();
        map.put("account",aliasString);
        map.put("signature", signature);
        map.put("challenge", challenge);

    /*
    final String message= req.getParameter("message");
    final String hash= req.getParameter("hash");
    final String satoshiToRevealString = req.getParameter("satoshiToReveal");
    final String timestampWhenRevealString = req.getParameter("timestampWhenReveal");
    final String pubKeyString = req.getParameter("pubKey");
    final String chainCodeString = req.getParameter("chainCode");
    final String indexString = req.getParameter("index");
     */

        final int index=0;
        final DeterministicKey external = ewDerivation.getAccount(index);//.getExternal(index);
        final String pubKey    = Hex.toHexString(external.getPubKey());
        final String chainCode = Hex.toHexString(external.getChainCode());

        map.put("message",message);
        map.put("txHash",curTx.getHashAsString());
        map.put("satoshiToReveal",1000 * satoshi);  //1mBTC
        map.put("timestampWhenReveal",timestamp);
        map.put("pubKey",pubKey);
        map.put("chainCode",chainCode);
        map.put("index", index);

        final Optional<String> stringOptional = Http.postForm(url, map);

        //assertTrue(stringOptional.isPresent());
/*
        Log.d(TAG, stringOptional.get());

        final DeterministicKey deterministicKey = HDKeyDerivation.deriveChildKey(external, new ChildNumber(0, true));

        Log.d(TAG, Bitcoin.keyToStringAddress(deterministicKey));

        final DeterministicKey deterministicKey2 = HDKeyDerivation.deriveChildKey(external, new ChildNumber(0, false));

        Log.d(TAG, Bitcoin.keyToStringAddress(deterministicKey2));
        */

    }



    private Transaction curTx;

    private void createTx(String message){

        MessageDigest digest = null;
        String messageHash=null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(message.getBytes());
            messageHash = Hex.toHexString(digest.digest());
            Log.d(TAG, "hash : " + messageHash);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.d(TAG, "NoSuchAlgorithmException");
        }

        if(messageHash!=null && !messageHash.isEmpty()) {
            final EWWalletService ewWalletService = ((EWApplication) getApplication()).getEwWalletService();
            try {
                curTx = ewWalletService.createMessageTx(curmsg, null);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Toast.makeText(this, "Wait the pending message", Toast.LENGTH_LONG).show();
                return;
            } catch (AddressFormatException e) {
                Toast.makeText(this, "Not more 100 messages available", Toast.LENGTH_LONG).show();
                return;
            } catch (InsufficientMoneyException e) {
                e.printStackTrace();
                Toast.makeText(this, "Insufficient coin", Toast.LENGTH_LONG).show();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Exception", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }


    private void sendBroadcast() {
        final EWWalletService ewWalletService = ((EWApplication) getApplication()).getEwWalletService();
        final TransactionBroadcast transactionBroadcast = ewWalletService.broadcastTransaction(curTx);

        try {
            Transaction transaction = transactionBroadcast.future().get();
            if (transaction == null) {
                Toast.makeText(this, "Error broadcasting transaction!", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this, "Message sent! You'll be notified when written", Toast.LENGTH_LONG).show();

                final String hash = transaction.getHashAsString();
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                Set<String> stringSet = new HashSet<>(sharedPref.getStringSet(Preferences.TO_NOTIFY, new HashSet<String>()));
                final SharedPreferences.Editor edit = sharedPref.edit();
                stringSet.add(hash);
                edit.putStringSet(Preferences.TO_NOTIFY, stringSet);
                edit.commit();

                Handler handler = new Handler();
                Runnable r = new Runnable() {
                    public void run() {
                        Optional<String> stringOptional = Http.get("http://eternitywall.it/v1/countmessagesinqueue");
                        Log.i(TAG, "count messages in queue returns " + stringOptional.isPresent());
                    }
                };
                handler.postDelayed(r, 3000);
            }

        } catch (ExecutionException | InterruptedException e) {
            Toast.makeText(this, "Error broadcasting message! Please retry", Toast.LENGTH_LONG).show();
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }




}
