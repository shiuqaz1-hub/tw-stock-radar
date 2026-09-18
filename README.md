# 台股雷達 Android App

原生 Android Java 台股行情 MVP，已串接 TWSE MIS 公開行情介面：

- 盤中每 30 秒自動更新行情
- 非盤中每 5 分鐘更新一次
- 顯示最新價、漲跌、漲跌幅、開高低、成交量與行情時間
- 顯示台灣時區的尚未開盤、開盤中、已收盤、週末休市
- 可依股票名稱或代號搜尋

目前自選股：2330、2454、2317、0050、2412。行情介面使用台灣證券交易所公開資料；免費公開資料不保證毫秒級即時，若需要商用即時串流或完整休市日曆，需依資料服務商授權與規範接入。

## 建置

- Android Studio Ladybug 或更新版本
- Android SDK 35
- JDK 17
- Package：`com.shiuqaz1hub.twstockradar`

開啟 Android Studio，選擇 **Open** 開啟專案，等待 Gradle 同步後即可執行。
