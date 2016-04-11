/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.eternitywall.eternitywall.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import it.eternitywall.eternitywall.IdenticonGenerator;
import it.eternitywall.eternitywall.Message;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.activity.DetailActivity;
import it.eternitywall.eternitywall.activity.ProfileActivity;
import it.eternitywall.eternitywall.components.EnglishNumberToWords;

public class MessageRecyclerViewAdapter
        extends RecyclerView.Adapter<MessageRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = MessageRecyclerViewAdapter.class.toString();

    private List<Message> mValues;
    private MessageListAdapterManager manager;
    private Integer inQueue;
    private LruCache<String, Bitmap> bitmapCache;

    public void setInQueue(Integer inQueue) {
        this.inQueue = inQueue;
    }

    public interface MessageListAdapterManager {
        public void loadMoreData();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public TextView txtDate = null;
        public TextView txtMessage = null;
        public TextView txtStatus = null;
        public TextView txtHeader = null;
        public ImageView identicon = null;
        public View.OnClickListener onClickListener;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            txtDate = (TextView) view.findViewById(R.id.txtDate);
            txtMessage = (TextView) view.findViewById(R.id.txtMessage);
            txtStatus = (TextView) view.findViewById(R.id.txtStatus);
            txtHeader = (TextView) view.findViewById(R.id.txtHeader);
            identicon = (ImageView) view.findViewById(R.id.identicon);
        }

    }

    public MessageRecyclerViewAdapter(List<Message> items,  Integer inQueue, MessageListAdapterManager manager, LruCache<String, Bitmap> bitmapCache) {
        mValues = items;
        this.manager = manager;
        this.inQueue=inQueue;
        this.bitmapCache=bitmapCache;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder h, int position) {

        final Message m = mValues.get(position);

        // message
        String text=m.getMessage();
        if(m.getLink()!=null) {
            String link = m.getLink();
            String linkreplace = "";

            if (link.startsWith("@"))
                linkreplace = "<a href='http://twitter.com/" + link + "'>" + link + "</a>";
            else if (link.contains("http"))
                linkreplace = "<a href='" + link + "'>" + link + "</a>";
            else if (link.contains("https"))
                linkreplace = "<a href='" + link + "'>" + link + "</a>";
            else
                linkreplace = "<a href='http://" + link + "'>" + link + "</a>";
            text=text.replace(link,linkreplace);
        }
        h.txtMessage.setText(Html.fromHtml(text));
        if (m.getRank() == 1) {
            h.txtMessage.setTextAppearance(h.mView.getContext(), android.R.style.TextAppearance_Large);
        } else if (m.getRank() == 2) {
            h.txtMessage.setTextAppearance(h.mView.getContext(), android.R.style.TextAppearance_Medium);
        } else if (m.getRank() == 3) {
            h.txtMessage.setTextAppearance(h.mView.getContext(), android.R.style.TextAppearance_Small);
        }
        h.txtMessage.setTextColor(h.mView.getContext().getResources().getColor(android.R.color.black));

        // status
        h.txtStatus.setVisibility(View.GONE);
        h.txtStatus.setText("");
        String strStatus = "";
        if (m.getReplies() > 0) {
            strStatus += String.valueOf(m.getReplies()) + (m.getReplies() == 1 ? " reply " : " replies ");
        }
        if (m.getAnswer() == true) {
            if (strStatus.length() > 0)
                strStatus += "- ";
            strStatus += "answer";
        }
        if (m.getLikes() > 0) {
            if (strStatus.length() > 0)
                strStatus += "- ";
            strStatus += String.valueOf(m.getLikes()) + (m.getLikes() == 1 ? " like " : " likes ");
        }
        if (strStatus.length() > 0) {
            h.txtStatus.setText(strStatus);
            h.txtStatus.setVisibility(View.VISIBLE);
        }

        // date
        String dateFormatted = new SimpleDateFormat("dd MMM yyyy HH.mm").format(new Date(m.getTimestamp()));
        if (m.getAliasName() != null) {
            h.txtDate.setText(m.getAliasName() + " - " + dateFormatted);
        } else {
            h.txtDate.setText(dateFormatted);
        }

        //identicon
        if (m.getAlias() != null) {
            Bitmap bitmap;
            if(bitmapCache!=null) {
                bitmap = bitmapCache.get(m.getAlias());
                Log.i(TAG,"bitmapFromCache " + bitmap + " bitmapCache size " + bitmapCache.size() + " of " + bitmapCache.maxSize() );
                if (bitmap == null) {
                    bitmap = IdenticonGenerator.generate(m.getAlias());
                    bitmapCache.put(m.getAlias(), bitmap);
                }
            } else {
                Log.i(TAG,"bitmapCache is null" );

                bitmap = IdenticonGenerator.generate(m.getAlias());
            }
            h.identicon.setImageBitmap(bitmap);
            h.identicon.setVisibility(View.VISIBLE);
            h.identicon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                    intent.putExtra("accountId",String.valueOf(m.getAlias()));
                    v.getContext().startActivity(intent);
                }
            });
        } else {
            h.identicon.setVisibility(View.GONE);
        }

        if(position==0) {
            if(inQueue!=null && inQueue>0) {
                h.txtHeader.setVisibility(View.VISIBLE);
                h.txtHeader.setText(EnglishNumberToWords.convert(inQueue) + " message" + (inQueue > 1 ? "s" : "") + " in queue…");
            }
        } else {
            h.txtHeader.setVisibility( View.GONE );
        }

        //onclick
        // add click listener
        h.onClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), DetailActivity.class);
                intent.putExtra("hash",String.valueOf(m.getTxHash()));
                v.getContext().startActivity(intent);
            }
        };
        h.mView.setOnClickListener(h.onClickListener);

        if(position == mValues.size()-1 && manager!=null)
            manager.loadMoreData();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }


    public void removeAt(int position) {
        mValues.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mValues.size());
    }

    public void add(Message message, int position) {
        mValues.add(position, message);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, mValues.size());
    }

    public void addAll(List<Message> messages) {
        mValues.addAll(messages);
        notifyDataSetChanged();
    }
    public void clear() {
        mValues.clear();
        notifyDataSetChanged();
    }

}
