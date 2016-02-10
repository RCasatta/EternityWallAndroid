package it.eternitywall.eternitywall.components;


import android.view.View;

public class Debug {
    public String name;
    public String value;
    public View.OnClickListener onClickListener;
    public Debug(String name,String value,View.OnClickListener onClickListener){
        this.name=name;
        this.value=value;
        this.onClickListener=onClickListener;
    }
    public Debug(String name,String value){
        this.name=name;
        this.value=value;
        this.onClickListener=null;
    }

}