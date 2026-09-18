const stockUniverse = [
  { code: '2330', name: '台積電' },
  { code: '2454', name: '聯發科' },
  { code: '2317', name: '鴻海' },
  { code: '0050', name: '元大台灣50' },
  { code: '2412', name: '中華電' },
  { code: '2891', name: '中信金' },
  { code: '2207', name: '和泰車' }
];

function clamp(value, min, max) {
  return Math.min(Math.max(value, min), max);
}

function sma(values, period) {
  if (!values.length) return 0;
  const slice = values.slice(-period);
  return slice.reduce((sum, v) => sum + v, 0) / slice.length;
}

function simulateMarket(days = 12) {
  const market = [];
  let basePrice = {};

  stockUniverse.forEach((stock, index) => {
    basePrice[stock.code] = 120 + index * 40 + (Math.random() * 20);
  });

  for (let i = 0; i < days; i++) {
    const date = new Date();
    date.setDate(date.getDate() - (days - i - 1));
    const dateKey = date.toISOString().slice(0, 10);
    const daily = { date: dateKey, prices: {} };

    stockUniverse.forEach((stock, index) => {
      const drift = (i / days) * 0.18 + (index * 0.02);
      const noise = (Math.random() - 0.5) * 0.08;
      const close = basePrice[stock.code] * (1 + drift + noise);
      const open = close * (0.98 + Math.random() * 0.04);
      const high = Math.max(open, close) * (1 + (Math.random() * 0.06 + 0.01));
      const low = Math.min(open, close) * (1 - (Math.random() * 0.06 + 0.01));
      const volume = Math.round((1200000 + index * 180000 + Math.random() * 800000) * (1 + i * 0.12));
      daily.prices[stock.code] = {
        open, high, low, close, volume
      };
      basePrice[stock.code] = close;
    });
    market.push(daily);
  }
  return market;
}

function computeJudgment(accountType, stockCode, dayData, history) {
  const prices = history.map((entry) => entry.close);
  const historyLen = prices.length;
  const shortMa = sma(prices, 5);
  const midMa = sma(prices, 10);
  const avgVolume = history.reduce((sum, e) => sum + e.volume, 0) / historyLen || 1;
  const current = history[historyLen - 1]?.close || dayData.close;
  const volume = dayData.volume;
  const relativeVolume = volume / avgVolume;
  const momentum = (current - history[Math.max(0, historyLen - 3)]?.close || current) / (history[Math.max(0, historyLen - 3)]?.close || current);

  if (accountType === 'ACCOUNT_4') {
    if (current > shortMa && shortMa > midMa && relativeVolume > 1.25 && momentum > 0.015) {
      return { action: 'BUY', reason: '當沖：短線爆發 + 量能放大 + 均線多頭' };
    }
    if (current < shortMa && momentum < -0.02) {
      return { action: 'SELL', reason: '當沖：短線破壞均線與動能轉弱' };
    }
    return { action: 'HOLD', reason: '當沖：等待更明確突破或回檔' };
  }

  if (current > midMa && shortMa > midMa && relativeVolume > 1.12 && momentum > 0.02) {
    return { action: 'BUY', reason: '波段：中期趨勢向上 + 量能支撐 + 均線多頭' };
  }
  if (current < midMa && momentum < -0.025) {
    return { action: 'SELL', reason: '波段：中期趨勢轉弱，宜先止損' };
  }
  return { action: 'HOLD', reason: '波段：震盪中，觀望趨勢確認' };
}

