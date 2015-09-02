package it.eternitywall.eternitywall;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Optional;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.eternitywall.eternitywall.adapters.MessageListAdapter;


public class WriteActivity extends ActionBarActivity {

    private static final Integer MAX_LENGTH = 72;

    private static final String TAG = "WriteActivity";

    private EditText txtMessage;
    private TextView txtCounter;
    private ProgressBar progress;

    private String curmsg = "";
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        txtMessage = (EditText) findViewById(R.id.txtMessage);
        if(getIntent().getExtras() != null && getIntent().getStringExtra("sharedText") != null)
            txtMessage.setText(getIntent().getStringExtra("sharedText"));

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

                if(curmsg.isEmpty()) {
                    Toast.makeText(WriteActivity.this, "Empty messages are not allowed", Toast.LENGTH_SHORT).show();
                    return;
                }

                AsyncTask t = new AsyncTask() {

                    private long value;
                    private String address;
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

                        if(ok) {

                        }
                        else {
                            //succhia!
                            Toast.makeText(WriteActivity.this, getString(R.string.err_check_internet), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    protected Object doInBackground(Object[] params) {
                        Optional<String> json = null;
                        try {
                            json = Http.get("http://eternitywall.it/bitcoinform?format=json&text=" + URLEncoder.encode(curmsg, "UTF-8") + "&source=" + getApplicationContext().getPackageName());
                            if(json.isPresent()) {
                                String jstring = json.get();
                                JSONObject jo = new JSONObject(jstring);

                                address = jo.getString("address");
                                value = jo.getLong("value");

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
        });

        /**
         * EW PROTOCOL
         *
         * input pau the fee
         * EWAxenoky set nick "xenoky" to first input of transaction
         *
         *
         * first input is the sender
         * other input are ignored
         * first output is the message id (DUST value)
         * second output (optional) if the message id answering to
         * OP_RETURN EW message
         * other outputs are ignored
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

         *
         */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast.makeText(this, "requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data , Toast.LENGTH_LONG).show();
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
