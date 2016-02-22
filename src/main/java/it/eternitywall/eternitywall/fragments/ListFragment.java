package it.eternitywall.eternitywall.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.eternitywall.eternitywall.Http;
import it.eternitywall.eternitywall.Message;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.activity.WriteActivity;
import it.eternitywall.eternitywall.adapters.MessageRecyclerViewAdapter;
import it.eternitywall.eternitywall.components.EnglishNumberToWords;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListFragment extends Fragment implements MessageRecyclerViewAdapter.MessageListAdapterManager {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = ListFragment.class.toString();

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
     * @return A new instance of fragment ListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListFragment newInstance(String param1, String param2) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ListFragment() {
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


    private RecyclerView lstMessages;
    private ProgressBar progress;
    private SwipeRefreshLayout swipe;
    //private TextView txtHeader;
    private MessageRecyclerViewAdapter messageRecyclerViewAdapter;

    private String search;
    private String sortby;
    private String cursor;
    private List<Message> messages;
    private Integer inQueue;

    public void setSearch(String search) {
        this.search = search;
    }
    public String getSearch() {
        return this.search;
    }
    public void setSortby(String sortby) {
        this.sortby = sortby;
    }
    public String getSortby() {
        return this.sortby;
    }
    public void clear() {
        messages.clear();
        cursor = null;
        search = null;
        sortby = null;
        inQueue = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_list, container, false);

        // Set Fragment Views
        lstMessages = (RecyclerView) v.findViewById(R.id.lstMessages);
        progress = (ProgressBar) v.findViewById(R.id.progress);
        swipe = (SwipeRefreshLayout) v.findViewById(R.id.activity_main_swipe_refresh_layout);
        //txtHeader = (TextView)v.findViewById(R.id.txtHeader);

        // Set Recyclerview
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager( getActivity().getApplicationContext() );
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        lstMessages.setLayoutManager(mLayoutManager);
        /*lstMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int previousTotal = 0;
            private boolean loading = true;
            private int visibleThreshold = 1;
            int firstVisibleItem, visibleItemCount, totalItemCount;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount != previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                    // End has been reached
                    Log.i("Yaeye!", "end called");
                    loading = true;
                    loadMoreData();
                }

            }
        });*/

        // Set empty variables and messages
        messages = new ArrayList<Message>();
        clear();

        // Set Message RecyclerView Adapter
        messageRecyclerViewAdapter = new MessageRecyclerViewAdapter(messages, inQueue,ListFragment.this);
        lstMessages.setAdapter( messageRecyclerViewAdapter );

        // load messages on Swipe
        loadMoreData();

        // Set Swipe on refresh scroll-upper event
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                messages = new ArrayList<Message>();
                cursor = null;
                //search = null;
                //sortby = null;
                inQueue = null;
                loadMoreData();
            }
        });

        // Show / Hide write button
        v.findViewById(R.id.payButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), WriteActivity.class);
                startActivity(i);
            }
        });

        return v;
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
                //txtHeader.setVisibility(View.GONE);
                // don't refresh if there are messages on sortby or search mode
                if ((sortby!=null || search!=null) && (messages!=null && !messages.isEmpty()))
                    progress.setVisibility(View.INVISIBLE);//nothing
                else if(!swipe.isRefreshing())
                    progress.setVisibility(View.VISIBLE);

            }

            @Override
            protected void onPostExecute(Object o) {
                if (getActivity()!=null && getActivity().isFinishing())  //exception will null pointer happened here, checking getActivity is null or use isAdded()????
                    return;
                super.onPostExecute(o);
                progress.setVisibility(View.INVISIBLE);
                swipe.setRefreshing(false);
/*
                if (messages.size()==0 && mMessages.size()==0) {
                    txtHeader.setText(getResources().getString(R.string.no_msg_found));
                    txtHeader.setVisibility(View.VISIBLE);
                }else if (inQueue==null){
                    txtHeader.setVisibility(View.GONE);
                }else if (inQueue>0){
                    txtHeader.setText(EnglishNumberToWords.convert(inQueue) + " message" + (inQueue > 1 ? "s" : "") + " in queue…");
                    txtHeader.setVisibility(View.VISIBLE);
                }else{
                    txtHeader.setVisibility(View.GONE);
                }
*/
                messageRecyclerViewAdapter.setInQueue(inQueue);
                if(ok) {

                    if(messages != null && !messages.isEmpty()) {
                        messages.addAll(mMessages);
                        final MessageRecyclerViewAdapter messageListAdapter = (MessageRecyclerViewAdapter) lstMessages.getAdapter();
                        messageListAdapter.notifyDataSetChanged();
                    }
                    else {
                        messages.addAll(mMessages);
                        lstMessages.setAdapter(new MessageRecyclerViewAdapter(messages, inQueue, ListFragment.this));
                    }
                }
                else {
                    //succhia!
                    if(isAdded()) {
                        if (statusMessage != null)
                            Toast.makeText(getActivity(), statusMessage, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity(), getString(R.string.err_check_internet), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            protected Object doInBackground(Object[] params) {
                Optional<String> json=null;
                if (search!=null) {
                    // search api return all the values without cursor
                    if(messages==null || messages.isEmpty())
                        json = cursor == null ? Http.get("http://eternitywall.it/search?format=json&q=" + search) : Http.get("http://eternitywall.it/?format=json&cursor=" + cursor + "&q=" + search);
                    else
                        ok = true;
                }else if (sortby!=null){
                    // sortby api return all the values without cursor
                    if(messages==null || messages.isEmpty())
                        json = Http.get("http://eternitywall.it/sortby/"+sortby+"?format=json");
                    else
                        ok = true;
                } else
                    json = cursor == null ? Http.get("http://eternitywall.it/?format=json") : Http.get("http://eternitywall.it/?format=json&cursor=" + cursor);


                if(json!=null && json.isPresent()) {
                    try {
                        String jstring = json.get();
                        JSONObject jo = new JSONObject(jstring);

                        try {
                            String status = jo.getString("status");
                            if (status.equals("ko")) {
                                statusMessage= jo.getString("statusMessage");
                                return null;
                            }
                        } catch (Exception ex) {
                            Log.i(TAG,"no value for status in json " + ex.getMessage() );
                        }

                        try {
                            cursor = jo.getString("next");
                            if (jo.has("messagesInQueue")) {
                                inQueue = jo.getInt("messagesInQueue");
                            }
                        } catch (Exception ex){
                            cursor = null;
                            Log.i(TAG, "no value for next in json " + ex.getMessage());
                        }

                        JSONArray ja = jo.getJSONArray("messages");

                        for(int m=0; m<ja.length(); m++) {
                            Message message = Message.buildFromJson(ja.getJSONObject(m));
                            mMessages.add(message);
                            Log.i(TAG, message.toString());
                        }

                        //sort by reverse timestamp only on main messages without parsing
                        if (search==null && sortby == null)
                            Collections.sort(mMessages);
                        ok = true;
                    } catch (Exception ex) {
                        Log.e(TAG,"Exception " + ex.getMessage());
                    }
                }
                return null;
            }
        };
        t.execute();
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
