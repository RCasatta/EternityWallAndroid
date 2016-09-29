package it.eternitywall.eternitywall.activity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Optional;

import org.bitcoinj.crypto.DeterministicKey;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;

import it.eternitywall.eternitywall.Http;
import it.eternitywall.eternitywall.Preferences;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.bitcoin.Bitcoin;
import it.eternitywall.eternitywall.components.Document;
import it.eternitywall.eternitywall.wallet.EWDerivation;

import static java.net.URLEncoder.encode;

public class NotarizeDetailActivity extends AppCompatActivity {

    Long id= new Long(0);
    Document document=null;


    ImageView imageView;
    TextView txtHash,txtPath,txtCreated,txtStamped;
    Button btnShow;
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
        txtCreated= (TextView)findViewById(R.id.txtCreated);
        txtStamped= (TextView)findViewById(R.id.txtStamped);
        btnShow= (Button)findViewById(R.id.btnShow);

        txtHash.setText(document.hash);
        txtPath.setText(document.path);
        txtCreated.setText(getDate(document.created_at));
        txtStamped.setText("");

        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView showText = new TextView(NotarizeDetailActivity.this);
                showText.setText(document.stamp);
                showText.setTextIsSelectable(true);

                new AlertDialog.Builder(NotarizeDetailActivity.this)
                        .setTitle("Stamp")
                        .setView(showText)
                        .setPositiveButton(android.R.string.ok, null)
                        .setCancelable(false)
                        .create().show();
            }
        });

        // Set the imageview retrieved from the document
        try {
            Uri uri = Uri.parse(document.path);
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            imageView.setImageBitmap(bitmap);
        }catch (Exception e ) {
            e.printStackTrace();
            Log.d(getClass().toString(),e.getLocalizedMessage());
        }

        checkMessage(document.hash);

    }
    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd MMMM yyyy HH:mm", cal).toString();
        return date;
    }


    public void checkMessage(final String hash) {

        AsyncTask<Void,Void,Boolean> t = new AsyncTask<Void,Void,Boolean>() {
            JSONObject jObj;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Boolean res) {
                super.onPostExecute(res);
                try {
                    document.stamp=jObj.toString();
                    document.stamped_at=jObj.getLong("timestamp") * 1000;
                    txtStamped.setText(getDate(document.stamped_at) );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    //String hash = Hex.toHexString(buffer);
                    String url = "https://eternitywall.it/v1/hash/%s";
                    String urlString = String.format(url, hash);
                    Optional<String>json = Http.get(urlString);
                    if(json!=null && json.isPresent()) {
                        try {
                            String jstring = json.get();
                            jObj = new JSONObject(jstring);
                        } catch (Exception ex) {
                            Log.i(getClass().toString(), "no value in json " + ex.getMessage());
                        }
                    }else{
                        return false;
                    }

                } catch (Exception e) {
                    return false;
                }
                return true;
            }
        };
        t.execute();
    }
}
