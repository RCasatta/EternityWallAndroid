package it.eternitywall.eternitywall.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import it.eternitywall.eternitywall.IdenticonGenerator;
import it.eternitywall.eternitywall.Message;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.activity.DetailActivity;
import it.eternitywall.eternitywall.activity.NotarizeDetailActivity;
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
        public TextView txtDate = null;
        public View.OnClickListener onClickListener;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            txtPath = (TextView) view.findViewById(R.id.txtPath);
            txtHash = (TextView) view.findViewById(R.id.txtHash);
            txtDate = (TextView) view.findViewById(R.id.txtDate);
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

        String name="";
        if (d.path.indexOf(':')>=0)
            name=d.path.substring(d.path.indexOf(':')+1);
        else
            name= d.path;

        h.txtHash.setText(d.hash);
        h.txtPath.setText(name);
        if (d.stamped_at>0) {
            h.txtDate.setText(getDate(d.stamped_at));
        }else {
            h.txtDate.setText(getDate(d.created_at));
        }

        //onclick add click listener
        h.onClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), NotarizeDetailActivity.class);
                intent.putExtra("id",d.getId());
                v.getContext().startActivity(intent);
            }
        };
        h.mView.setOnClickListener(h.onClickListener);

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd MMMM yyyy HH:mm", cal).toString();
        return date;
    }
}
