package com.shiuqaz1hub.twstockradar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private final List<Stock> stocks = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private StockStorage storage;
    private EditText searchInput;
    private LinearLayout stockList;
    private TextView marketStatus;
    private TextView updatedAt;
    private boolean loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storage = new StockStorage(this);
        searchInput = findViewById(R.id.searchInput);
        stockList = findViewById(R.id.stockList);
        marketStatus = findViewById(R.id.marketStatus);
        updatedAt = findViewById(R.id.updatedAt);
        Button addButton = findViewById(R.id.addButton);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderStocks(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        addButton.setOnClickListener(v -> {
            String code = searchInput.getText().toString().trim();
            String cleaned = code.replaceAll("[^0-9]", "");
            if (!cleaned.isEmpty()) {
                storage.addSymbol(cleaned);
                searchInput.setText("");
                refreshStocks();
            }
        });

        refreshStocks();
    }

    private void refreshStocks() {
        if (loading) return;
        loading = true;
        marketStatus.setText("市場狀態：更新中…");
        final List<String> codes = storage.getWatchlist();
        executor.execute(() -> {
            List<Stock> loaded = new ArrayList<>();
            try {
                loaded = StockApi.fetchStocks(codes);
            } catch (Exception e) {
                loaded = new ArrayList<>();
            }

            final List<Stock> finalList = loaded;
            handler.post(() -> {
                loading = false;
                stocks.clear();
                stocks.addAll(finalList);
                renderStocks();
                updatedAt.setText(new SimpleDateFormat("HH:mm:ss", Locale.TAIWAN).format(new Date()) + " 更新");
                marketStatus.setText("市場狀態：" + MarketStatusResolver.getStatus());
                marketStatus.setTextColor(MarketStatusResolver.isOpen() ? Color.rgb(220, 38, 38) : Color.rgb(37, 99, 235));
            });
        });
    }

    private void renderStocks() {
        stockList.removeAllViews();
        String query = searchInput.getText().toString().trim().toLowerCase(Locale.ROOT);
        int count = 0;

        for (Stock stock : stocks) {
            String matchText = (stock.name + " " + stock.code).toLowerCase(Locale.ROOT);
            if (query.isEmpty() || matchText.contains(query)) {
                stockList.addView(createCard(stock));
                count++;
            }
        }

        if (count == 0) {
            TextView empty = new TextView(this);
            empty.setText(stocks.isEmpty() ? "正在取得台股行情…" : "找不到符合的股票");
            empty.setTextColor(Color.rgb(100, 116, 139));
            empty.setTextSize(15);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(40), 0, 0);
            stockList.addView(empty, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(100)
            ));
        }
    }

    private View createCard(final Stock stock) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(120)
        );
        params.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(params);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView nameView = new TextView(this);
        nameView.setText(stock.name);
        nameView.setTextColor(Color.rgb(15, 23, 42));
        nameView.setTextSize(17);
        nameView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        top.addView(nameView, nameParams);

        TextView codeView = new TextView(this);
        codeView.setText(stock.code);
        codeView.setTextColor(Color.rgb(100, 116, 139));
        codeView.setTextSize(13);
        top.addView(codeView);

        card.addView(top);

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setGravity(Gravity.CENTER_VERTICAL);
        bottom.setPadding(0, dp(8), 0, 0);

        TextView priceView = new TextView(this);
        priceView.setText(stock.price);
        priceView.setTextColor(Color.rgb(15, 23, 42));
        priceView.setTextSize(22);
        priceView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        bottom.addView(priceView, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView changeView = new TextView(this);
        changeView.setText(stock.change + "  " + stock.percent);
        changeView.setTextColor(stock.up ? Color.rgb(220, 38, 38) : Color.rgb(22, 163, 74));
        changeView.setTextSize(14);
        changeView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        bottom.addView(changeView);
        card.addView(bottom);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(8), 0, 0);

        Button detail = new Button(this);
        detail.setText("明細");
        detail.setTextColor(Color.WHITE);
        detail.setBackgroundColor(Color.rgb(37, 99, 235));
        detail.setOnClickListener(v -> openDetail(stock));
        actions.addView(detail, new LinearLayout.LayoutParams(0, dp(38), 1f));

        Button remove = new Button(this);
        remove.setText("移除");
        remove.setTextColor(Color.WHITE);
        remove.setBackgroundColor(Color.rgb(148, 163, 184));
        remove.setOnClickListener(v -> {
            storage.removeSymbol(stock.code);
            refreshStocks();
        });
        actions.addView(remove, new LinearLayout.LayoutParams(0, dp(38), 1f));
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        actionParams.setMargins(0, dp(4), 0, 0);
        actions.setLayoutParams(actionParams);
        card.addView(actions);

        return card;
    }

    private void openDetail(Stock stock) {
        Intent intent = new Intent(this, StockDetailActivity.class);
        intent.putExtra("stock", stock);
        startActivity(intent);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
        handler.removeCallbacksAndMessages(null);
    }
}
