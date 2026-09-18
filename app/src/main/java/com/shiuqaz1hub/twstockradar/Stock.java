package com.shiuqaz1hub.twstockradar;

import java.io.Serializable;
import java.util.Locale;
import org.json.JSONObject;

public class Stock implements Serializable {
    private static final long serialVersionUID = 1L;

    public String name;
    public String code;
    public String price = "--";
    public String change = "--";
    public String percent = "--";
    public String open = "--";
    public String high = "--";
    public String low = "--";
    public String volume = "--";
    public String time = "";
    public boolean up;

    public Stock(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public static Stock fromTwseJson(JSONObject row) {
        Stock stock = new Stock(row.optString("nf", row.optString("n", "")), row.optString("c", ""));
        stock.price = normalize(row.optString("z", "--"));
        stock.open = normalize(row.optString("o", "--"));
        stock.high = normalize(row.optString("h", "--"));
        stock.low = normalize(row.optString("l", "--"));
        stock.volume = normalize(row.optString("v", "--"));
        stock.time = normalize(row.optString("t", ""));

        String prev = normalizeNumber(row.optString("y", "0"));
        String curr = normalizeNumber(stock.price);
        double prevValue = toDouble(prev);
        double currValue = toDouble(curr);
        double diff = currValue - prevValue;
        stock.change = String.format(Locale.US, "%+.2f", diff);
        stock.up = diff >= 0;
        stock.percent = (prevValue == 0) ? "--" : String.format(Locale.US, "%+.2f%%", (diff / prevValue) * 100);
        return stock;
    }

    public static String normalize(String value) {
        if (value == null || value.isEmpty() || "-".equals(value)) {
            return "--";
        }
        return value;
    }

    public static String normalizeNumber(String value) {
        if (value == null || value.isEmpty() || "-".equals(value)) {
            return "0";
        }
        return value.replace(",", "");
    }

    public static double toDouble(String value) {
        try {
            return Double.parseDouble(normalizeNumber(value));
        } catch (Exception e) {
            return 0d;
        }
    }
}
