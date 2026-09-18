package com.shiuqaz1hub.twstockradar

import java.util.Locale

enum class AccountType(val label: String) {
    ACCOUNT_4("AI 當沖"),
    ACCOUNT_5("AI 波段")
}

enum class SignalType(val label: String) {
    BUY("買進"),
    SELL("賣出"),
    HOLD("持有")
}

data class TradeRecord(
    val account: AccountType,
    val symbol: String,
    val action: SignalType,
    val price: Double,
    val quantity: Int,
    val timestamp: String,
    val reason: String,
    val pnl: Double = 0.0,
    val notes: String = ""
)

data class PositionSnapshot(
    val account: AccountType,
    val symbol: String,
    val quantity: Int,
    val avgCost: Double,
    val currentPrice: Double,
    val pnl: Double,
    val action: String
)

data class AccountSummary(
    val account: AccountType,
    val cash: Double,
    val equity: Double,
    val pnl: Double,
    val winRate: Double,
    val trades: Int,
    val positions: Int,
    val status: String
)

object SimulationEngine {
    private val account4Signals = listOf(
        "當沖重點在短線爆發與量能放大，買進前須確認成交量放大且價格站穩均線",
        "日內反彈遇支撐不破，並且短期 RSI 轉強，視為短線買點",
        "若股價大於均線且量能放大，偏向追價，但須設停損避免反轉"
    )

    private val account5Signals = listOf(
        "波段策略偏好長紅K與均線多頭排列，重視後續持續性的量能支撐",
        "若股價站穩月線與半年線，且資金流向轉強，可維持多頭持倉",
        "盤整區間出現高量突破，且法人與外資連續加碼，採取中長線買進"
    )

    fun generateSignals(symbol: String, price: Double, volume: Long, trendScore: Double): List<TradeRecord> {
        val account4Signal = when {
            price > 0 && trendScore > 0.72 && volume > 500000 -> SignalType.BUY
            trendScore < -0.45 -> SignalType.SELL
            else -> SignalType.HOLD
        }

        val account5Signal = when {
            price > 0 && trendScore > 0.82 && volume > 800000 -> SignalType.BUY
            trendScore < -0.35 -> SignalType.SELL
            else -> SignalType.HOLD
        }

        val records = mutableListOf<TradeRecord>()

        if (account4Signal != SignalType.HOLD) {
            records.add(
                TradeRecord(
                    account = AccountType.ACCOUNT_4,
                    symbol = symbol,
                    action = account4Signal,
                    price = price,
                    quantity = 1000,
                    timestamp = java.time.LocalDateTime.now().toString(),
                    reason = account4Signals.random(),
                    pnl = if (account4Signal == SignalType.SELL) 120.0 else 0.0
                )
            )
        }

        if (account5Signal != SignalType.HOLD) {
            records.add(
                TradeRecord(
                    account = AccountType.ACCOUNT_5,
                    symbol = symbol,
                    action = account5Signal,
                    price = price,
                    quantity = 2000,
                    timestamp = java.time.LocalDateTime.now().toString(),
                    reason = account5Signals.random(),
                    pnl = if (account5Signal == SignalType.SELL) 420.0 else 0.0
                )
            )
        }

        return records
    }

    fun summarize(records: List<TradeRecord>): Pair<List<AccountSummary>, List<PositionSnapshot>> {
        val byAccount = records.groupBy { it.account }
        val summaries = mutableListOf<AccountSummary>()
        val positions = mutableListOf<PositionSnapshot>()

        for (account in AccountType.values()) {
            val accountRecords = byAccount[account].orEmpty()
            val cash = 5000000.0 + accountRecords.filter { it.action == SignalType.SELL }.sumOf { it.pnl }
            val equity = cash + accountRecords.filter { it.action == SignalType.BUY }.sumOf { it.pnl }
            val pnl = accountRecords.sumOf { it.pnl }
            val winRate = if (accountRecords.isEmpty()) 0.0 else {
                accountRecords.count { it.pnl > 0.0 } / accountRecords.size.toDouble()
            }

            summaries.add(
                AccountSummary(
                    account = account,
                    cash = cash,
                    equity = equity,
                    pnl = pnl,
                    winRate = winRate,
                    trades = accountRecords.size,
                    positions = accountRecords.filter { it.action == SignalType.BUY }.size,
                    status = if (account == AccountType.ACCOUNT_4) "當沖模式" else "波段模式"
                )
            )
        }

        val samplePositions = listOf(
            PositionSnapshot(AccountType.ACCOUNT_4, "2330", 1000, 1025.0, 1068.5, 43_500.0, "持有"),
            PositionSnapshot(AccountType.ACCOUNT_5, "2454", 1500, 628.0, 674.0, 69_000.0, "持有"),
            PositionSnapshot(AccountType.ACCOUNT_5, "2317", 1200, 170.0, 181.0, 13_200.0, "持有")
        )

        return summaries to samplePositions
    }

    fun formatMoney(value: Double): String = String.format(Locale.TAIWAN, "%,.0f", value)
}
