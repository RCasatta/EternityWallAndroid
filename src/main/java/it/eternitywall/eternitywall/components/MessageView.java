package it.eternitywall.eternitywall.components;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import it.eternitywall.eternitywall.IdenticonGenerator;
import it.eternitywall.eternitywall.Message;
import it.eternitywall.eternitywall.R;
import it.eternitywall.eternitywall.activity.DetailActivity;
import it.eternitywall.eternitywall.activity.ProfileActivity;

/**
 * Created by luca on 18/04/16.
 */
public class MessageView  extends LinearLayout {

    private View view;
    TextView txtMessage,txtStatus,txtDate,txtHeader;
    ImageView identicon;

    public MessageView(Context context) {
        super(context);
        init(context);
    }

    public MessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    private void init(Context context) {
        LayoutInflater mInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view=mInflater.inflate(R.layout.item_message, this);

        txtMessage = (TextView) view.findViewById(R.id.txtMessage);
        txtStatus = (TextView) view.findViewById(R.id.txtStatus);
        txtDate = (TextView) view.findViewById(R.id.txtDate);
        txtHeader = (TextView) view.findViewById(R.id.txtHeader);
        identicon = (ImageView) view.findViewById(R.id.identicon);
    }


    public void setTextMessage( int resource){
        txtMessage.setTextAppearance(getContext(), resource);

    }

    public void set(final Message m){
        // message
        String text = m.getMessage();
        if (m.getLink() != null) {
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
            text = text.replace(link, linkreplace);
        }
        txtMessage.setText(Html.fromHtml(text));
        if (m.getRank() == 1) {
            txtMessage.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
        } else if (m.getRank() == 2) {
            txtMessage.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
        } else if (m.getRank() == 3) {
            txtMessage.setTextAppearance(getContext(), android.R.style.TextAppearance_Small);
        }
        txtMessage.setTextColor(getContext().getResources().getColor(android.R.color.black));

        // status
        txtStatus.setVisibility(View.GONE);
        txtStatus.setText("");
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
            txtStatus.setText(strStatus);
            txtStatus.setVisibility(View.VISIBLE);
        }

        // date
        String dateFormatted = new SimpleDateFormat("dd MMM yyyy HH.mm").format(new Date(m.getTimestamp()));
        if (m.getAliasName() != null) {
            txtDate.setText(m.getAliasName() + " - " + dateFormatted);
        } else {
            txtDate.setText(dateFormatted);
        }

        //identicon
        if (m.getAlias() != null) {
            Bitmap bitmap = IdenticonGenerator.generate(m.getAlias());
            identicon.setImageBitmap(bitmap);
            identicon.setVisibility(View.VISIBLE);
            identicon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                    intent.putExtra("accountId", String.valueOf(m.getAlias()));
                    v.getContext().startActivity(intent);
                }
            });
        } else {
            identicon.setVisibility(View.GONE);
        }

        txtHeader.setVisibility(View.GONE);


        //onclick
        // add click listener

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), DetailActivity.class);
                intent.putExtra("hash", String.valueOf(m.getTxHash()));
                v.getContext().startActivity(intent);
            }
        });
    }

}