package it.eternitywall.eternitywall.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.Preferences;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.dialogfragments.EmptyWalletDialogFragment;
import it.eternitywall.eternitywall.dialogfragments.PersonalNodeDialogFragment;
import it.eternitywall.eternitywall.dialogfragments.PinAlertDialogFragment;
import it.eternitywall.eternitywall.wallet.EWWalletService;

public class PreferencesActivity extends AppCompatActivity {
    private static final String TAG = "PreferencesActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        View llResync = findViewById(R.id.llResync);
        View llAbout = findViewById(R.id.llAbout);
        View llRemove = findViewById(R.id.llRemove);
        View llPassphrase = findViewById(R.id.llPassphrase);
        View llEmpty = findViewById(R.id.llEmpty);
        View llPersonalNode = findViewById(R.id.llPersonalNode);

        Switch switchDonation = (Switch) findViewById(R.id.switchDonation);
        LinearLayout llDebug = (LinearLayout) findViewById(R.id.llDebug);

        llResync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (existAccount()) {
                    dialogPin_resyncAccount();
                } else
                    dialogCreateAccount();
            }
        });
        llAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PreferencesActivity.this, AboutActivity.class));
            }
        });
        llRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (existAccount()) {
                    dialogPin_removeAccount();
                } else
                    dialogCreateAccount();
            }
        });
        llPassphrase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (existAccount())
                    dialogPin_showPassphrase();
                else
                    dialogCreateAccount();
            }
        });



        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(PreferencesActivity.this);

        switchDonation.setChecked(sharedPref.getBoolean(Preferences.DONATION, true));
        switchDonation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(PreferencesActivity.this);
                sharedPref.edit().putBoolean(Preferences.DONATION,isChecked ).apply();
                Log.i(TAG, "isChecked=" + isChecked);
                if(isChecked) {
                    Toast.makeText(PreferencesActivity.this,"Thank you! :)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PreferencesActivity.this,":(", Toast.LENGTH_SHORT).show();

                }
            }
        });

        llDebug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (existAccount())
                    startActivity(new Intent(PreferencesActivity.this, DebugActivity.class));
                else
                    dialogCreateAccount();
            }
        });


        llEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!existAccount()) {
                    dialogCreateAccount();
                } else {
                    dialogPin_emptyWallet();
                }
            }
        });

        llPersonalNode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPersonalNode();
            }
        });

    }

    private void dialogPersonalNode() {
        PersonalNodeDialogFragment personalNodeDialogFragment= new PersonalNodeDialogFragment();
        personalNodeDialogFragment.show(getSupportFragmentManager(),PersonalNodeDialogFragment.class.toString());
    }

    private void dialogEmptyWallet() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialogEmptyWallet");
        if(prev != null)
            ft.remove(prev);
        ft.addToBackStack(null);

        EmptyWalletDialogFragment emptyWalletDialogFragment = new EmptyWalletDialogFragment();
        emptyWalletDialogFragment.show(ft,EmptyWalletDialogFragment.class.toString());
    }

    private boolean existAccount(){
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(PreferencesActivity.this);
        String passphrase = sharedPref.getString(Preferences.PASSPHRASE,null);
        if(passphrase!=null) {
            return true;
        }else
            return false;
    }


    private void dialogCreateAccount() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Warning");
        alertDialog.setMessage("Account not defined, please create a wallet.");
        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    private void dialogPin_showPassphrase(){
        final PinAlertDialogFragment pinAlertDialogFragment= PinAlertDialogFragment.newInstance(R.string.confirm_pin);
        pinAlertDialogFragment.setPositive(new Runnable() {
            @Override
            public void run() {
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(PreferencesActivity.this);
                String pin = sharedPref.getString(Preferences.PIN,null);

                if (pin!=null && pinAlertDialogFragment.getPin().equals(pin)){
                    String passphrase = sharedPref.getString(Preferences.PASSPHRASE, null);
                    if (passphrase != null) {
                        // SHOW
                        TextView txtPassphrase = (TextView) findViewById(R.id.txtPassphrase);
                        txtPassphrase.setText(passphrase);
                    }
                }else {
                    new AlertDialog.Builder(PreferencesActivity.this)
                            .setTitle("Attention")
                            .setMessage("Invalid PIN. Retry...")
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            }).show();
                }
            }
        });
        pinAlertDialogFragment.show(getSupportFragmentManager(),PinAlertDialogFragment.class.toString());

    }

    private void dialogPin_resyncAccount(){
        new AlertDialog.Builder(PreferencesActivity.this)
                .setTitle("Are you sure to resync?")
                .setMessage("Resyncing is necessary only if wallet can't see some transactions and it could take some minutes.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        EWWalletService ewWalletService = ((EWApplication) getApplication()).getEwWalletService();
                        ewWalletService.stopSync();
                        ewWalletService.startSync();
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

    private void dialogPin_removeAccount(){
        final PinAlertDialogFragment pinAlertDialogFragment= PinAlertDialogFragment.newInstance(R.string.confirm_pin);
        pinAlertDialogFragment.setPositive(new Runnable() {
            @Override
            public void run() {
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(PreferencesActivity.this);
                String pin = sharedPref.getString(Preferences.PIN,null);

                if (pin!=null && pinAlertDialogFragment.getPin().equals(pin)){
                    String passphrase = sharedPref.getString(Preferences.PASSPHRASE,null);
                    if (passphrase!=null){
                        // stop & remove
                        EWWalletService ewWalletService = ((EWApplication) getApplication()).getEwWalletService();
                        ewWalletService.removePasshrase();
                        ewWalletService.stopSync();
                        Toast.makeText(PreferencesActivity.this,"Account removed!",Toast.LENGTH_LONG).show();
                    }
                }else {
                    new AlertDialog.Builder(PreferencesActivity.this)
                            .setTitle("Attention")
                            .setMessage("Invalid PIN. Retry...")
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            }).show();
                }
            }
        });
        pinAlertDialogFragment.show(getSupportFragmentManager(),PinAlertDialogFragment.class.toString());
    }

    private void dialogPin_emptyWallet(){
        final PinAlertDialogFragment pinAlertDialogFragment= PinAlertDialogFragment.newInstance(R.string.confirm_pin);
        pinAlertDialogFragment.setPositive(new Runnable() {
            @Override
            public void run() {
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(PreferencesActivity.this);
                String pin = sharedPref.getString(Preferences.PIN,null);

                if (pin!=null && pinAlertDialogFragment.getPin().equals(pin)){
                    String passphrase = sharedPref.getString(Preferences.PASSPHRASE,null);
                    if (passphrase!=null){
                        // stop & remove
                        dialogEmptyWallet();
                    }
                }else {
                    new AlertDialog.Builder(PreferencesActivity.this)
                            .setTitle("Attention")
                            .setMessage("Invalid PIN. Retry...")
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            }).show();
                }
            }
        });
        pinAlertDialogFragment.show(getSupportFragmentManager(),PinAlertDialogFragment.class.toString());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preferences, menu);
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
}