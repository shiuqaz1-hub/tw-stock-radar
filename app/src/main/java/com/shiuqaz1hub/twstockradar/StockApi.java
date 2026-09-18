package com.shiuqaz1hub.twstockradar;

import java.util.ArrayList;
import java.util.List;

public class StockApi {
    public static List<Stock> fetchStocks(List<String> codes) throws Exception {
        return new StockRepository().fetchStocks(codes);
    }
}