function runSimulation() {
  const market = simulateMarket(12);
  const accounts = {
    ACCOUNT_4: { cash: 5000000, positions: {}, trades: [], equity: 5000000 },
    ACCOUNT_5: { cash: 5000000, positions: {}, trades: [], equity: 5000000 }
  };

  const historyMap = {};
  stockUniverse.forEach((stock) => {
    historyMap[stock.code] = [];
  });

  const allTrades = [];

  market.forEach((day) => {
    stockUniverse.forEach((stock) => {
      const priceInfo = day.prices[stock.code];
      const codeHistory = historyMap[stock.code];
      codeHistory.push({
        date: day.date,
        close: priceInfo.close,
        volume: priceInfo.volume,
        high: priceInfo.high,
        low: priceInfo.low,
        open: priceInfo.open
      });

      ['ACCOUNT_4', 'ACCOUNT_5'].forEach((accountKey) => {
        const account = accounts[accountKey];
        const decision = computeJudgment(accountKey, stock.code, priceInfo, codeHistory);
        const position = account.positions[stock.code];

        if (decision.action === 'BUY' && !position && account.cash > 0) {
          const unit = accountKey === 'ACCOUNT_4' ? 1000 : 1500;
          const qty = Math.max(1, Math.floor(account.cash * 0.15 / priceInfo.close / unit) * unit);
          const spend = qty * priceInfo.close;
          if (spend <= account.cash) {
            account.cash -= spend;
            account.positions[stock.code] = {
              qty,
              avgCost: priceInfo.close,
              entryDate: day.date,
              symbol: stock.code,
              account: accountKey
            };
            const trade = {
              account: accountKey,
              symbol: stock.code,
              action: 'BUY',
              price: priceInfo.close,
              quantity: qty,
              time: day.date,
              reason: decision.reason,
              pnl: 0
            };
            allTrades.push(trade);
            account.trades.push(trade);
          }
        }

        if (decision.action === 'SELL' && position) {
          const saleValue = position.qty * priceInfo.close;
          account.cash += saleValue;
          const pnl = saleValue - position.qty * position.avgCost;
          const trade = {
            account: accountKey,
            symbol: stock.code,
            action: 'SELL',
            price: priceInfo.close,
            quantity: position.qty,
            time: day.date,
            reason: decision.reason,
            pnl
          };
          allTrades.push(trade);
          account.trades.push(trade);
          delete account.positions[stock.code];
        }
      });
    });
  });

  Object.keys(accounts).forEach((accountKey) => {
    const account = accounts[accountKey];
    const holdings = Object.values(account.positions);
    const currentValue = holdings.reduce((sum, p) => {
      const latest = market[market.length - 1].prices[p.symbol];
      return sum + p.qty * (latest ? latest.close : p.avgCost);
    }, 0);
    account.equity = account.cash + currentValue;
  });

  return { market, allTrades, accounts };
}

function numberFormat(value) {
  return new Intl.NumberFormat('zh-TW', { maximumFractionDigits: 0 }).format(value);
}

function renderDashboard() {
  const result = runSimulation();
  const accounts = result.accounts;

  const cards = document.getElementById('summaryCards');
  cards.innerHTML = '';

  Object.keys(accounts).forEach((key) => {
    const data = accounts[key];
    const pnl = data.equity - 5000000;
    const card = document.createElement('div');
    card.className = 'card';
    card.innerHTML = `
      <div class="label">${key === 'ACCOUNT_4' ? '帳戶 4 · 當沖' : '帳戶 5 · 波段'}</div>
      <div class="value ${pnl >= 0 ? 'green' : 'red'}">${pnl >= 0 ? '+' : '-'}NT$ ${numberFormat(Math.abs(pnl))}</div>
      <div class="subtle">現金 ${numberFormat(data.cash)} · 權益 ${numberFormat(data.equity)}</div>
    `;
    cards.appendChild(card);
  });

  const tradeBody = document.getElementById('tradeBody');
  tradeBody.innerHTML = '';
  result.allTrades.forEach((trade) => {
    const row = document.createElement('tr');
    const actionClass = trade.action === 'BUY' ? 'buy' : 'sell';
    row.innerHTML = `
      <td>${trade.account === 'ACCOUNT_4' ? '帳戶 4' : '帳戶 5'}</td>
      <td>${trade.symbol}</td>
      <td><span class="pill ${actionClass}">${trade.action}</span></td>
      <td>${numberFormat(Math.round(trade.price))}</td>
      <td>${numberFormat(trade.quantity)}</td>
      <td>${trade.time}</td>
      <td>${trade.reason}</td>
    `;
    tradeBody.appendChild(row);
  });

  const statusText = document.getElementById('statusText');
  statusText.textContent = `模擬結果：${result.allTrades.length} 筆成交，正在展示帳戶 4 / 5 自動判斷結果`;
}

document.addEventListener('DOMContentLoaded', () => {
  renderDashboard();
  document.getElementById('runBtn').addEventListener('click', renderDashboard);
});
