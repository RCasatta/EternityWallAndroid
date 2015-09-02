package it.eternitywall.eternitywall.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import it.eternitywall.eternitywall.Message;
import it.eternitywall.eternitywall.R;

/**
 * Created by federicoserrelli on 26/08/15.
 */
public class MessageListAdapter extends ArrayAdapter<Message> {

    public interface MessageListAdapterManager {
        public void loadMoreData();
    }

    private int layoutResourceId = R.layout.item_message;
    private List<Message> data;
    private Activity activity;
    private MessageListAdapterManager manager;

    public MessageListAdapter(Activity activity, int resource, List<Message> objects, MessageListAdapterManager manager) {
        super(activity, resource, objects);
        this.data = objects;
        this.activity = activity;
        this.manager = manager;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {
        Message m = data.get(position);
        MessageHolder h;

        if(row == null) {
            row = activity.getLayoutInflater().inflate(layoutResourceId, parent, false);
            h = new MessageHolder();
            h.txtDate = (TextView) row.findViewById(R.id.txtDate);
            h.txtMessage = (TextView) row.findViewById(R.id.txtMessage);
            row.setTag(h);
        }
        else {
            h = (MessageHolder) row.getTag();
        }


        h.txtDate.setText(new SimpleDateFormat("dd MMM yyyy HH.mm").format(new Date(m.getTimestamp())));
        h.txtMessage.setText(m.getMessage());

        if(position == data.size()-1)
            manager.loadMoreData();
        return row;
    }

    protected class MessageHolder {
        protected TextView txtDate;
        protected TextView txtMessage;
    }
}
