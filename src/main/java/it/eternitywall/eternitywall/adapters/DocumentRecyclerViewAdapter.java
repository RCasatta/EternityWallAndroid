package it.eternitywall.eternitywall.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import it.eternitywall.eternitywall.IdenticonGenerator;
import it.eternitywall.eternitywall.Message;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.activity.DetailActivity;
import it.eternitywall.eternitywall.activity.ProfileActivity;
import it.eternitywall.eternitywall.components.Debug;
import it.eternitywall.eternitywall.components.Document;
import it.eternitywall.eternitywall.components.EnglishNumberToWords;

/**
 * Created by federicoserrelli on 26/08/15.
 */


public class DocumentRecyclerViewAdapter
        extends RecyclerView.Adapter<DocumentRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = DocumentRecyclerViewAdapter.class.toString();

    private List<Document> mValues;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public TextView txtPath = null;
        public TextView txtHash = null;
        public View.OnClickListener onClickListener;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            txtPath = (TextView) view.findViewById(R.id.txtPath);
            txtHash = (TextView) view.findViewById(R.id.txtHash);
        }

    }

    public DocumentRecyclerViewAdapter(List<Document> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_document, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DocumentRecyclerViewAdapter.ViewHolder h, int position) {
        final Document d = mValues.get(position);

        h.txtHash.setText(d.hash);
        h.txtPath.setText(d.path);

        //onclick add click listener
        h.onClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(v.getContext(), DetailActivity.class);
                intent.putExtra("hash",String.valueOf(m.getTxHash()));
                v.getContext().startActivity(intent);*/
            }
        };
        h.mView.setOnClickListener(h.onClickListener);

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

}
