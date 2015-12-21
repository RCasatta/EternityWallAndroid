package it.eternitywall.eternitywall.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.bitcoinj.core.Coin;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.bitcoin.Bitcoin;
import it.eternitywall.eternitywall.components.CurrencyView;
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
public class WalletFragment extends Fragment {
    private static final String TAG = WalletFragment.class.toString();

    private RelativeLayout syncingLayout;
    private LinearLayout syncedLayout;
    private TextView syncingText;
    private TextView currentAddressText;
    private TextView aliasNameText;
    private TextView btcBalanceUnconfirmed;
    private ImageView currentQrCode;
    private ImageView identicon;
    private CurrencyView btcBalance;
    private Button setAliasButton;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

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
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, ".onServiceDisconnected()");
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
                    if (walletObservable.getState() == WalletObservable.State.SYNCED) {
                        final Coin walletBalance = walletObservable.getWalletBalance();
                        final Coin walletUnconfirmedBalance = walletObservable.getWalletUnconfirmedBalance();
                        long value1 = walletBalance.getValue();
                        long value2 = walletUnconfirmedBalance.getValue();
                        long value = Math.max(value1, value2) ;  //Optimistic view
                        final String units = String.valueOf(value);
                        final Bitmap QrCodeBitmap = walletObservable.getCurrentQrCode();
                        final Bitmap IdenticonBitmap = walletObservable.getCurrentIdenticon();
                        final String aliasName = walletObservable.getAliasName();

                        syncedLayout.setVisibility(View.VISIBLE);
                        syncingLayout.setVisibility(View.GONE);
                        currentAddressText.setText(walletObservable.getCurrent().toString());
                        btcBalance.setUnits(units);
                        btcBalance.refreshUI();
                        if (QrCodeBitmap != null)
                            currentQrCode.setImageBitmap(QrCodeBitmap);
                        if (IdenticonBitmap != null)
                            identicon.setImageBitmap(IdenticonBitmap);
                        if(aliasName!=null) {
                            aliasNameText.setText(aliasName);
                            aliasNameText.setVisibility(View.VISIBLE);
                            setAliasButton.setVisibility(View.GONE);
                        } else {
                            aliasNameText.setVisibility(View.GONE);
                            setAliasButton.setVisibility(View.VISIBLE);
                        }
                        if(value1!=value2) {
                            btcBalanceUnconfirmed.setVisibility(View.VISIBLE);
                        } else {
                            btcBalanceUnconfirmed.setVisibility(View.GONE);
                        }

                    } else if (walletObservable.getState() == WalletObservable.State.SYNCING) {
                        syncedLayout.setVisibility(View.GONE);
                        syncingLayout.setVisibility(View.VISIBLE);
                        if (walletObservable.getPercSync() != null)
                            syncingText.setText(String.format("Syncing (%d%%)", walletObservable.getPercSync()));
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

        syncingLayout = (RelativeLayout) view.findViewById(R.id.syncingLayout);
        syncedLayout = (LinearLayout) view.findViewById(R.id.syncedLayout);
        syncingText = (TextView) view.findViewById(R.id.syncingText);
        aliasNameText = (TextView) view.findViewById(R.id.aliasName);
        currentAddressText = (TextView) view.findViewById(R.id.currentAddress);
        currentQrCode = (ImageView) view.findViewById(R.id.currentQrCode);
        identicon = (ImageView) view.findViewById(R.id.identicon);
        btcBalance = (CurrencyView) view.findViewById(R.id.btcBalance);
        setAliasButton = (Button) view.findViewById(R.id.setAlias);
        btcBalanceUnconfirmed = (TextView) view.findViewById(R.id.btcBalanceUnconfirmed);

        setAliasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long walletBalance = walletObservable.getWalletUnconfirmedBalance().longValue();
                Log.i(TAG,"walletBalance=" + walletBalance);
                if( walletBalance < (EWWalletService.DUST + EWWalletService.FEE) ) {
                    Toast.makeText(getActivity(), "You need at least 0.2 mBTC to register an alias", Toast.LENGTH_LONG).show();
                    return;
                }

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if(prev != null)
                    ft.remove(prev);
                ft.addToBackStack(null);

                RegAliasDialogFragment frag = new RegAliasDialogFragment(walletObservable);
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


        if(walletObservable!=null && walletObservable.getState()== WalletObservable.State.SYNCED) {
            syncedLayout.setVisibility(View.VISIBLE);
            syncingLayout.setVisibility(View.GONE);
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
}
