# AI 模擬股票交易系統

這個 repo 現在已包含一個模擬交易架構，重點是：

- 帳戶 4：AI 當沖
- 帳戶 5：AI 波段
- 交易與績效紀錄
- 本地模擬判斷邏輯
- 一個可檢視歷史交易的靜態儀表板
- 不會連接真實券商

## 主要內容

- `app/src/main/java/com/shiuqaz1hub/twstockradar/SimulationEngine.kt`
  - 交易訊號與帳戶摘要邏輯
- `app/src/main/java/com/shiuqaz1hub/twstockradar/SimulationDemoData.kt`
  - 範例歷史交易紀錄
- `docs/ai-trading-dashboard.html`
  - 用於查看帳戶 4 / 5 的模擬交易歷史與績效

## 使用方式

1. 打開 `docs/ai-trading-dashboard.html`
2. 在瀏覽器中預覽
3. 可觀察範例帳戶資料與 AI 判斷紀錄

## 設計原則

- 不連接真實券商 API
- 不執行真實下單
- 交易資料只保留於模擬環境
- 可擴充成 Web + Backend + Database 版本

## 後續擴充建議

- 儲存交易紀錄進資料庫（PostgreSQL / SQLite）
- 建立 Web Dashboard + 登入系統
- 增加持倉、損益、勝率、最大回撤
- 增加策略回測功能
- 改成每分鐘/每日由排程自動更新
- 將這個模擬交易邏輯擴充進後端服務
