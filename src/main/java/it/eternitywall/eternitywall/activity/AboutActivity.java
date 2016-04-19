package it.eternitywall.eternitywall.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Iconify.with(new FontAwesomeModule());

        Button contact = (Button) findViewById(R.id.btnContact);
        Button visit   = (Button) findViewById(R.id.btnVisit);

        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addresses[] = { "feedback@eternitywall.it" };
                composeEmail(addresses, "Feedback from Android app");
            }
        });

        visit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://eternitywall.it";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

    }


    //from https://developer.android.com/guide/components/intents-common.html#Email
    public void composeEmail(String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
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
