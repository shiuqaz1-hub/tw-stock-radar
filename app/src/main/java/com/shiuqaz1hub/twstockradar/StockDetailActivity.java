package com.shiuqaz1hub.twstockradar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import java.util.Arrays;
import java.util.Locale;

public class StockDetailActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        Stock stock = (Stock) getIntent().getSerializableExtra("stock");
        if (stock == null) {
            finish();
            return;
        }

        TextView title = findViewById(R.id.detailTitle);
        TextView code = findViewById(R.id.detailCode);
        TextView price = findViewById(R.id.detailPrice);
        TextView change = findViewById(R.id.detailChange);
        TextView status = findViewById(R.id.detailStatus);
        TextView open = findViewById(R.id.metricOpen);
        TextView high = findViewById(R.id.metricHigh);
        TextView low = findViewById(R.id.metricLow);
        TextView volume = findViewById(R.id.metricVolume);
        TextView time = findViewById(R.id.metricTime);
        StockChartView chart = findViewById(R.id.chartView);

        title.setText(stock.name);
        code.setText(stock.code);
        price.setText(stock.price);
        change.setText(stock.change + "  " + stock.percent);
        change.setTextColor(stock.up ? Color.rgb(220, 38, 38) : Color.rgb(22, 163, 74));
        status.setText("市場狀態：" + MarketStatusResolver.getStatus());
        status.setTextColor(MarketStatusResolver.isOpen() ? Color.rgb(220, 38, 38) : Color.rgb(37, 99, 235));

        open.setText("開盤：" + stock.open);
        high.setText("最高：" + stock.high);
        low.setText("最低：" + stock.low);
        volume.setText("成交量：" + stock.volume);
        time.setText("更新時間：" + (stock.time.isEmpty() ? "--" : stock.time));

        float openValue = Stock.toDouble(stock.open);
        float highValue = Stock.toDouble(stock.high);
        float lowValue = Stock.toDouble(stock.low);
        float closeValue = Stock.toDouble(stock.price);
        float[] series = {
                openValue * 0.995f,
                openValue * 1.002f,
                lowValue * 0.998f,
                closeValue,
                highValue * 1.001f,
                highValue
        };
        chart.setData(series);
    }
}
