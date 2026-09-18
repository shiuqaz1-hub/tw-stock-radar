package com.shiuqaz1hub.twstockradar;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private final List<Stock> stocks = Arrays.asList(
        new Stock("台積電", "2330", "1,080.00", "+15.00", "+1.41%", true),
        new Stock("聯發科", "2454", "1,320.00", "-10.00", "-0.75%", false),
        new Stock("鴻海", "2317", "185.50", "+3.50", "+1.92%", true),
        new Stock("元大台灣50", "0050", "198.20", "+1.20", "+0.61%", true),
        new Stock("中華電", "2412", "125.00", "-0.50", "-0.40%", false)
    );
    private LinearLayout list;
    private EditText search;
    private TextView updated;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        buildScreen();
        render("");
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), 0);
        root.setBackgroundColor(Color.rgb(248, 250, 252));

        TextView title = text("台股雷達", 28, Color.rgb(15, 23, 42));
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(42)));
        TextView subtitle = text("掌握自選股的即時變化", 14, Color.rgb(100, 116, 139));
        root.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(32)));

        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);
        search = new EditText(this);
        search.setHint("搜尋股票名稱或代號");
        search.setSingleLine(true);
        search.setTextSize(15);
        search.setPadding(dp(14), 0, dp(10), 0);
        search.setBackgroundColor(Color.WHITE);
        searchRow.addView(search, new LinearLayout.LayoutParams(0, dp(48), 1));
        Button refresh = new Button(this);
        refresh.setText("更新");
        refresh.setTextColor(Color.WHITE);
        refresh.setTextSize(14);
        refresh.setBackgroundColor(Color.rgb(37, 99, 235));
        LinearLayout.LayoutParams refreshParams = new LinearLayout.LayoutParams(dp(78), dp(48));
        refreshParams.setMargins(dp(8), 0, 0, 0);
        searchRow.addView(refresh, refreshParams);
        root.addView(searchRow, new LinearLayout.LayoutParams(-1, dp(56)));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView watch = text("我的自選股", 19, Color.rgb(15, 23, 42));
        watch.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        header.addView(watch, new LinearLayout.LayoutParams(0, dp(55), 1));
        updated = text("尚未更新", 12, Color.rgb(100, 116, 139));
        header.addView(updated);
        root.addView(header);

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int before, int count) { render(s.toString()); }
            public void afterTextChanged(Editable e) {}
        });
        refresh.setOnClickListener(v -> {
            updated.setText("剛剛更新");
            render(search.getText().toString());
        });
    }

    private void render(String query) {
        list.removeAllViews();
        String q = query.toLowerCase(Locale.ROOT).trim();
        int count = 0;
        for (Stock stock : stocks) {
            if (q.isEmpty() || stock.name.contains(q) || stock.code.contains(q)) {
                list.addView(card(stock));
                count++;
            }
        }
        if (count == 0) {
            TextView empty = text("找不到符合的股票", 15, Color.rgb(100, 116, 139));
            empty.setGravity(Gravity.CENTER);
            list.addView(empty, new LinearLayout.LayoutParams(-1, dp(100)));
        }
    }

    private View card(Stock stock) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(100));
        params.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(params);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView name = text(stock.name, 17, Color.rgb(15, 23, 42));
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        top.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
        top.addView(text(stock.code, 13, Color.rgb(100, 116, 139)));
        card.addView(top);

        LinearLayout bottom = new LinearLayout(this);
        bottom.setGravity(Gravity.CENTER_VERTICAL);
        bottom.setPadding(0, dp(8), 0, 0);
        bottom.addView(text(stock.price, 22, Color.rgb(15, 23, 42)), new LinearLayout.LayoutParams(0, -2, 1));
        int color = stock.up ? Color.rgb(220, 38, 38) : Color.rgb(22, 163, 74);
        TextView change = text(stock.change + "  " + stock.percent, 14, color);
        change.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        bottom.addView(change);
        card.addView(bottom);
        return card;
    }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value); view.setTextSize(size); view.setTextColor(color);
        return view;
    }
    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }

    private static class Stock {
        final String name, code, price, change, percent; final boolean up;
        Stock(String n, String c, String p, String ch, String pc, boolean u) { name=n; code=c; price=p; change=ch; percent=pc; up=u; }
    }
}
