package it.eternitywall.eternitywall.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.bitcoin.Bitcoin;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecoverPassphraseFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecoverPassphraseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecoverPassphraseFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public RecoverPassphraseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecoverPassphraseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecoverPassphraseFragment newInstance(String param1, String param2) {
        RecoverPassphraseFragment fragment = new RecoverPassphraseFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_recover_passphrase, container, false);
        final TextView passphraseText = (TextView) view.findViewById(R.id.passphraseText);
        final EditText pin            = (EditText) view.findViewById(R.id.pin);
        final EditText confirmPin     = (EditText) view.findViewById(R.id.confirmPin);

        final Button button           = (Button) view.findViewById(R.id.savePassphrase);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Editable pinText = pin.getText();
                final Editable confirmPinText = confirmPin.getText();
                final CharSequence passphraseCharSequence = passphraseText.getText();
                if( pinText.toString().isEmpty() || confirmPinText.toString().isEmpty() || passphraseCharSequence.toString().isEmpty() ) {
                    Toast.makeText(getActivity(), "All inputs are required", Toast.LENGTH_LONG).show();
                }

                if(  !pinText.toString().equals(confirmPinText.toString()) ) {
                    Toast.makeText(getActivity(), "PIN are not the same", Toast.LENGTH_LONG).show();
                    return;
                }

                final String passphrase = passphraseCharSequence.toString();
                final byte[] entropyFromPassphrase = Bitcoin.getEntropyFromPassphrase(passphrase);
                if (entropyFromPassphrase == null) {
                    Toast.makeText(getActivity(), "Passphrase invalid", Toast.LENGTH_LONG).show();

                    return;
                }
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                final SharedPreferences.Editor edit = sharedPref.edit();
                edit.putString("passphrase", passphrase);
                edit.commit();
                Toast.makeText(getActivity(), "Passphrase saved", Toast.LENGTH_LONG).show();
                passphraseText.setText("");
                launchService();

            }
        });
        return view;
    }

    private void launchService() {
        EWApplication ewApplication = (EWApplication) getActivity().getApplication();
        ewApplication.getEwWalletService().startSync();
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
        void onFragmentInteraction(Uri uri);
    }
}
