package it.eternitywall.eternitywall.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Optional;
import com.joanzapata.iconify.widget.IconTextView;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.Http;
import it.eternitywall.eternitywall.Message;
import it.eternitywall.eternitywall.Preferences;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.activity.WriteActivity;
import it.eternitywall.eternitywall.adapters.MessageRecyclerViewAdapter;
import it.eternitywall.eternitywall.bitcoin.Bitcoin;
import it.eternitywall.eternitywall.components.CurrencyView;
import it.eternitywall.eternitywall.dialogfragments.PersonalNodeDialogFragment;
import it.eternitywall.eternitywall.dialogfragments.QRDialogFragment;
import it.eternitywall.eternitywall.dialogfragments.RegAliasDialogFragment;
import it.eternitywall.eternitywall.wallet.EWWalletService;
import it.eternitywall.eternitywall.wallet.WalletObservable;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WalletFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WalletFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WalletFragment extends Fragment implements MessageRecyclerViewAdapter.MessageListAdapterManager{
    private static final String TAG = WalletFragment.class.toString();

    private LinearLayout syncingLayout;
    private LinearLayout syncedLayout;
    private LinearLayout linearLayout;
    private TextView syncingText;
    private TextView currentAddressText;
    private TextView aliasNameText;
    private TextView aliasNameUnconfirmed;
    private TextView btcBalanceUnconfirmed;
    private TextView messagePending;
    private TextView txtHeader;
    private TextView walletWarning;
    private ImageView currentQrCode;
    private ImageView identicon;
    private CurrencyView btcBalance;
    private Button setAliasButton;
    private android.support.design.widget.FloatingActionButton payButton;
    //private ListView myMessageList;
    private List<Message> messages;
    private Integer inQueue;
    private RecyclerView recyclerView;
    private MessageRecyclerViewAdapter messageRecyclerViewAdapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private String cursor;
    private boolean end=false;
    private IconTextView btcQR;

    public WalletFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WalletFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WalletFragment newInstance(String param1, String param2) {
        WalletFragment fragment = new WalletFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate");
        if (savedInstanceState!=null) {
            return;
        }


        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        final FragmentActivity activity = getActivity();
        if(activity!=null) {
            final Intent intent = new Intent(activity, EWWalletService.class);
            activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }


    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        final FragmentActivity activity = getActivity();
        if(activity!=null) {
            activity.unbindService(mConnection);
        }
    }

    boolean connect=false;
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(final ComponentName className,
                                       final IBinder service) {
            Log.i(TAG, ".onServiceConnected()");
            EWApplication ewApplication = (EWApplication) getActivity().getApplication();
            walletObservable = ewApplication.getWalletObservable();   //Should be moved in activity, but this way you have no callback for the listener here
            if(walletObservable!=null && updateUI!=null) {   //could be that the app is detroyed but the service still active, in that case there is a null pointer here, TODO ugly, could cause problem
                walletObservable.addObserver(updateUI);
                updateUI.update(null, null);  //Refresh UI
                connect=true;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, ".onServiceDisconnected()");
            connect=false;
            //walletObservable.deleteObserver(updateUI);
        }

    };

    private WalletObservable walletObservable;
    private Observer updateUI = new Observer() {
        @Override
        public void update(Observable observable, Object data) {
            Log.i(TAG, "update");
            final FragmentActivity activity = getActivity();
            if (activity == null)
                return;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, android.os.Process.myTid() + " TID UI : Refreshing wallet fragment " + walletObservable);

                    final Bitmap IdenticonBitmap = walletObservable.getCurrentIdenticon();
                    if (IdenticonBitmap != null)
                        identicon.setImageBitmap(IdenticonBitmap);
                    final String aliasName1 = walletObservable.getAliasName();
                    if (aliasName1 != null) {
                        aliasNameText.setText(aliasName1);
                        aliasNameText.setVisibility(View.VISIBLE);
                    }

                    if (walletObservable.isSyncedOrPending()) {
                        final Coin walletBalance = walletObservable.getWalletBalance();
                        final Coin walletUnconfirmedBalance = walletObservable.getWalletUnconfirmedBalance();
                        long value1 = walletBalance.getValue();
                        long value2 = walletUnconfirmedBalance.getValue();
                        long value = Math.max(value1, value2);  //Optimistic view
                        final String units = String.valueOf(value);
                        final Bitmap QrCodeBitmap = walletObservable.getCurrentQrCode();

                        final String aliasName = walletObservable.getAliasName();
                        final String unconfirmedAliasName = walletObservable.getUnconfirmedAliasName();

                        if (value > 0) {
                            walletWarning.setVisibility(View.GONE);
                        } else {
                            walletWarning.setVisibility(View.VISIBLE);
                        }

                        syncedLayout.setVisibility(View.VISIBLE);
                        syncingLayout.setVisibility(View.GONE);
                        Address current = walletObservable.getCurrent();
                        currentAddressText.setText(current != null ? current.toString() : "");
                        btcBalance.setUnits(units);
                        btcBalance.refreshUI();
                        if (QrCodeBitmap != null)
                            currentQrCode.setImageBitmap(QrCodeBitmap);


                        if (aliasName == null && unconfirmedAliasName == null) {  //Alias still to be defined
                            setAliasButton.setVisibility(View.VISIBLE);
                            aliasNameText.setVisibility(View.GONE);
                            aliasNameUnconfirmed.setVisibility(View.GONE);
                        } else if (aliasName == null) {                      // unconfirmed alias defined!
                            setAliasButton.setVisibility(View.GONE);
                            aliasNameText.setVisibility(View.VISIBLE);
                            aliasNameUnconfirmed.setVisibility(View.VISIBLE);
                            aliasNameText.setText(unconfirmedAliasName);
                        } else {                                           //alias defined and confirmed
                            setAliasButton.setVisibility(View.GONE);
                            aliasNameText.setVisibility(View.VISIBLE);
                            aliasNameUnconfirmed.setVisibility(View.GONE);
                            aliasNameText.setText(aliasName);
                        }

                        if (value1 != value2) {
                            btcBalanceUnconfirmed.setVisibility(View.VISIBLE);
                        } else {
                            btcBalanceUnconfirmed.setVisibility(View.GONE);
                        }
                        payButton.setVisibility(View.VISIBLE);

                        if (walletObservable.getMessagePending() > 0) {
                            messagePending.setVisibility(View.VISIBLE);
                            messagePending.setText(walletObservable.getMessagePending() + " message" + (walletObservable.getMessagePending() > 1 ? "s" : "") + " pending");
                        } else {
                            messagePending.setVisibility(View.GONE);
                        }

                        // refresh actionbar
                        activity.invalidateOptionsMenu();

                        // Load my messages
                        inQueue = null;
                        loadMoreData();

                    } else if (walletObservable.getState() == WalletObservable.State.SYNCING) {
                        if (activity != null && activity.isFinishing())
                            return;
                        syncedLayout.setVisibility(View.GONE);
                        syncingLayout.setVisibility(View.VISIBLE);
                        setAliasButton.setVisibility(View.INVISIBLE);
                        payButton.setVisibility(View.GONE);
                        if (walletObservable.getPercSync() != null)
                            syncingText.setText(String.format("Syncing (%d%%)", walletObservable.getPercSync()));
                    } else {
                        payButton.setVisibility(View.GONE);
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String passphrase = sharedPref.getString(Preferences.PASSPHRASE, null);
                        if (passphrase == null) {
                            transaction.replace(R.id.root_frame, new HelloFragment());
                            transaction.commitAllowingStateLoss();
                            getFragmentManager().executePendingTransactions();
                        } else {
                            ;//nothing = transaction.replace(R.id.root_frame, new WalletFragment());
                        }
                    }
                }
            });
        }
    };



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        Log.i(TAG, "onCreateView");

        syncingLayout         = (LinearLayout) view.findViewById(R.id.syncingLayout);
        syncedLayout          = (LinearLayout) view.findViewById(R.id.syncedLayout);
        linearLayout          = (LinearLayout) view.findViewById(R.id.linearLayout);
        syncingText           = (TextView) view.findViewById(R.id.syncingText);
        aliasNameText         = (TextView) view.findViewById(R.id.aliasName);
        aliasNameUnconfirmed  = (TextView) view.findViewById(R.id.aliasNameUnconfirmed);
        currentAddressText    = (TextView) view.findViewById(R.id.currentAddress);
        currentQrCode         = (ImageView) view.findViewById(R.id.currentQrCode);
        identicon             = (ImageView) view.findViewById(R.id.identicon);
        btcBalance            = (CurrencyView) view.findViewById(R.id.btcBalance);
        setAliasButton        = (Button) view.findViewById(R.id.setAlias);
        btcBalanceUnconfirmed = (TextView) view.findViewById(R.id.btcBalanceUnconfirmed);
        messagePending        = (TextView) view.findViewById(R.id.messagePending);
        btcQR                 = (IconTextView) view.findViewById(R.id.btcQR);
        txtHeader             = (TextView) view.findViewById(R.id.txtHeader);
        //myMessageList       = (ListView) view.findViewById(R.id.myMessageList);
        recyclerView          = (RecyclerView) view.findViewById(R.id.recyclerview);
        walletWarning         = (TextView) view.findViewById(R.id.wallet_warning);


        btcQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QRDialogFragment qrDialogFragment = new QRDialogFragment();
                qrDialogFragment.show(getFragmentManager(),PersonalNodeDialogFragment.class.toString());
            }
        });

        setAliasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long walletBalance = walletObservable.getWalletBalance().longValue();
                Log.i(TAG,"walletBalance=" + walletBalance);
                if( walletBalance < (EWWalletService.DUST + EWWalletService.FEE) ) {
                    Toast.makeText(getActivity(), "You need at least 0.2 mBTC confirmed balance to register an alias", Toast.LENGTH_LONG).show();
                    return;
                }

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if(prev != null)
                    ft.remove(prev);
                ft.addToBackStack(null);

                RegAliasDialogFragment frag = new RegAliasDialogFragment();
                frag.setWalletObservable(walletObservable);
                frag.show(ft, "dialog");

            }
        });

        currentQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(walletObservable!=null && walletObservable.getCurrent()!=null && Bitcoin.isValidAddress( walletObservable.getCurrent().toString())) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("bitcoin:" + walletObservable.getCurrent().toString()));
                    if(isAvailable(i))
                        startActivity(i);
                    else {
                        AlertDialog d = new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.app_name))
                                .setMessage("There are no bitcoin wallet app installed, do you want to install GreenBits?")
                                .setNegativeButton("No, thanks", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton("Yes" + "!", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.greenaddress.greenbits_android_wallet")));
                                    }
                                })
                                .create();
                        d.show();
                    }
                }
            }
        });




        // Show / Hide write button
        payButton=(android.support.design.widget.FloatingActionButton)view.findViewById(R.id.payButton);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String passphrase=sharedPref.getString(Preferences.PASSPHRASE, null);
        if (passphrase==null){
            // Hide write button on activity if there is no account
            payButton.setVisibility(View.GONE);
        } else {
            // Show write button on activity if there is one account
            payButton.setVisibility(View.VISIBLE);
            payButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), WriteActivity.class);
                    startActivity(i);
                }
            });
        }

        // Set Recyclerview
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager( getActivity().getApplicationContext() );
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);

        // Load my messages
        messages = new ArrayList<Message>();
        inQueue = null;

        // Set Message RecyclerView Adapter
        messageRecyclerViewAdapter = new MessageRecyclerViewAdapter(messages,inQueue,WalletFragment.this);
        recyclerView.setAdapter(messageRecyclerViewAdapter);

        // check on wallet observable
        if(walletObservable==null){
            // do nothig.. wait
        }else if( walletObservable.isSyncedOrPending()) {
            syncedLayout.setVisibility(View.VISIBLE);
            syncingLayout.setVisibility(View.GONE);
            loadMoreData();
        }else {
            syncedLayout.setVisibility(View.GONE);
            syncingLayout.setVisibility(View.VISIBLE);
        }

        return view;
    }

    private boolean isAvailable(Intent intent) {
        final PackageManager mgr = getActivity().getPackageManager();
        List<ResolveInfo> list =
                mgr.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
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
        Log.i(TAG, "onAttach");
        if (walletObservable!=null && connect)
            walletObservable.addObserver(updateUI);

        if (activity instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "onDetach");

        mListener = null;
        if (walletObservable!=null && connect)
            walletObservable.deleteObserver(updateUI);

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
        void onFragmentInteraction(Uri uri);
    }



    @Override
    public void loadMoreData() {
        AsyncTask t = new AsyncTask() {

            private boolean ok = false;
            private String statusMessage=null;
            private List<Message> mMessages = new ArrayList<>();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                txtHeader.setVisibility(View.GONE);

            }

            @Override
            protected void onPostExecute(Object o) {
                if (getActivity()!=null && getActivity().isFinishing())  //exception will null pointer happened here, checking getActivity is null or use isAdded()????
                    return;
                super.onPostExecute(o);


                if (messages.size()==0 && mMessages.size()==0) {
                    Log.i(TAG,"no messages");
                    txtHeader.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                }
                else {
                    Log.i(TAG,"there are messages " + messages.size());
                    txtHeader.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                if(ok) {

                    if(messages != null && !messages.isEmpty()) {
                        messages.addAll(mMessages);
                        final MessageRecyclerViewAdapter messageListAdapter = (MessageRecyclerViewAdapter) recyclerView.getAdapter();
                        messageListAdapter.notifyDataSetChanged();
                    }
                    else {
                        messages.addAll(mMessages);
                        recyclerView.setAdapter(new MessageRecyclerViewAdapter(messages, inQueue, WalletFragment.this));
                    }

                }
                else {
                    //succhia!
                    if(isAdded()) {
                        if (statusMessage != null)
                            Toast.makeText(getActivity(), statusMessage, Toast.LENGTH_SHORT).show();
                        //else
                        //    Toast.makeText(getActivity(), getString(R.string.err_check_internet), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            protected Object doInBackground(Object[] params) {

                if(walletObservable==null || walletObservable.getCurrent()==null || !walletObservable.isSyncedOrPending()) {
                    ok = true;
                    return true;
                }

                if(end)
                    return true;

                String address=walletObservable.getAlias().toString();
                String urlString = "http://eternitywall.it/from/" + address + "?format=json";
                if(cursor!=null)
                    urlString = urlString + "&cursor=" + cursor;
                Log.i(TAG,"apifrom url:" + urlString);
                Optional<String> json = Http.get(urlString);
                /* API EXAMPLE:
                In the user tab, at the bottom, add user messages.
                Api: http://eternitywall.it/from/19U9MAyuyrZcMZxP24zXzTYevAAUKvgp3o?format=json
               Where the address is the alias in the walletobservable
                */

                if(json.isPresent()) {
                    Log.i(TAG,"value present!");
                    try {
                        String jstring = json.get();
                        JSONObject jo = new JSONObject(jstring);

                        try {
                            String status = jo.getString("status");
                            if (status.equals("ko")) {
                                statusMessage= jo.getString("statusMessage");
                                return null;
                            }
                        } catch (Exception ex){
                            ex.printStackTrace();
                        }

                        try {
                            cursor = jo.getString("next");

                        } catch (Exception ex){
                            cursor=null;
                            ex.printStackTrace();
                        }



                        JSONArray ja = jo.getJSONArray("messages");

                        if(ja.length()==0)
                            end=true;


                        for(int m=0; m<ja.length(); m++) {
                            Message message = Message.buildFromJson(ja.getJSONObject(m));
                            mMessages.add(message);
                            Log.i(TAG, message.toString());
                        }

                        //sort by reverse timestamp only on main messages without parsing
                        Collections.sort(mMessages);
                        ok = true;
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Log.i(TAG,"value not present!");
                }
                return null;
            }
        };
        t.execute();
    }
}
