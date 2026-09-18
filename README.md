# 台股雷達 Android App

這是一個台股雷達 Android App，包含：

- 自選股行情列表
- 即時 TWSE 公開行情抓取
- 新增 / 刪除自選股
- 搜尋股票名稱或代號
- 詳細資訊頁面
- 市場狀態判斷（開盤中、已收盤、休市）
- 簡易 K 線風格走勢圖

目前支援：
- 台積電、聯發科、鴻海、元大台灣50、中華電等自選股
- 可新增更多股票代號
- 盤中會持續更新行情資訊

此版本以 TWSE 公開 API 為基礎，提供免費行情讀取；若需要官方電子報價 / 毫秒級即時串流，通常須另行申請授權及對應資料服務。

## 建置方式

1. 開啟 Android Studio
2. 選擇 Open
3. 打開 `tw-stock-radar` 專案
4. 等待 Gradle 同步完成
5. 執行 Android Emulator 或實體裝置

## 需求

- Android Studio Ladybug 或更新版本
- Android SDK 35
- JDK 17

## 套件

- `com.shiuqaz1hub.twstockradar`
