package com.shiuqaz1hub.twstockradar;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StockApi {
    public static List<Stock> fetchStocks(List<String> codes) throws Exception {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> unique = new ArrayList<>();
        for (String code : codes) {
            String cleaned = code == null ? "" : code.trim();
            if (!cleaned.isEmpty() && !unique.contains(cleaned)) {
                unique.add(cleaned);
            }
        }
        if (unique.isEmpty()) {
            return Collections.emptyList();
        }

        StringBuilder exCh = new StringBuilder();
        for (String code : unique) {
            if (exCh.length() > 0) exCh.append("|");
            exCh.append("tse_").append(code).append(".tw");
        }

        String url = "https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch="
                + URLEncoder.encode(exCh.toString(), "UTF-8")
                + "&json=1&delay=0";
        String response = get(url);
        JSONObject root = new JSONObject(response);
        JSONArray rows = root.optJSONArray("msgArray");
        if (rows == null) {
            return Collections.emptyList();
        }

        List<Stock> stocks = new ArrayList<>();
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.getJSONObject(i);
            Stock stock = Stock.fromTwseJson(row);
            if (stock != null && stock.code != null && !stock.code.isEmpty()) {
                stocks.add(stock);
            }
        }
        return stocks;
    }

    private static String get(String address) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestMethod("GET");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            return body.toString();
        } finally {
            connection.disconnect();
        }
    }
}
