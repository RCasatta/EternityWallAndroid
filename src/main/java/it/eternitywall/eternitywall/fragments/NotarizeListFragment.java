package it.eternitywall.eternitywall.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orm.SugarContext;
import com.orm.SugarDb;

import java.util.List;

import it.eternitywall.eternitywall.EWApplication;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.activity.NotarizeActivity;
import it.eternitywall.eternitywall.adapters.DocumentRecyclerViewAdapter;
import it.eternitywall.eternitywall.components.Document;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NotarizeListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NotarizeListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
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




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_notarize_list, container, false);
        // Show / Hide write button
        v.findViewById(R.id.fabNew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), NotarizeActivity.class);
                startActivity(i);
            }
        });

        // Set Fragment Views
        RecyclerView lstMessages = (RecyclerView) v.findViewById(R.id.lstMessages);

        // Set Recyclerview
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager( getActivity().getApplicationContext() );
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        lstMessages.setLayoutManager(mLayoutManager);

        // Init DB
        SugarContext.init(getActivity());
        // Put elements
        List<Document> documents = Document.find(Document.class, " 1=1");
        DocumentRecyclerViewAdapter documentListAdapter = new DocumentRecyclerViewAdapter(documents);
        lstMessages.setAdapter( documentListAdapter );

        return v;
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

}
