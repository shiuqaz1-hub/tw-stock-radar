package com.shiuqaz1hub.twstockradar

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object SimulationDemoData {
    fun generateTradeHistory(): List<TradeRecord> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now().format(formatter)

        return listOf(
            TradeRecord(
                account = AccountType.ACCOUNT_4,
                symbol = "2330",
                action = SignalType.BUY,
                price = 1020.0,
                quantity = 1000,
                timestamp = "$today 09:31:00",
                reason = "當沖策略：短線量能放大且站穩均線",
                pnl = 0.0
            ),
            TradeRecord(
                account = AccountType.ACCOUNT_4,
                symbol = "2330",
                action = SignalType.SELL,
                price = 1048.0,
                quantity = 1000,
                timestamp = "$today 13:25:00",
                reason = "當沖策略：短線反彈達到停利區",
                pnl = 28000.0,
                notes = "AI 當沖已完成當日交易"
            ),
            TradeRecord(
                account = AccountType.ACCOUNT_5,
                symbol = "2454",
                action = SignalType.BUY,
                price = 620.0,
                quantity = 1500,
                timestamp = "$today 10:11:00",
                reason = "波段策略：均線多頭且外資轉強",
                pnl = 0.0
            ),
            TradeRecord(
                account = AccountType.ACCOUNT_5,
                symbol = "2454",
                action = SignalType.SELL,
                price = 670.0,
                quantity = 1500,
                timestamp = "$today 14:18:00",
                reason = "波段策略：達到目標價位與停利條件",
                pnl = 75000.0,
                notes = "AI 波段可持續觀察後續趨勢"
            )
        )
    }
}
