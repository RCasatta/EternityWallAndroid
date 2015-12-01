package it.eternitywall.eternitywall.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.MoreObjects;

import java.util.HashMap;
import java.util.Map;

import it.eternitywall.eternitywall.R;

/**
 * Created by casatta on 30/01/15.
 */
public class CurrencyView extends LinearLayout {
    private static final String TAG = "CurrencyView";
    private String  units;  /* apparently type long not supported as attribute */
    private String  type;
    private Integer textSizeSp;
    private Boolean onlySymbol;
    private TextView decimalPart    ;
    private TextView integerPart    ;
    private TextView separator      ;
    private TextView unitView       ;
    private TextView currencySymbol ;


    public void init(Context context, AttributeSet attrs ) {
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_bitcoin, this);
        decimalPart    = (TextView) findViewById(R.id.bitcoinDecimalPart);
        integerPart    = (TextView) findViewById(R.id.bitcoinIntegerPart);
        separator      = (TextView) findViewById(R.id.bitcoinSeparator);
        unitView       = (TextView) findViewById(R.id.bitcoinUnit);
        currencySymbol = (TextView) findViewById(R.id.currencySymbol);

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.CurrencyView);

        textSizeSp = arr.getInteger(R.styleable.CurrencyView_textSizeSp, 14);
        units      = MoreObjects.firstNonNull(arr.getString(R.styleable.CurrencyView_units), "0") ;
        type       = MoreObjects.firstNonNull(arr.getString(R.styleable.CurrencyView_type), "BTC") ;
        onlySymbol = arr.getBoolean(R.styleable.CurrencyView_onlySymbol, false);
        arr.recycle();  // Do this when done.

        Log.d(TAG,"textSizeSp=" + textSizeSp + " units="   + units + " type="    + type);

        refreshUI();
    }

    public void refreshUI() {
        Long unitsLong = Long.parseLong(units);
        if(unitsLong<0) {
            setVisibility(View.INVISIBLE);
        } else {
            setVisibility(View.VISIBLE);
            decimalPart.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp * 0.8f);
            integerPart.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
            separator.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
            unitView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
            currencySymbol.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);

            if ("BTC".equals(type)) { //view_bitcoin
                integerPart.setText(String.valueOf(unitsLong / 100000000));
                decimalPart.setText(calcDecimal(unitsLong,8) );
                unitView.setText("");
                currencySymbol.setText(Html.fromHtml("&#xf15a;"));
            } else if ("mBTC".equals(type)) { //milli view_bitcoin
                integerPart.setText(String.valueOf(unitsLong / 100000));
                decimalPart.setText(calcDecimal(unitsLong, 5) );
                unitView.setText("m");
            } else if ("uBTC".equals(type)) { //micro view_bitcoin
                integerPart.setText(String.valueOf(unitsLong / 100));
                decimalPart.setText(calcDecimal(unitsLong,2) ) ;
                unitView.setText(Html.fromHtml("&micro;"));
            } else if ("SAT".equals(type)) { //satoshis
                integerPart.setText(unitsLong.toString());
                separator.setVisibility(View.GONE);
                decimalPart.setVisibility(View.GONE);
                unitView.setText("SAT");
                currencySymbol.setVisibility(View.GONE);
            } else if ( map.get(type)!=null || map2.get(type)!=null ) {
                integerPart.setText(String.valueOf(unitsLong / 100));
                decimalPart.setText(calcDecimal(unitsLong, 2) );
                if( map2.get(type)!=null)
                    unitView.setText(map2.get(type));
                else
                    unitView.setText("");
                if(  map.get(type)!=null)
                    currencySymbol.setText(Html.fromHtml(map.get(type)));
                else
                    currencySymbol.setText("");
            } else {
                Log.w(TAG,"currency type not supported");
            }
        }

        if(onlySymbol) {
            decimalPart.setVisibility(View.GONE);
            integerPart.setVisibility(View.GONE);
            separator.setVisibility(View.GONE);
        } else {
            decimalPart.setVisibility(View.VISIBLE);
            integerPart.setVisibility(View.VISIBLE);
            separator.setVisibility(View.VISIBLE);
        }
    }

    private String calcDecimal(Long unitsLong, int position) {
        final String val="0000000000000000000000" + unitsLong;
        final int length=val.length();
        final String val2 = val.substring(length-position);
        return val2.substring(0,2);

    }


    private static final Map<String, String> map = new HashMap<>();
    static {
        map.put("USD", "&#xf155;");
        map.put("EUR", "&#xf153;");
        map.put("CNY", "&#xf157;");
        map.put("GBP", "&#xf154;");
        map.put("ILS", "&#xf20b;");
        map.put("AUD", "&#xf155;");
        map.put("CAD", "&#xf155;");
        map.put("INR", "&#xf156;");
        map.put("KRW", "&#xf159;");
        map.put("SGD", "&#xf155;");
        map.put("RUB", "&#xf158;");
        map.put("TRY", "&#xf195;");
        map.put("RUB", "&#xf158;");
        map.put("BRL", "&#xf155;");
        map.put("JPY", "&#xf157;");
    }

    private static final Map<String, String> map2 = new HashMap<>();
    static {
        map2.put("BRL", "R");
        map2.put("CHF", "CHF");
    }

    public CurrencyView(Context context) {
        super(context);
    }

    public CurrencyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public CurrencyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    @TargetApi(21)
    public CurrencyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context,attrs);
    }

    public void setUnits(String units) { this.units=units; }
    public String getUnits() { return units; }

    public void setType(String type) { this.type=type;  }
    public String getType() { return type; }

    public void setTextSizeSp(Integer textSizeSp) { this.textSizeSp = textSizeSp; }
    public Integer getTextSizeSp() { return textSizeSp; }

    public Boolean isOnlySymbol() {
        return onlySymbol;
    }
    public void setOnlySymbol(Boolean onlySymbol) {
        this.onlySymbol = onlySymbol;
    }
}
