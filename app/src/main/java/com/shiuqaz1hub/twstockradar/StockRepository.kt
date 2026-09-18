package com.shiuqaz1hub.twstockradar

import retrofit2.HttpException

class StockRepository {
    suspend fun fetchStocks(codes: List<String>): List<Stock> {
        if (codes.isEmpty()) return emptyList()

        val uniqueCodes = codes
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        if (uniqueCodes.isEmpty()) return emptyList()

        val exCh = uniqueCodes.joinToString("|") { "tse_${it}.tw" }
        val response = RetrofitClient.api.getStocks(exCh = exCh, json = 1, delay = 0)

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val payload = response.body() ?: return emptyList()
        return payload.msgArray.orEmpty().mapNotNull { item ->
            val stock = item.toStock()
            if (stock.code.isEmpty()) null else stock
        }
    }
}
