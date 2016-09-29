package it.eternitywall.eternitywall.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
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
        public ImageView imageView = null;
        public View.OnClickListener onClickListener;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            txtPath = (TextView) view.findViewById(R.id.txtPath);
            txtHash = (TextView) view.findViewById(R.id.txtHash);
            txtDate = (TextView) view.findViewById(R.id.txtDate);
            imageView = (ImageView) view.findViewById(R.id.identicon);
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

        // Set name label of the filename of the document
        String name="";

        try {
            Uri uri = Uri.parse(d.path);
            name=uri.getLastPathSegment();
            h.txtPath.setText(name);
        }catch (Exception e ) {
            e.printStackTrace();
            Log.d(getClass().toString(),e.getLocalizedMessage());
        }

        // Set the imageview retrieved from the document
        try {
            Uri uri = Uri.parse(d.path);
            Bitmap bitmap = decodeImageFile(uri,200,200,h.mView.getContext());
            //InputStream is = h.mView.getContext().getContentResolver().openInputStream(uri);
            //Bitmap bitmap = BitmapFactory.decodeStream(is);
            h.imageView.setImageBitmap(bitmap);
        }catch (Exception e ) {
            e.printStackTrace();
            Log.d(getClass().toString(),e.getLocalizedMessage());
        }

        // Set hash and date
        h.txtHash.setText(d.hash);
        if (d.stamped_at>0) {
            h.txtDate.setText(getDate(d.stamped_at));
        }else {
            h.txtDate.setText(getDate(d.created_at));
        }

        // Set onclick add click listener
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

    public Bitmap decodeImageFile(Uri uri, int WIDTH, int HIGHT,Context context){
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            InputStream is = context.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(is,null,o);

            //The new size we want to scale to
            final int REQUIRED_WIDTH=WIDTH;
            final int REQUIRED_HIGHT=HIGHT;
            //Find the correct scale value. It should be the power of 2.
            int scale=1;
            while(o.outWidth/scale/2>=REQUIRED_WIDTH && o.outHeight/scale/2>=REQUIRED_HIGHT)
                scale*=2;

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            InputStream is1 = context.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(is1,null,o2);

        } catch (FileNotFoundException e) {
            Log.d( "decode" ,e.getLocalizedMessage().toString());
        }
        return null;
    }
}
