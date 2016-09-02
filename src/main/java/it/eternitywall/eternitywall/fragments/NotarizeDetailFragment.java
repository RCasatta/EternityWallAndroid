package it.eternitywall.eternitywall.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Optional;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.bitcoinj.crypto.DeterministicKey;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import it.eternitywall.eternitywall.Http;
import it.eternitywall.eternitywall.Preferences;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.bitcoin.Bitcoin;
import it.eternitywall.eternitywall.components.Document;
import it.eternitywall.eternitywall.wallet.EWDerivation;

import static java.net.URLEncoder.encode;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NotarizeDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NotarizeDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotarizeDetailFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int PICK_CAMERA_REQUEST = 100;
    private static final int PICK_IMAGE_REQUEST = 101;
    private static final int PICK_FILE_REQUEST = 102;
    private static final int RESULT_OK = 0;

    private static final float alpha=(float)0.5;
    private static final String TAG = "NOTARIZE";

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
    public static NotarizeDetailFragment newInstance(String param1, String param2) {
        NotarizeDetailFragment fragment = new NotarizeDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public NotarizeDetailFragment() {
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

    ImageView imageView;
    TextView txtHash,txtPath;
    ProgressBar progress;
    String path="";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_notarize_new, container, false);

        imageView= (ImageView)v.findViewById(R.id.imageView);
        txtHash= (TextView)v.findViewById(R.id.txtHash);
        txtPath= (TextView)v.findViewById(R.id.txtPath);

        FloatingActionButton fabCamera=(FloatingActionButton)v.findViewById(R.id.fabCamera);
        FloatingActionButton fabPhoto=(FloatingActionButton)v.findViewById(R.id.fabPhoto);
        FloatingActionButton fabFile=(FloatingActionButton)v.findViewById(R.id.fabFile);
        FloatingActionButton fabPosition=(FloatingActionButton)v.findViewById(R.id.fabPosition);

        fabCamera.setImageDrawable(new IconDrawable(getActivity(), FontAwesomeIcons.fa_camera).color(Color.WHITE).sizeDp(16));
        fabPhoto.setImageDrawable(new IconDrawable(getActivity(), FontAwesomeIcons.fa_photo).color(Color.WHITE).sizeDp(16));
        fabFile.setImageDrawable(new IconDrawable(getActivity(), FontAwesomeIcons.fa_file).color(Color.WHITE).sizeDp(16));
        fabPosition.setImageDrawable(new IconDrawable(getActivity(), FontAwesomeIcons.fa_location_arrow).color(Color.WHITE).sizeDp(16));


        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    takePictureButton.setEnabled(false);
                    getActivity().requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
                }*/
                takeCamera();
            }
        });

        fabFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeFile();
            }
        });
        fabPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeGallery();
            }
        });


        progress = (ProgressBar) v.findViewById(R.id.progress);

        Button btnSend = (Button) v.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(txtHash.getText().toString());
            }
        });
        return v;
    }

    Uri file;

    public void takeCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = Uri.fromFile(getOutputMediaFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, file);
        startActivityForResult(intent, PICK_CAMERA_REQUEST);
    }
    public  void takeGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    public void takeFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                Log.d("NOTARIZE",uri.getPath());
                path=uri.getPath();

                // set image
                try {
                    InputStream is = getActivity().getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    if(bitmap==null){
                        Log.d("NOTARIZE","no image file");
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.eternity_logo));
                    }else {
                        imageView.setImageBitmap(bitmap);
                    }
                }catch(Exception e){
                    Log.d("NOTARIZE","no image file");
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.eternity_logo));
                }

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
                    String hash = Hex.toHexString(digest.digest());
                    txtHash.setText(hash.toString());

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

            }
        } else if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                Uri uri = data.getData();
                path=uri.getPath();
                try {
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;

                    //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);


                    Bitmap bitmap =decodeSampledBitmapFromUri(uri, 100, 100);
                    Log.d("NOTARIZE", String.valueOf(bitmap));
                    imageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("NOTARIZE","error image file");
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.eternity_logo));
                }


                // build digest
                /*InputStream is = getActivity().getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                try {
                    MessageDigest digest = null;
                    digest = MessageDigest.getInstance("SHA-256");
                    digest.update(bytes);
                    String hash = Hex.toHexString(digest.digest());
                    txtHash.setText(hash.toString());

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }*/
            }
        } else if (requestCode == PICK_CAMERA_REQUEST) {
            Uri selectedImage = file;
            path=selectedImage.getPath();
            getActivity().getContentResolver().notifyChange(selectedImage, null);
            ContentResolver cr = getActivity().getContentResolver();
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media
                        .getBitmap(cr, selectedImage);
                imageView.setImageBitmap(bitmap);
                Toast.makeText(getActivity(), selectedImage.toString(), Toast.LENGTH_LONG).show();

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                try {
                    MessageDigest digest = null;
                    digest = MessageDigest.getInstance("SHA-256");
                    digest.update(byteArray);
                    String hash = Hex.toHexString(digest.digest());
                    txtHash.setText(hash.toString());

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                Toast.makeText(getActivity(), "Failed to load", Toast.LENGTH_SHORT)
                        .show();
                Log.d("NOTARIZE","error image from camera file");
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.eternity_logo));
            }
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = Math.round((float)width / (float)reqWidth);
            }
        }
        return inSampleSize;
    }

    public Bitmap decodeSampledBitmapFromUri(Uri uri,
                                                    int reqWidth, int reqHeight) throws FileNotFoundException {


        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream (getActivity().getContentResolver().openInputStream(uri), null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(uri), null, options);
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(getActivity(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    private static File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "EW");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");
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



    public void sendMessage(final String message) {
        Log.i(TAG,"sending message " + message);
        final String hash = txtHash.getText().toString();

        AsyncTask<Void,Void,Boolean> t = new AsyncTask<Void,Void,Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progress.setVisibility(View.VISIBLE);


            }

            @Override
            protected void onPostExecute(Boolean res) {
                super.onPostExecute(res);

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
                    passphrase="kiss clap snap wear alter desk rally dance donate lava adult notice";
                    byte[] mySeed = Bitcoin.getEntropyFromPassphrase(passphrase);
                    EWDerivation ewDerivation = new EWDerivation(mySeed);


                    final DeterministicKey alias = ewDerivation.getAlias();
                    //final String aliasString = Bitcoin.keyToStringAddress(alias);
                    final String aliasString="17cua9VpaJqTLQ2PArDog6DWapcDZrPba2";
                    final String challenge = System.currentTimeMillis() + "";
                    final String signature=alias.signMessage(challenge);

                    //String fakeInput = "This is the string that your fake input stream will return";
                    //MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    //digest.update(fakeInput.getBytes());
        /*DigestInputStream digestInputStream = new DigestInputStream(p.getInputStream(), digest);
        while (digestInputStream.read() != -1) {
        }
        digestInputStream.close();*/

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
                    document.save();

                    return result.isPresent();

                } catch (Exception e) {
                    return false;
                }

            }
        };
        t.execute();
    }




