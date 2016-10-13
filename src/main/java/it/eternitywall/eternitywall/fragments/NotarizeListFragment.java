package it.eternitywall.eternitywall.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.orm.SugarContext;

import org.bitcoinj.crypto.DeterministicKey;
import org.spongycastle.util.encoders.Hex;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import it.eternitywall.eternitywall.Http;
import it.eternitywall.eternitywall.Preferences;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.activity.NotarizeDetailActivity;
import it.eternitywall.eternitywall.adapters.DocumentRecyclerViewAdapter;
import it.eternitywall.eternitywall.bitcoin.Bitcoin;
import it.eternitywall.eternitywall.components.Document;
import it.eternitywall.eternitywall.wallet.EWDerivation;

import static java.net.URLEncoder.encode;

public class NotarizeListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NotarizeListFragment newInstance(String param1, String param2) {
        NotarizeListFragment fragment = new NotarizeListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public NotarizeListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    TextView txtHeader;
    ProgressBar progress;
    RecyclerView lstMessages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_notarize_list, container, false);

        // Set TextViews
        txtHeader=(TextView) v.findViewById(R.id.txtHeader);

        // Show / Hide write button
        v.findViewById(R.id.fabNew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeFile();
            }
        });

        // Set Fragment Views
        lstMessages = (RecyclerView) v.findViewById(R.id.lstMessages);

        // Set Recyclerview
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager( getActivity().getApplicationContext() );
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        lstMessages.setLayoutManager(mLayoutManager);

        // Init DB
        SugarContext.init(getActivity());
        // Put elements
        refresh();
        // Set Swipe on refresh scroll-upper event
        final SwipeRefreshLayout swipe = (SwipeRefreshLayout) v.findViewById(R.id.activity_main_swipe_refresh_layout);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe.setRefreshing(false);
                refresh();
            }
        });
        progress = (ProgressBar) v.findViewById(R.id.progress);


        return v;
    }

    private void refresh(){
        List<Document> documents = Document.find(Document.class, null, null, null, "createdat DESC",null);
        DocumentRecyclerViewAdapter documentListAdapter = new DocumentRecyclerViewAdapter(documents);
        lstMessages.setAdapter( documentListAdapter );

        if(documents.isEmpty()){
            txtHeader.setVisibility(View.VISIBLE);
        }else{
            txtHeader.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Fragment currentFragment=getFragmentManager().findFragmentById(R.id.root_frame);
        //getFragmentManager().putFragment(outState,"currentFragment",currentFragment);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    // pick request code
    private static final int PICK_FILE_REQUEST = 100;

    private Long id_document;

    // Choose file with intent
    public void takeFile(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    // Retrieve file from intent
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String path=null,hash=null;

        if (requestCode == PICK_FILE_REQUEST) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                path=uri.toString();
                Log.d("NOTARIZE",uri.getPath());

                // set persistent Authorization to read the file from data storage
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    getActivity().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION );
                }

                // set image
                /*try {
                    InputStream is = getActivity().getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    if(bitmap==null){
                        Log.d("NOTARIZE","no image file");
                        //imageView.setImageDrawable(getResources().getDrawable(R.drawable.eternity_logo));
                    }else {
                        //imageView.setImageBitmap(bitmap);
                    }
                }catch(Exception e){
                    Log.d("NOTARIZE","no image file");
                    //imageView.setImageDrawable(getResources().getDrawable(R.drawable.eternity_logo));
                }*/

                // read file
                byte[] bytes=null;
                try {
                    InputStream is = getActivity().getContentResolver().openInputStream(uri);
                    int size = (int) is.available();
                    bytes = new byte[size];
                    is.read(bytes, 0, bytes.length);
                    is.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // build digest
                try {
                    MessageDigest digest = null;
                    digest = MessageDigest.getInstance("SHA-256");
                    digest.update(bytes);
                    hash = Hex.toHexString(digest.digest());
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                // run
                if(path!=null && hash!=null){
                    sendMessage(path,hash);
                }else {
                    dialogFailure();
                }

            }
        }
    }


    // Send hash message
    public void sendMessage(final String path,final String hash) {

        // Check if the message was just notarized = check into documents DB
        boolean found=false;
        List<Document>  documents = Document.find(Document.class, null, null, null, "createdat DESC",null);
        for (int i=0;i<documents.size();i++){
            if(documents.get(i).hash.equals(hash)){
                found=true;
            }
        }
        if(found==true){
            dialogExistDocument();
            return;
        }

        AsyncTask<Void,Void,Boolean> t = new AsyncTask<Void,Void,Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progress.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Boolean res) {
                super.onPostExecute(res);
                progress.setVisibility(View.GONE);

                if (res==true)
                    dialogSuccess();
                else
                    dialogFailure();

            }

            @Override
            protected Boolean doInBackground(Void... params) {


                try {
                    final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String passphrase = sharedPref.getString(Preferences.PASSPHRASE, null);
                    if (passphrase == null)
                        return false;
                    //passphrase="kiss clap snap wear alter desk rally dance donate lava adult notice";
                    byte[] mySeed = Bitcoin.getEntropyFromPassphrase(passphrase);
                    EWDerivation ewDerivation = new EWDerivation(mySeed);


                    final DeterministicKey alias = ewDerivation.getAlias();
                    final String aliasString=Bitcoin.keyToStringAddress(alias);
                    final String challenge = System.currentTimeMillis() + "";
                    final String signature=alias.signMessage(challenge);


                    //String hash = Hex.toHexString(buffer);
                    String url = "https://eternitywall.it/v1/auth/hash/%s?account=%s&signature=%s&challenge=%s";
                    String urlString = String.format(url, hash, encode(aliasString), encode(signature), encode(challenge));
                    Optional<String> result = Http.post(urlString, "", "");

                    // Check result
                    Log.d("NOTARIZE","url : "+urlString);
                    Log.d("NOTARIZE","present : "+result.isPresent());
                    Log.d("NOTARIZE","result : "+result.get());


                    // Save document into DB
                    Document document = new Document();
                    document.path=path;
                    document.hash=hash;
                    document.created_at= System.currentTimeMillis();
                    document.stamped_at=new Long(0);
                    document.stamp="";
                    document.signature=signature;
                    document.challenge=challenge;
                    document.save();
                    id_document=document.getId();

                    return result.isPresent();

                } catch (Exception e) {
                    return false;
                }

            }
        };
        t.execute();
    }


    private void dialogSuccess() {
        Log.i(getClass().toString(),"showing dialogSuccess");
        android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Success");
        alertDialog.setMessage("Message successfully notarized.");
        alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Refresh content
                List<Document> documents = Document.find(Document.class, null, null, null, "createdat DESC",null);
                DocumentRecyclerViewAdapter documentListAdapter = new DocumentRecyclerViewAdapter(documents);
                lstMessages.setAdapter( documentListAdapter );

                if(documents.isEmpty()){
                    txtHeader.setVisibility(View.VISIBLE);
                }else{
                    txtHeader.setVisibility(View.GONE);
                }

                // show message detail
                Intent intent = new Intent(getActivity(), NotarizeDetailActivity.class);
                intent.putExtra("id",id_document);
                startActivity(intent);
            }
        });
        alertDialog.setCancelable(false);
        android.support.v7.app.AlertDialog alert = alertDialog.create();
        alert.show();
    }
    private void dialogFailure() {
        Log.i(getClass().toString(),"showing dialogFailure");
        android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Failure");
        alertDialog.setMessage("Try again.");
        alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ;
            }
        });
        alertDialog.setCancelable(false);
        android.support.v7.app.AlertDialog alert = alertDialog.create();
        alert.show();
    }
    private void dialogExistDocument() {
        android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Attention");
        alertDialog.setMessage("This document was already notarized.");
        alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ;
            }
        });
        alertDialog.setCancelable(true);
        android.support.v7.app.AlertDialog alert = alertDialog.create();
        alert.show();
    }
}
