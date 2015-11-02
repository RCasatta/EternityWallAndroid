package it.eternitywall.eternitywall;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Riccardo Casatta @RCasatta on 21/06/15.
 */


public class Message implements Serializable, Comparable<Message> {


    private String txHash;
    private String message;
    private Long timestamp;
    private String messageId;
    private Integer replies;
    private Integer likes;
    private Boolean answer;


    public Boolean getAnswer() {
        return answer;
    }

    public void setAnswer(Boolean answer) {
        this.answer = answer;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Integer getReplies() {
        return replies;
    }

    public void setReplies(Integer replies) {
        this.replies = replies;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    @Override
    public String toString() {
        return "Message{" +
                ", txHash='" + txHash + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    /*
            "message":"Easy to use soft to browse your data on blockchain ... soon !",
            "timestamp":"1440565502",
            "time":"26 agosto 2015 5.05 GMT",
            "cursor":null,
            "height":371549,
            "hash":"9f601f7ff281598b61ebc7247fee5fe4b4eed8ca3af32a0f30286d7f992a89af",
            "replies":null,
            "answer":false
    */

    public static Message buildFromJson(JSONObject jo) throws Exception {

        Message m = new Message();

        try { m.message = StringEscapeUtils.unescapeHtml(jo.getString("message"));} catch (JSONException e) { throw new Exception("Missing mandatory field <message>");} //mandatory
        try { m.timestamp = jo.getLong("timestamp")*1000;} catch (JSONException e) { throw new Exception("Missing mandatory field <timestamp>");} //mandatory

        try { m.messageId = jo.getString("messageId");} catch (JSONException e) {} //optional
        try { m.txHash = jo.getString("hash");} catch (JSONException e) {} //optional
        try { m.replies = jo.getInt("replies");} catch (JSONException e) { m.replies = 0; } //optional
        try { m.answer = jo.getBoolean("answer");} catch (JSONException e) { m.answer = false; } //optional
        try { m.likes = jo.getInt("likes");} catch (JSONException e) { m.likes = 0; } //optional

        return m;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof Message && ((Message) o).txHash.equals(this.txHash);
    }

    @Override
    public int compareTo(Message another) {
        return -this.timestamp.compareTo(another.getTimestamp());
    }
}
