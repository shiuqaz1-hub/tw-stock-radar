package com.shiuqaz1hub.twstockradar;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StockStorage {
    private static final String PREFS = "tw_stock_radar";
    private static final String KEY = "watchlist";
    private final SharedPreferences prefs;

    public StockStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<String> getWatchlist() {
        String raw = prefs.getString(KEY, null);
        if (raw == null || raw.trim().isEmpty()) {
            return new ArrayList<>(Arrays.asList("2330", "2454", "2317", "0050", "2412"));
        }
        try {
            JSONArray arr = new JSONArray(raw);
            List<String> codes = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                String code = arr.optString(i, "").trim();
                if (!code.isEmpty() && !codes.contains(code)) {
                    codes.add(code);
                }
            }
            if (codes.isEmpty()) {
                return defaultCodes();
            }
            return codes;
        } catch (Exception e) {
            return defaultCodes();
        }
    }

    public void saveWatchlist(List<String> codes) {
        JSONArray arr = new JSONArray();
        for (String code : codes) {
            if (code != null && !code.trim().isEmpty()) {
                arr.put(code.trim());
            }
        }
        prefs.edit().putString(KEY, arr.toString()).apply();
    }

    public void addSymbol(String code) {
        List<String> codes = getWatchlist();
        String cleaned = code == null ? "" : code.trim();
        if (!cleaned.isEmpty() && !codes.contains(cleaned)) {
            codes.add(cleaned);
            saveWatchlist(codes);
        }
    }

    public void removeSymbol(String code) {
        List<String> codes = getWatchlist();
        String cleaned = code == null ? "" : code.trim();
        codes.remove(cleaned);
        saveWatchlist(codes);
    }

    public static List<String> defaultCodes() {
        return new ArrayList<>(Arrays.asList("2330", "2454", "2317", "0050", "2412"));
    }
}
