package com.vortex663.mt5tradecalculator;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.content.Intent;
import android.net.Uri;
import android.widget.*;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    EditText entry, sl, balance, risk, point, tickSize, tickValue, volumeStep, symbol;
    RadioGroup side;
    MaterialButton buyButton, sellButton;
    Spinner instrumentType;
    TextView output;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(20,18,24));
        getWindow().setNavigationBarColor(Color.rgb(20,18,24));
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(22), dp(20), dp(28));
        root.setBackgroundColor(Color.rgb(20,18,24));
        scroll.addView(root);
        setContentView(scroll);

        TextView title = new TextView(this);
        title.setText("MT5 Trade Calculator");
        title.setTextColor(Color.rgb(240,225,255)); title.setTextSize(30); title.setTypeface(null, 1);
        root.addView(title);
        TextView subtitle = new TextView(this);
        subtitle.setText("Offline calculator • Fixed risk : reward 1 : 3");
        subtitle.setTextColor(Color.rgb(205,190,220)); subtitle.setTextSize(14);
        subtitle.setPadding(0, dp(5), 0, dp(18)); root.addView(subtitle);

        addLabel(root, "Instrument type");
        instrumentType = new Spinner(this);
        String[] types = {"Forex", "Metals", "Indices", "Crypto", "Energies", "Stocks", "Futures", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        instrumentType.setAdapter(adapter); root.addView(instrumentType, params(-1,dp(48),0,0,0,2));
        symbol = addField(root, "Exact broker symbol", "e.g. XAUUSD, EURUSD, US30.cash");

        MaterialButton search = new MaterialButton(this);
        search.setText("SEARCH WEB FOR SYMBOL SPECS"); search.setCornerRadius(dp(24)); search.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(75,55,105)));
        root.addView(search, params(-1,-2,0,8,0,10));
        search.setOnClickListener(v -> searchSpecs());

        addLabel(root, "Trade direction");
        side = new RadioGroup(this); side.setOrientation(RadioGroup.HORIZONTAL);
        side.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        buyButton = new MaterialButton(this);
        buyButton.setText("BUY");
        buyButton.setTextSize(18);
        buyButton.setTypeface(null, 1);
        buyButton.setTextColor(Color.WHITE);
        buyButton.setCornerRadius(dp(22));
        buyButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(35,145,85)));
        sellButton = new MaterialButton(this);
        sellButton.setText("SELL");
        sellButton.setTextSize(18);
        sellButton.setTypeface(null, 1);
        sellButton.setTextColor(Color.WHITE);
        sellButton.setCornerRadius(dp(22));
        sellButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(190,55,65)));
        RadioGroup.LayoutParams tradeButtonParams = new RadioGroup.LayoutParams(0, dp(64), 1f);
        tradeButtonParams.setMargins(0, dp(4), dp(8), dp(8));
        side.addView(buyButton, tradeButtonParams);
        RadioGroup.LayoutParams sellParams = new RadioGroup.LayoutParams(0, dp(64), 1f);
        sellParams.setMargins(dp(8), dp(4), 0, dp(8));
        side.addView(sellButton, sellParams);
        side.check(0);
        buyButton.setOnClickListener(v -> selectTradeSide(true));
        sellButton.setOnClickListener(v -> selectTradeSide(false));
        root.addView(side, params(-1, dp(76), 0, 0, 0, 4));
        selectTradeSide(true);

        entry = addField(root, "Entry price", "e.g. 2350.50");
        sl = addField(root, "Stop loss (MT5 points)", "e.g. 500");
        balance = addField(root, "Account balance", "e.g. 1000");
        risk = addField(root, "Risk (%)", "e.g. 1");

        addLabel(root, "Symbol specifications (verify against MT5)");
        point = addField(root, "Point size", "e.g. 0.01");
        tickSize = addField(root, "Tick size", "e.g. 0.01");
        tickValue = addField(root, "Tick value per 1.0 lot (account currency)", "e.g. 1.00");
        volumeStep = addField(root, "Volume step", "e.g. 0.01");

        MaterialButton calc = new MaterialButton(this); calc.setText("CALCULATE"); calc.setCornerRadius(dp(28)); calc.setTextSize(16); calc.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        root.addView(calc, params(-1,-2,0,8,0,14));
        output = new TextView(this); output.setTextColor(Color.WHITE); output.setTextSize(16);
        output.setPadding(dp(18),dp(20),dp(18),dp(20)); output.setBackgroundColor(Color.rgb(48,40,58)); output.setTextColor(Color.rgb(245,235,255));
        root.addView(output, params(-1,-2,0,12,0,0));
        calc.setOnClickListener(v -> calculate());

        TextView note = new TextView(this);
        note.setText("Web search opens results in your browser; it does not automatically import values. Tick value, tick size and volume rules are broker- and account-specific. Confirm the exact symbol in MT5 before relying on a calculation.");
        note.setTextColor(Color.rgb(190,180,200)); note.setTextSize(12); note.setPadding(0,dp(16),0,0); root.addView(note);
    }

    void selectTradeSide(boolean isBuy) {
        side.check(isBuy ? 0 : 1);
        buyButton.setAlpha(isBuy ? 1.0f : 0.55f);
        sellButton.setAlpha(isBuy ? 0.55f : 1.0f);
        buyButton.setStrokeWidth(isBuy ? dp(3) : 0);
        sellButton.setStrokeWidth(isBuy ? 0 : dp(3));
        buyButton.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.rgb(170,245,195)));
        sellButton.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.rgb(255,180,180)));
    }

    void searchSpecs() {
        String sym = symbol.getText().toString().trim();
        if (sym.isEmpty()) { symbol.setError("Enter the exact symbol first"); return; }
        String type = instrumentType.getSelectedItem().toString();
        String query = sym + " " + type
            + " MT5 symbol specification tick value tick size contract size volume step";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)));
        startActivity(intent);
    }

    int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density); }
    LinearLayout.LayoutParams params(int w,int h,int l,int t,int r,int b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w,h);
        p.setMargins(l==0?0:dp(l),dp(t),r==0?0:dp(r),dp(b)); return p;
    }
    void addLabel(LinearLayout root, String text) {
        TextView tv = new TextView(this); tv.setText(text); tv.setTextColor(Color.rgb(220,190,255));
        tv.setTextSize(14); tv.setTypeface(null,1); tv.setPadding(0,dp(12),0,dp(5)); root.addView(tv);
    }
    EditText addField(LinearLayout root, String label, String hint) {
        addLabel(root,label);
        EditText e = new EditText(this); e.setSingleLine(true); e.setTextColor(Color.WHITE);
        e.setHintTextColor(Color.rgb(175,165,185)); e.setHint(hint); e.setTextSize(16);
        e.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        if (label.equals("Entry price") || label.contains("points") || label.contains("balance") || label.equals("Risk (%)") || label.contains("size") || label.contains("value") || label.contains("step")) {
            e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        }
        e.setPadding(dp(14),dp(10),dp(14),dp(10)); e.setBackgroundColor(Color.rgb(42,36,50));
        root.addView(e, params(-1,dp(50),0,0,0,2)); return e;
    }
    double val(EditText e) { String s=e.getText().toString().trim(); if(s.isEmpty()) throw new IllegalArgumentException("Fill in every field."); return Double.parseDouble(s); }

    void calculate() {
        try {
            double ep=val(entry), stop=val(sl), bal=val(balance), rp=val(risk);
            double ps=val(point), ts=val(tickSize), tv=val(tickValue), step=val(volumeStep);
            if(stop<=0||bal<=0||rp<=0||rp>100||ps<=0||ts<=0||tv<=0||step<=0) throw new IllegalArgumentException("Values must be positive; risk must be 100% or less.");
            boolean isBuy = side.getCheckedRadioButtonId() == 0;
            double tpPoints=stop*3.0, distance=stop*ps;
            double slPrice=isBuy?ep-distance:ep+distance;
            double tpPrice=isBuy?ep+tpPoints*ps:ep-tpPoints*ps;
            double riskMoney=bal*rp/100.0;
            double lossPerLot=(distance/ts)*tv;
            double lots=Math.floor((riskMoney/lossPerLot+1e-10)/step)*step;
            double actualRisk=lots*lossPerLot;
            if(lots<=0) throw new IllegalArgumentException("Calculated lot size is below the broker's minimum volume step.");
            output.setText(String.format(Locale.US,
                "INSTRUMENT: %s • %s\nTAKE PROFIT: %.0f points (1:3)\n\nStop loss price: %s\nTake profit price: %s\n\nSuggested lot size: %.4f\nRisk budget: %.2f\nEstimated risk at rounded lot: %.2f\nPotential reward at 1:3: %.2f",
                instrumentType.getSelectedItem(), symbol.getText().toString().trim(), tpPoints, fmt(slPrice), fmt(tpPrice), lots, riskMoney, actualRisk, actualRisk*3.0));
        } catch(Exception ex) {
            output.setText("Please check your inputs. " + (ex.getMessage()==null?"Invalid number.":ex.getMessage()));
        }
    }
    String fmt(double n) { return String.format(Locale.US,"%.8f",n).replaceAll("0+$","").replaceAll("\\.$",""); }
}