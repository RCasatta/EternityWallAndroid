package it.eternitywall.eternitywall;

import android.graphics.drawable.Drawable;

public class ApplicationDetail {

    private CharSequence label;
    private CharSequence name;
    private Drawable icon;

    public ApplicationDetail(CharSequence label, CharSequence name, Drawable icon) {
        this.label = label;
        this.name = name;
        this.icon = icon;
    }


    public CharSequence getLabel() {
        return label;
    }

    public CharSequence getName() {
        return name;
    }

    public Drawable getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return "ApplicationDetail{" +
                "icon=" + icon +
                ", label=" + label +
                ", name=" + name +
                '}';
    }
}