package it.eternitywall.eternitywall.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import it.eternitywall.eternitywall.DetailActivity;
import it.eternitywall.eternitywall.Message;
import it.eternitywall.eternitywall.R;

/**
 * Created by federicoserrelli on 26/08/15.
 */
public class MessageListAdapter extends ArrayAdapter<Message> {
    private final static Logger log      = Logger.getLogger(MessageListAdapter.class.getName());

    public interface MessageListAdapterManager {
        public void loadMoreData();
    }

    private int layoutResourceId = R.layout.item_message;
    private List<Message> data;
    private Activity activity;
    private MessageListAdapterManager manager;
    private Integer inQueue;

    public MessageListAdapter(Activity activity, int resource, List<Message> objects, Integer inQueue, MessageListAdapterManager manager) {
        super(activity, resource, objects);
        this.data = objects;
        this.activity = activity;
        this.manager = manager;
        this.inQueue = inQueue;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {
        final Message m = data.get(position);
        MessageHolder h;

        log.info("position=" + position);


        if(row == null) {
            row = activity.getLayoutInflater().inflate(layoutResourceId, parent, false);
            h = new MessageHolder();
            h.txtDate = (TextView) row.findViewById(R.id.txtDate);
            h.txtMessage = (TextView) row.findViewById(R.id.txtMessage);
            h.txtStatus = (TextView) row.findViewById(R.id.txtStatus);
            h.headerText = (TextView) row.findViewById(R.id.headerText);
            row.setTag(h);
        }
        else {
            h = (MessageHolder) row.getTag();
        }


        h.txtDate.setText(new SimpleDateFormat("dd MMM yyyy HH.mm").format(new Date(m.getTimestamp())));
        h.txtMessage.setText(m.getMessage());
        if (m.getRank()==1){
            h.txtMessage.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
        }else if (m.getRank()==2){
            h.txtMessage.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium );
        }else if (m.getRank()==3){
            h.txtMessage.setTextAppearance(getContext(), android.R.style.TextAppearance_Small );
        }
        h.txtMessage.setTextColor(getContext().getResources().getColor(android.R.color.black));
        h.txtStatus.setVisibility(View.GONE);
        h.txtStatus.setText("");

        // Add status field
        String strStatus="";
        if (m.getReplies()>0){
            strStatus+=String.valueOf(m.getReplies())+(m.getReplies()==1?" reply ": " replies ");
        }
        if (m.getAnswer()==true){
            if (strStatus.length()>0)
                strStatus+="- ";
            strStatus+="answer";
        }
        if (m.getLikes()>0){
            if (strStatus.length()>0)
                strStatus+="- ";
            strStatus+=String.valueOf(m.getLikes())+(m.getLikes()==1?" like ": " likes ");
        }

        if (strStatus.length()>0){
            h.txtStatus.setText(strStatus);
            h.txtStatus.setVisibility(View.VISIBLE);
        }
        // add click listener
        h.onClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.putExtra("hash",String.valueOf( m.getTxHash() ));
                getContext().startActivity(intent);
            }
        };
        row.setOnClickListener(h.onClickListener);



        if(position==0) {
            if(inQueue!=null && inQueue>0) {
                h.headerText.setVisibility(View.VISIBLE);
                String value = mapNumbers.get(inQueue);
                if(value==null)
                    value = inQueue.toString();

                h.headerText.setText(value + " message" + (inQueue > 1 ? "s" : "") + " in queue…");

            }
        } else {
            h.headerText.setVisibility( View.GONE );
        }

        if(position == data.size()-1 && manager!=null)
            manager.loadMoreData();
        return row;
    }

    protected class MessageHolder {
        protected TextView headerText;
        protected TextView txtDate;
        protected TextView txtMessage;
        protected TextView txtStatus;
        protected View.OnClickListener onClickListener;
    }

    static Map<Integer,String> mapNumbers=new HashMap<>();
    static {
        mapNumbers.put(1,"one");
        mapNumbers.put(2,"two");
        mapNumbers.put(3,"three");
        mapNumbers.put(4,"four");
        mapNumbers.put(5,"five");
        mapNumbers.put(6,"six");
        mapNumbers.put(7,"seven");
        mapNumbers.put(8,"eight");
        mapNumbers.put(9,"nine");
        mapNumbers.put(10,"ten");
    }



}
