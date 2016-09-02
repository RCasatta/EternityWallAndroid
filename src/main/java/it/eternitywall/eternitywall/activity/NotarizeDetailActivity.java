package it.eternitywall.eternitywall.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.components.Document;
import it.eternitywall.eternitywall.fragments.NotarizeNewFragment;

public class NotarizeDetailActivity extends AppCompatActivity implements NotarizeNewFragment.OnFragmentInteractionListener{

    Long id= new Long(0);
    Document document=null;


    ImageView imageView;
    TextView txtHash,txtPath,txtCreated,txtStamped,txtStamp;
    ProgressBar progress;
    String path="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_notarize);


        try {
            id = getIntent().getLongExtra("id",0);
        } catch (Exception e) {
            //succhia!
            Toast.makeText(NotarizeDetailActivity.this, getString(R.string.err_check_internet), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            document = Document.findById( Document.class , id );
        } catch (Exception e) {
            //succhia!
            Toast.makeText(NotarizeDetailActivity.this, getString(R.string.err_check_internet), Toast.LENGTH_SHORT).show();
            return;
        }


        imageView= (ImageView)findViewById(R.id.imageView);
        txtHash= (TextView)findViewById(R.id.txtHash);
        txtPath= (TextView)findViewById(R.id.txtPath);
        /*txtCreated= (TextView)findViewById(R.id.txtCreated);
        txtStamped= (TextView)findViewById(R.id.txtStamped);
        txtStamp= (TextView)findViewById(R.id.txtStamp);

        txtHash.setText( document.hash );
        txtPath.setText( document.path );
        txtCreated.setText( getDate(document.created_at) );
        txtStamped.setText( getDate(document.stamped_at) );
        txtStamp.setText( document.stamp);
*/

    }
    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd MMMM yyyy HH:mm", cal).toString();
        return date;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
