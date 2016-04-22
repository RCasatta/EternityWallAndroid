package it.eternitywall.eternitywall.activity;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.common.base.Optional;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.Http;
import it.eternitywall.eternitywall.Preferences;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.dialogfragments.NotifyDialogFragment;


public class ThxActivity extends ActionBarActivity {

    private static final String TAG = "ThxActivity";
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thx);
        Iconify.with(new FontAwesomeModule());

        Button btn = (Button) findViewById(R.id.btnNotify);


        if(getIntent().getExtras() != null && getIntent().getStringExtra("address") != null)
            address = getIntent().getStringExtra("address");

        SharedPreferences sp = getSharedPreferences(Application.class.getSimpleName(), MODE_PRIVATE);

        //something wrong!! address should always be set...
        if(address == null) {
            btn.setText("OK");
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        else if(sp.getString(Preferences.EMAIL, "").isEmpty()) {
            findViewById(R.id.btnNotify).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
                    if(prev != null)
                        ft.remove(prev);
                    ft.addToBackStack(null);

                    NotifyDialogFragment frag = new NotifyDialogFragment();
                    frag.setAddress(address);
                    frag.show(ft, "dialog");
                }
            });

        }
        else {
            btn.setText("OK");
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            final String email = sp.getString(Preferences.EMAIL, "");
            final boolean ckOne = sp.getBoolean(Preferences.CHK_ONE , true);
            final boolean ckTwo = sp.getBoolean(Preferences.CHK_TWO , true);

            //and offline notify
            AsyncTask t = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    try {
                        //https://eternitywall.appspot.com/v1/notify?email=[email]&hash=[hash]&address=[address]&subscribe=[subscribe]&notifyreply=[notifyreply]
                        Optional<String> res = Http.get(
                                "https://eternitywall.appspot.com/v1/notify?" +
                                        "email=" + email + "&address=" + address +
                                        (ckOne ? "&subscribe=true" : "") +
                                        (ckTwo ? "&notifyreply=true" : ""));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }return null;
                }
            };
            t.execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
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
        Toast.makeText(this, "requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data , Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((EWApplication)getApplication()).onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((EWApplication)getApplication()).onResume();
    }
}
