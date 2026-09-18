package com.shiuqaz1hub.twstockradar;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private final List<Stock> stocks = new ArrayList<>();
    private final List<Stock> watchlist = Arrays.asList(
        new Stock("台積電", "2330"), new Stock("聯發科", "2454"),
        new Stock("鴻海", "2317"), new Stock("元大台灣50", "0050"),
        new Stock("中華電", "2412")
    );
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private LinearLayout list;
    private EditText search;
    private TextView updated, market;
    private boolean loading;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        buildScreen();
        refreshData();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), 0);
        root.setBackgroundColor(Color.rgb(248, 250, 252));

        TextView title = text("台股雷達", 28, Color.rgb(15, 23, 42));
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(42)));
        root.addView(text("即時掌握開盤、收盤與休市狀態", 14, Color.rgb(100, 116, 139)),
                new LinearLayout.LayoutParams(-1, dp(32)));

        market = text("市場狀態：讀取中…", 15, Color.rgb(37, 99, 235));
        market.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(market, new LinearLayout.LayoutParams(-1, dp(34)));

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

        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int before, int count) { render(s.toString()); }
            public void afterTextChanged(android.text.Editable e) {}
        });
        refresh.setOnClickListener(v -> refreshData());
    }

    private void refreshData() {
        if (loading) return;
        loading = true;
        market.setText("市場狀態：更新中…");
        executor.execute(() -> {
            List<Stock> result = new ArrayList<>();
            String error = null;
            try {
                StringBuilder symbols = new StringBuilder();
                for (Stock s : watchlist) {
                    if (symbols.length() > 0) symbols.append("|");
                    symbols.append("tse_").append(s.code).append(".tw");
                }
                String url = "https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch="
                        + URLEncoder.encode(symbols.toString(), "UTF-8") + "&json=1&delay=0";
                JSONObject root = new JSONObject(get(url));
                JSONArray rows = root.optJSONArray("msgArray");
                if (rows == null) throw new Exception("行情資料格式錯誤");
                for (int i = 0; i < rows.length(); i++) result.add(Stock.fromJson(rows.getJSONObject(i)));
                if (result.isEmpty()) error = "目前沒有可用行情";
            } catch (Exception e) { error = e.getMessage(); }
            final List<Stock> data = result;
            final String failure = error;
            runOnUiThread(() -> {
                loading = false;
                if (!data.isEmpty()) { stocks.clear(); stocks.addAll(data); render(search.getText().toString()); }
                updated.setText(new SimpleDateFormat("HH:mm:ss", Locale.TAIWAN).format(new Date()) + " 更新");
                updateMarketStatus(failure);
            });
        });
    }

    private void updateMarketStatus(String failure) {
        String status = localMarketStatus();
        if (failure != null && stocks.isEmpty()) status += "（行情暫時無法取得）";
        market.setText("市場狀態：" + status);
        market.setTextColor(status.startsWith("開盤") ? Color.rgb(220, 38, 38) : Color.rgb(37, 99, 235));
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(this::refreshData, status.startsWith("開盤") ? 30000L : 300000L);
    }

    private String localMarketStatus() {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"));
        int day = now.get(Calendar.DAY_OF_WEEK);
        int minutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) return "休市（週末）";
        if (minutes < 9 * 60) return "尚未開盤（09:00 開盤）";
        if (minutes <= 13 * 60 + 30) return "開盤中（09:00–13:30）";
        return "已收盤（13:30 收盤）";
    }

    private void render(String query) {
        list.removeAllViews();
        String q = query.toLowerCase(Locale.ROOT).trim();
        int count = 0;
        for (Stock stock : stocks) {
            if (q.isEmpty() || stock.name.contains(q) || stock.code.contains(q)) {
                list.addView(card(stock)); count++;
            }
        }
        if (count == 0) {
            TextView empty = text(stocks.isEmpty() ? "正在取得台股行情…" : "找不到符合的股票", 15, Color.rgb(100, 116, 139));
            empty.setGravity(Gravity.CENTER);
            list.addView(empty, new LinearLayout.LayoutParams(-1, dp(100)));
        }
    }

    private View card(Stock stock) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(118));
        params.setMargins(0, 0, 0, dp(10)); card.setLayoutParams(params);
        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL);
        TextView name = text(stock.name, 17, Color.rgb(15, 23, 42));
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        top.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
        top.addView(text(stock.code + "  " + stock.time, 12, Color.rgb(100, 116, 139)));
        card.addView(top);
        LinearLayout bottom = new LinearLayout(this); bottom.setGravity(Gravity.CENTER_VERTICAL);
        bottom.setPadding(0, dp(8), 0, 0);
        bottom.addView(text(stock.price, 22, Color.rgb(15, 23, 42)), new LinearLayout.LayoutParams(0, -2, 1));
        int color = stock.up ? Color.rgb(220, 38, 38) : Color.rgb(22, 163, 74);
        TextView change = text(stock.change + "  " + stock.percent, 14, color);
        change.setTypeface(Typeface.DEFAULT, Typeface.BOLD); bottom.addView(change); card.addView(bottom);
        card.addView(text("開 " + stock.open + "　高 " + stock.high + "　低 " + stock.low + "　量 " + stock.volume,
                12, Color.rgb(100, 116, 139)));
        return card;
    }

    private static String get(String address) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(10000); connection.setReadTimeout(10000); connection.setRequestMethod("GET");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder body = new StringBuilder(); String line;
            while ((line = reader.readLine()) != null) body.append(line);
            return body.toString();
        } finally { connection.disconnect(); }
    }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this); view.setText(value); view.setTextSize(size); view.setTextColor(color); return view;
    }
    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }

    private static class Stock {
        String name, code, price = "--", change = "--", percent = "--", open = "--", high = "--", low = "--", volume = "--", time = "";
        boolean up;
        Stock(String n, String c) { name = n; code = c; }
        static Stock fromJson(JSONObject row) {
            Stock s = new Stock(row.optString("n", row.optString("nf", "")), row.optString("c", ""));
            s.price = dash(row.optString("z", "--"));
            String previous = number(row.optString("y", "0"));
            String current = number(s.price);
            double diff = toDouble(current) - toDouble(previous);
            s.change = format(diff);
            s.up = diff >= 0;
            s.percent = toDouble(previous) == 0 ? "--" : String.format(Locale.US, "%+.2f%%", diff / toDouble(previous) * 100);
            s.open = dash(row.optString("o", "--")); s.high = dash(row.optString("h", "--")); s.low = dash(row.optString("l", "--"));
            s.volume = dash(row.optString("v", "--")); s.time = row.optString("t", "");
            return s;
        }
        static String dash(String value) { return value == null || value.isEmpty() || "-".equals(value) ? "--" : value; }
        static String number(String value) { return value == null || value.isEmpty() || "-".equals(value) ? "0" : value.replace(",", ""); }
        static double toDouble(String value) { try { return Double.parseDouble(number(value)); } catch (Exception e) { return 0; } }
        static String format(double value) { return String.format(Locale.US, "%+.2f", value); }
    }

    @Override protected void onDestroy() { super.onDestroy(); handler.removeCallbacksAndMessages(null); executor.shutdownNow(); }
}
