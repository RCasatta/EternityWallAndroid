package it.eternitywall.eternitywall.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.R;
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
    private RelativeLayout syncingLayout;
    private LinearLayout syncedLayout;
    private TextView syncingText;
    private TextView balanceText;
    private TextView currentAddressText;
    private ImageView currentQrCode;

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
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(final ComponentName className,
                                       final IBinder service) {
            EWApplication ewApplication = (EWApplication) getActivity().getApplication();
            final WalletObservable walletObservable = ewApplication.getWalletObservable();
            walletObservable.addObserver(new Observer() {
                @Override
                public void update(Observable observable, Object data) {
                    final FragmentActivity activity = getActivity();
                    if (activity == null)
                        return;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (walletObservable.getState() == WalletObservable.State.SYNCED) {
                                syncedLayout.setVisibility(View.VISIBLE);
                                syncingLayout.setVisibility(View.GONE);
                                balanceText.setText(walletObservable.getWalletBalance().toPlainString());
                                currentAddressText.setText(walletObservable.getCurrent().toString());

                            } else if (walletObservable.getState() == WalletObservable.State.SYNCING) {
                                syncedLayout.setVisibility(View.GONE);
                                syncingLayout.setVisibility(View.VISIBLE);
                                if (walletObservable.getPercSync() != null)
                                    syncingText.setText(String.format("Syncing (%d%%)", walletObservable.getPercSync()));
                            }
                        }
                    });
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

    };



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        syncingLayout = (RelativeLayout) view.findViewById(R.id.syncingLayout);
        syncedLayout = (LinearLayout) view.findViewById(R.id.syncedLayout);
        syncingText = (TextView) view.findViewById(R.id.syncingText);
        balanceText = (TextView) view.findViewById(R.id.balance);
        currentAddressText = (TextView) view.findViewById(R.id.currentAddress);
        currentQrCode = (ImageView) view.findViewById(R.id.currentQrCode);

        return view;
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
