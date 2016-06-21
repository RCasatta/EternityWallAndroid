package it.eternitywall.eternitywall.activity;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.fragments.NotarizeNewFragment;

public class NotarizeActivity extends AppCompatActivity implements NotarizeNewFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notarize);

        // Specify that tabs should be displayed in the action bar.
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("NOTARIZE");
        setSupportActionBar(toolbar);*/

        setTitle("NOTARIZE");
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
