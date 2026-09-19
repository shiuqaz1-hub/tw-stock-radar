module.exports = async function handler(req, res) {
  const requestedDate = String(req.query.date || '').trim();
  const date = /^\d{8}$/.test(requestedDate) ? requestedDate : tradingDate();
  const rows = [];
  const errors = [];

  try {
    const data = await fetchJson(`https://www.twse.com.tw/exchangeReport/MI_INDEX?response=json&date=${date}&type=ALLBUT0999`);
    const table = Array.isArray(data?.data9) ? data.data9 : [];
    table.forEach(row => { const x = parseTwse(row); if (x) rows.push(x); });
  } catch (e) { errors.push('TWSE'); }

  try {
    const data = await fetchJson(`https://www.tpex.org.tw/web/stock/aftertrading/daily_close_quotes/stk_quote_result.php?l=zh-tw&o=json&d=${date}`);
    const table = Array.isArray(data?.aaData) ? data.aaData : [];
    table.forEach(row => { const x = parseTpex(row); if (x) rows.push(x); });
  } catch (e) { errors.push('TPEX'); }

  const stocks = [...new Map(rows.map(x => [x.code, x])).values()]
    .filter(x => x.price > 0)
    .sort((a, b) => b.changePct - a.changePct);

  res.setHeader('Cache-Control', 's-maxage=30, stale-while-revalidate=120');
  res.status(200).json({ date, delayed: true, count: stocks.length, errors, stocks });
};

function tradingDate() {
  const d = new Date(Date.now() + 8 * 3600000);
  if (d.getUTCHours() < 1) d.setUTCDate(d.getUTCDate() - 1);
  while ([0, 6].includes(d.getUTCDay())) d.setUTCDate(d.getUTCDate() - 1);
  return `${d.getUTCFullYear()}${String(d.getUTCMonth() + 1).padStart(2, '0')}${String(d.getUTCDate()).padStart(2, '0')}`;
}

async function fetchJson(url) {
  const response = await fetch(url, { headers: { Accept: 'application/json' } });
  if (!response.ok) throw new Error(String(response.status));
  return response.json();
}

function num(value) {
  const text = String(value ?? '').replace(/[,%\s]/g, '');
  if (!text || text === '--' || text === '-') return 0;
  const result = Number(text);
  return Number.isFinite(result) ? result : 0;
}

function valid(code, name) { return /^\d{4,6}$/.test(code) && Boolean(name); }

function parseTwse(row) {
  if (!Array.isArray(row) || row.length < 10) return null;
  const code = String(row[0] || '').trim();
  const name = String(row[1] || '').trim();
  if (!valid(code, name)) return null;
  const price = num(row[8]);
  const change = num(row[9]);
  const previous = price - change;
  return { code, name, market: '上市', price, previous, change, changePct: previous ? change / previous * 100 : 0, volume: num(row[2]), open: num(row[5]), high: num(row[6]), low: num(row[7]) };
}

function parseTpex(row) {
  if (!Array.isArray(row) || row.length < 8) return null;
  const code = String(row[0] || '').trim();
  const name = String(row[1] || '').trim();
  if (!valid(code, name)) return null;
  const price = num(row[2]);
  const change = num(row[3]);
  const previous = price - change;
  return { code, name, market: '上櫃', price, previous, change, changePct: previous ? change / previous * 100 : 0, volume: num(row[8] || row[15]), open: num(row[4]), high: num(row[5]), low: num(row[6]) };
}
