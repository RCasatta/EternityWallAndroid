package it.eternitywall.eternitywall.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import it.eternitywall.eternitywall.IdenticonGenerator;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.bitcoin.Bitcoin;
import it.eternitywall.eternitywall.wallet.EWDerivation;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateFragment extends Fragment {
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
     * @return A new instance of fragment CreateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateFragment newInstance(String param1, String param2) {
        CreateFragment fragment = new CreateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public CreateFragment() {
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

    private byte[] seed;
    private String passphrase;
    private EWDerivation ewDerivation;
    private String alias;
    private Bitmap identicon;
    private void newPassphrase() {
        seed=Bitcoin.getRandomSeed();
        passphrase=Bitcoin.getNewMnemonicPassphrase(seed);
        ewDerivation=new EWDerivation(seed);
        alias= Bitcoin.keyToStringAddress(ewDerivation.getAlias());
        identicon=IdenticonGenerator.generate(alias);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_create, container, false);
        final EditText etPassword = (EditText) v.findViewById(R.id.etPassword);
        final ImageView ivIdenticon = (ImageView) v.findViewById(R.id.ivIdenticon);

        ((Button)v.findViewById(R.id.btnRefresh)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // generate password & identicon
                newPassphrase();
                etPassword.setText(passphrase);
                ivIdenticon.setImageBitmap(identicon);
            }
        });
        ((Button)v.findViewById(R.id.btnConfirm)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // save password
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                final SharedPreferences.Editor edit = sharedPref.edit();
                edit.putString("passphrase", passphrase);
                edit.commit();
                Toast.makeText(getActivity(), "Passphrase saved", Toast.LENGTH_LONG).show();

                FragmentTransaction trans = getFragmentManager().beginTransaction();
                trans.replace(R.id.root_frame, new WalletFragment());
                trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                trans.addToBackStack(null);
                trans.commit();
            }
        });


        newPassphrase();
        etPassword.setText(passphrase);
        ivIdenticon.setImageBitmap(identicon);

        return v;

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

}
