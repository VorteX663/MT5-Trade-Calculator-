package com.vortex663.mt5tradecalculator;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.Locale;

public class MainActivity extends Activity {
    EditText entry, sl, balance, risk, point, tickSize, tickValue, volumeStep;
    RadioGroup side;
    TextView output;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(15,18,25));
        getWindow().setNavigationBarColor(Color.rgb(15,18,25));
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(22), dp(20), dp(28));
        root.setBackgroundColor(Color.rgb(15,18,25));
        scroll.addView(root);
        setContentView(scroll);

        TextView title = new TextView(this);
        title.setText("MT5 Trade Calculator");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        title.setTypeface(null, 1);
        root.addView(title);
        TextView subtitle = new TextView(this);
        subtitle.setText("Offline • Fixed risk : reward 1 : 3");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(13);
        subtitle.setPadding(0, dp(5), 0, dp(18));
        root.addView(subtitle);

        addLabel(root, "Trade direction");
        side = new RadioGroup(this);
        side.setOrientation(RadioGroup.HORIZONTAL);
        RadioButton buy = new RadioButton(this); buy.setText("BUY"); buy.setTextColor(Color.WHITE); buy.setId(View.generateViewId());
        RadioButton sell = new RadioButton(this); sell.setText("SELL"); sell.setTextColor(Color.WHITE); sell.setId(View.generateViewId());
        side.addView(buy); side.addView(sell); side.check(buy.getId());
        root.addView(side);

        entry = addField(root, "Entry price", "e.g. 2350.50");
        sl = addField(root, "Stop loss (MT5 points)", "e.g. 500");
        balance = addField(root, "Account balance", "e.g. 1000");
        risk = addField(root, "Risk (%)", "e.g. 1");

        addLabel(root, "Symbol specifications (from your broker's MT5)");
        point = addField(root, "Point size", "e.g. 0.01");
        tickSize = addField(root, "Tick size", "e.g. 0.01");
        tickValue = addField(root, "Tick value per 1.0 lot (account currency)", "e.g. 1.00");
        volumeStep = addField(root, "Volume step", "e.g. 0.01");

        Button calc = new Button(this);
        calc.setText("CALCULATE");
        root.addView(calc, params(-1, -2, 0, 0, 0, 14));
        output = new TextView(this);
        output.setTextColor(Color.WHITE);
        output.setTextSize(16);
        output.setPadding(dp(14), dp(16), dp(14), dp(16));
        output.setBackgroundColor(Color.rgb(31,38,50));
        root.addView(output, params(-1, -2, 0, 12, 0, 0));
        calc.setOnClickListener(v -> calculate());

        TextView note = new TextView(this);
        note.setText("Check point size, tick size, tick value and volume step for the exact symbol and account in MT5. Results are estimates; this app does not connect to MT5 or place trades.");
        note.setTextColor(Color.LTGRAY);
        note.setTextSize(12);
        note.setPadding(0, dp(16), 0, 0);
        root.addView(note);
    }

    int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density); }
    LinearLayout.LayoutParams params(int w,int h,int l,int t,int r,int b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w,h);
        p.setMargins(l==0?0:dp(l),dp(t),r==0?0:dp(r),dp(b)); return p;
    }
    void addLabel(LinearLayout root, String text) {
        TextView tv = new TextView(this); tv.setText(text); tv.setTextColor(Color.rgb(150,190,255));
        tv.setTextSize(14); tv.setTypeface(null,1); tv.setPadding(0,dp(12),0,dp(5)); root.addView(tv);
    }
    EditText addField(LinearLayout root, String label, String hint) {
        addLabel(root,label);
        EditText e = new EditText(this); e.setSingleLine(true); e.setTextColor(Color.WHITE);
        e.setHintTextColor(Color.GRAY); e.setHint(hint); e.setTextSize(16);
        e.setInputType( android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        e.setPadding(dp(12),dp(8),dp(12),dp(8)); e.setBackgroundColor(Color.rgb(31,38,50));
        root.addView(e, params(-1,dp(50),0,0,0,2)); return e;
    }
    double val(EditText e) { String s=e.getText().toString().trim(); if(s.isEmpty()) throw new IllegalArgumentException("Fill in every field."); return Double.parseDouble(s); }

    void calculate() {
        try {
            double ep=val(entry), stop=val(sl), bal=val(balance), rp=val(risk);
            double ps=val(point), ts=val(tickSize), tv=val(tickValue), step=val(volumeStep);
            if(stop<=0||bal<=0||rp<=0||rp>100||ps<=0||ts<=0||tv<=0||step<=0) throw new IllegalArgumentException("Values must be positive; risk must be 100% or less.");
            boolean isBuy = side.getCheckedRadioButtonId() == side.getChildAt(0).getId();
            double tpPoints=stop*3.0, distance=stop*ps;
            double slPrice=isBuy?ep-distance:ep+distance;
            double tpPrice=isBuy?ep+tpPoints*ps:ep-tpPoints*ps;
            double riskMoney=bal*rp/100.0;
            double lossPerLot=(distance/ts)*tv;
            double rawLots=riskMoney/lossPerLot;
            double lots=Math.floor((rawLots+1e-10)/step)*step;
            double actualRisk=lots*lossPerLot;
            if(lots<=0) throw new IllegalArgumentException("Calculated lot size is below the broker's minimum volume step.");
            output.setText(String.format(Locale.US,
                "TAKE PROFIT: %.0f points (1:3)\n\nStop loss price: %s\nTake profit price: %s\n\nSuggested lot size: %.4f\nRisk budget: %.2f\nEstimated risk at rounded lot: %.2f\nPotential reward at 1:3: %.2f",
                tpPoints, fmt(slPrice), fmt(tpPrice), lots, riskMoney, actualRisk, actualRisk*3.0));
        } catch(Exception ex) {
            output.setText("Please check your inputs. " + (ex.getMessage()==null?"Invalid number.":ex.getMessage()));
        }
    }
    String fmt(double n) { return String.format(Locale.US,"%.8f",n).replaceAll("0+$","").replaceAll("\\.$",""); }
}
