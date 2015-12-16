package it.eternitywall.eternitywall.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.Preferences;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.wallet.EWWalletService;

public class PreferencesActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);


        findViewById(R.id.llResync).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (existAccount()) {
                    EWWalletService ewWalletService = ((EWApplication) getApplication()).getEwWalletService();
                    ewWalletService.stopSync();
                }else
                    dialogCreateAccount();
            }
        });
        findViewById(R.id.llAbout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PreferencesActivity.this, AboutActivity.class));
            }
        });
        findViewById(R.id.llRemove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (existAccount()) {
                    EWWalletService ewWalletService = ((EWApplication) getApplication()).getEwWalletService();
                    ewWalletService.stopSync();
                    dialogPin_removePassphrase();
                }else
                    dialogCreateAccount();
            }
        });
        findViewById(R.id.llPassphrase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (existAccount())
                    dialogPin_showPassphrase();
                else
                    dialogCreateAccount();
            }
        });


        ((Switch)findViewById(R.id.switchDonation)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
        ((LinearLayout)findViewById(R.id.llDebug)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (existAccount())
                    startActivity(new Intent(PreferencesActivity.this, DebugActivity.class));
                else
                    dialogCreateAccount();
            }
        });
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
        alertDialog.setMessage("Account not defined, please crate a wallet.");
        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    private void dialogPin_showPassphrase(){
        AlertDialog.Builder builder = new AlertDialog.Builder(PreferencesActivity.this)
                .setTitle("Insert PIN");
        final FrameLayout frameView = new FrameLayout(PreferencesActivity.this);
        final EditText editText = new EditText(PreferencesActivity.this);
        editText.setHint("****");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(getResources().getDimensionPixelSize(R.dimen.padding_high), getResources().getDimensionPixelSize(R.dimen.padding_high), getResources().getDimensionPixelSize(R.dimen.padding_high), getResources().getDimensionPixelSize(R.dimen.padding_high));
        editText.setLayoutParams(params);
        frameView.addView(editText, params);
        builder.setView(frameView);
        builder.setPositiveButton(getResources().getString(R.string.btn_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(PreferencesActivity.this);
                String pin = sharedPref.getString(Preferences.PIN, null);
                if (pin != null && editText.getText().toString().equals(pin)) {
                    String passphrase = sharedPref.getString(Preferences.PASSPHRASE, null);
                    if (passphrase != null) {
                        // SHOW
                        TextView txtPassphrase = (TextView) findViewById(R.id.txtPassphrase);
                        txtPassphrase.setText(passphrase);
                    }
                } else {
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
        builder.create().show();
    }


    private void dialogPin_removePassphrase(){
        AlertDialog.Builder builder = new AlertDialog.Builder(PreferencesActivity.this)
                .setTitle("Insert PIN");
        final FrameLayout frameView = new FrameLayout(PreferencesActivity.this);
        final EditText editText = new EditText(PreferencesActivity.this);
        editText.setHint("****");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(getResources().getDimensionPixelSize(R.dimen.padding_high), getResources().getDimensionPixelSize(R.dimen.padding_high), getResources().getDimensionPixelSize(R.dimen.padding_high), getResources().getDimensionPixelSize(R.dimen.padding_high));
        editText.setLayoutParams(params);
        frameView.addView(editText,params);
        builder.setView(frameView);
        builder.setPositiveButton(getResources().getString(R.string.btn_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(PreferencesActivity.this);
                String pin = sharedPref.getString(Preferences.PIN,null);
                if (pin!=null && editText.getText().toString().equals(pin)){
                    String passphrase = sharedPref.getString(Preferences.PASSPHRASE,null);
                    if (passphrase!=null){
                        // REMOVE


                        // remove preferences
                        final SharedPreferences.Editor edit = sharedPref.edit();
                        edit.putString(Preferences.PASSPHRASE, null);
                        edit.putString(Preferences.PIN, null);
                        edit.commit();

                        // stop observable

                        EWWalletService ewWalletService = ((EWApplication) getApplication()).getEwWalletService();
                        ewWalletService.stopSync();

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
        builder.create().show();
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