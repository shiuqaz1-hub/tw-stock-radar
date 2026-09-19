module.exports = async function handler(req, res) {
  const market = String(req.query.market || 'all').toLowerCase();
  const date = String(req.query.date || tradingDate());
  const rows = [];
  if (market !== 'otc') {
    const data = await fetchJson(`https://www.twse.com.tw/exchangeReport/MI_INDEX?response=json&date=${date}&type=ALLBUT0999`);
    const table = Array.isArray(data?.data9) ? data.data9 : (Array.isArray(data?.data) ? data.data : []);
    table.forEach((row) => { const item = normalize(row, '上市'); if (item) rows.push(item); });
  }
  if (market !== 'listed') {
    const data = await fetchJson(`https://www.tpex.org.tw/web/stock/aftertrading/daily_close_quotes/stk_quote_result.php?l=zh-tw&o=json&d=${date}`);
    const table = Array.isArray(data?.aaData) ? data.aaData : (Array.isArray(data?.data) ? data.data : []);
    table.forEach((row) => { const item = normalize(row, '上櫃'); if (item) rows.push(item); });
  }
  const stocks = [...new Map(rows.filter((x) => x.price > 0).map((x) => [x.code, x])).values()];
  res.setHeader('Cache-Control', 's-maxage=30, stale-while-revalidate=120');
  res.status(200).json({ date, delayed: true, count: stocks.length, stocks });
};
function tradingDate() { const d = new Date(Date.now() + 8 * 3600000); while ([0,6].includes(d.getUTCDay())) d.setUTCDate(d.getUTCDate() - 1); return `${d.getUTCFullYear()}${String(d.getUTCMonth()+1).padStart(2,'0')}${String(d.getUTCDate()).padStart(2,'0')}`; }
async function fetchJson(url) { const r = await fetch(url, { headers: { 'User-Agent': 'tw-stock-radar/1.0' } }); if (!r.ok) throw new Error(`upstream ${r.status}`); return r.json(); }
function num(v) { const n = Number(String(v ?? '').replace(/[,%\s]/g, '')); return Number.isFinite(n) ? n : 0; }
function normalize(row, market) { if (!Array.isArray(row) || row.length < 8) return null; const code=String(row[0]||'').trim(), name=String(row[1]||'').trim(); if (!/^\d{4,6}$/.test(code) || !name) return null; const price=num(row[8] || row[7] || row[2]); const change=num(row[9] || row[3]); return {code,name,market,price,change,changePct:price ? change/price*100 : 0,volume:num(row[2] || row[8]),open:num(row[4]),high:num(row[5]),low:num(row[6])}; }
