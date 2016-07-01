package it.eternitywall.eternitywall.components;

import com.orm.SugarRecord;

public class Document extends SugarRecord {

    public String path="";
    public String hash="";
    public Long created_at= Long.valueOf(0);
    public Long stamped_at= Long.valueOf(0);
    public String stamp="";
}