/*
    public boolean registerMessage(String message){
        Log.i(TAG,"registerMessage " + message );

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String passphrase = sharedPref.getString(Preferences.PASSPHRASE, null);
        if (passphrase == null)
            return false;
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


        final int index=0;  //TODO this need to grow!
        final DeterministicKey external = ewDerivation.getAccount(index);//.getExternal(index);
        final String pubKey    = Hex.toHexString(external.getPubKey());
        final String chainCode = Hex.toHexString(external.getChainCode());

        map.put("message",message);
        map.put("txHash",curTx.getHashAsString());
        map.put("pubKey",pubKey);
        map.put("chainCode",chainCode);
        map.put("index", index);
        Log.i(TAG, "calling " + url);
        Log.i(TAG,"with param " + map);

        final Optional<String> stringOptional = Http.postForm(url, map);
        Log.i(TAG,"returning " + stringOptional);
        return stringOptional.isPresent();
    }



    private Transaction curTx;

    private void createTx(String message){
        Log.i(TAG,"createTx " + message);

        String messageHash=null;

        messageHash=message;

        if(messageHash!=null && !messageHash.isEmpty()) {
            final EWWalletService ewWalletService = ((EWApplication) getActivity().getApplication()).getEwWalletService();
            try {
                curTx = ewWalletService.createMessageTx(messageHash, null);
                Log.i(TAG , "created transaction " + curTx.getHashAsString() );
                Log.i(TAG, Bitcoin.transactionToHex(curTx));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Wait the pending message", Toast.LENGTH_LONG).show();
                return;
            } catch (AddressFormatException e) {
                Toast.makeText(getActivity(), "Not more 100 messages available", Toast.LENGTH_LONG).show();
                return;
            } catch (InsufficientMoneyException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Insufficient coin", Toast.LENGTH_LONG).show();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Exception", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }


    private void sendBroadcast() {
        Log.i(TAG,"broadcasting...");
        final EWWalletService ewWalletService = ((EWApplication) getActivity().getApplication()).getEwWalletService();
        final TransactionBroadcast transactionBroadcast = ewWalletService.broadcastTransaction(curTx);

        try {
            Transaction transaction = transactionBroadcast.future().get();
            if (transaction == null) {
                Toast.makeText(getActivity(), "Error broadcasting transaction!", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(getActivity(), "Message sent! You'll be notified when written", Toast.LENGTH_LONG).show();

                final String hash = transaction.getHashAsString();
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
            Toast.makeText(getActivity(), "Error broadcasting message! Please retry", Toast.LENGTH_LONG).show();
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

*/
    private void dialogSuccess() {
        Log.i(TAG,"showing dialog");
        android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Success");
        alertDialog.setMessage("Message successfully notarized.");
        alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });
        alertDialog.setCancelable(false);
        android.support.v7.app.AlertDialog alert = alertDialog.create();
        alert.show();
    }
    private void dialogFailure() {
        Log.i(TAG,"showing dialog");
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
}
