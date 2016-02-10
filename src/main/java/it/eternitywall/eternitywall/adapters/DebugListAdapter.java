package it.eternitywall.eternitywall.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.logging.Logger;

import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.components.Debug;

/**
 * Created by federicoserrelli on 26/08/15.
 */
public class DebugListAdapter extends ArrayAdapter<Debug> {
    private final static Logger log      = Logger.getLogger(DebugListAdapter.class.getName());

    public interface MessageListAdapterManager {
        public void loadMoreData();
    }

    private int layoutResourceId = R.layout.item_debug;
    private List<Debug> data;
    private Activity activity;
    private MessageListAdapterManager manager;

    public DebugListAdapter(Activity activity, int resource, List<Debug> objects, MessageListAdapterManager manager) {
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
        final Debug d = data.get(position);
        DebugHolder h;

        log.info("position=" + position);


        if(row == null) {
            row = activity.getLayoutInflater().inflate(layoutResourceId, parent, false);
            h = new DebugHolder();
            h.txtName = (TextView) row.findViewById(R.id.txtName);
            h.txtValue = (TextView) row.findViewById(R.id.txtValue);
            row.setTag(h);
        }
        else {
            h = (DebugHolder) row.getTag();
        }


        h.txtName.setText("");
        h.txtValue.setText("");
        if (d.name!=null)
            h.txtName.setText(d.name);
        if (d.value!=null)
            h.txtValue.setText(d.value);

        // add click listener
        if(d.onClickListener!=null) {
            row.setOnClickListener(d.onClickListener);
        }

        if(position == data.size()-1 && manager!=null)
            manager.loadMoreData();
        return row;
    }

    protected class DebugHolder {
        protected TextView txtName;
        protected TextView txtValue;
    }

}
