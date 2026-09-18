package com.shiuqaz1hub.twstockradar

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface TwseApiService {
    @GET("stock/api/getStockInfo.jsp")
    suspend fun getStocks(
        @Query("ex_ch") exCh: String,
        @Query("json") json: Int = 1,
        @Query("delay") delay: Int = 0
    ): Response<TwseResponse>
}

data class TwseResponse(
    @SerializedName("msgArray") val msgArray: List<TwseStockItem>? = emptyList()
)

data class TwseStockItem(
    @SerializedName("c") val code: String? = null,
    @SerializedName("n") val name: String? = null,
    @SerializedName("nf") val fallbackName: String? = null,
    @SerializedName("z") val price: String? = null,
    @SerializedName("o") val open: String? = null,
    @SerializedName("h") val high: String? = null,
    @SerializedName("l") val low: String? = null,
    @SerializedName("v") val volume: String? = null,
    @SerializedName("t") val time: String? = null,
    @SerializedName("y") val previousClose: String? = null
) {
    fun toStock(): Stock {
        val stock = Stock(name ?: fallbackName ?: "", code ?: "")
        stock.price = Stock.normalize(price ?: "--")
        stock.open = Stock.normalize(open ?: "--")
        stock.high = Stock.normalize(high ?: "--")
        stock.low = Stock.normalize(low ?: "--")
        stock.volume = Stock.normalize(volume ?: "--")
        stock.time = Stock.normalize(time ?: "")

        val previous = Stock.normalizeNumber(previousClose ?: "0")
        val current = Stock.normalizeNumber(stock.price)
        val previousValue = Stock.toDouble(previous)
        val currentValue = Stock.toDouble(current)
        val diff = currentValue - previousValue
        stock.change = String.format(java.util.Locale.US, "%+.2f", diff)
        stock.up = diff >= 0
        stock.percent = if (previousValue == 0.0) "--" else String.format(java.util.Locale.US, "%+.2f%%", (diff / previousValue) * 100)
        return stock
    }
}

object RetrofitClient {
    val api: TwseApiService by lazy {
        val builder = Retrofit.Builder()
            .baseUrl("https://mis.twse.com.tw/")
            .addConverterFactory(GsonConverterFactory.create())

        val retrofit = builder.build()
        retrofit.create(TwseApiService::class.java)
    }
}
