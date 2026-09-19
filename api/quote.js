module.exports = async function handler(req, res) {
  const code = String(req.query.code || '').trim().toUpperCase();
  const market = String(req.query.market || 'tse').toLowerCase() === 'otc' ? 'otc' : 'tse';
  if (!/^\d{4,6}$/.test(code)) return res.status(400).json({ error: 'invalid stock code' });
  const data = await fetch(`https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=${market}_${code}.tw`, { headers: { 'User-Agent': 'tw-stock-radar/1.0' } }).then(r => r.json());
  const item = data?.msgArray?.[0];
  if (!item) return res.status(404).json({ error: 'quote not found', code });
  const price = number(item.z || item.p), previous = number(item.y), change = price - previous;
  res.setHeader('Cache-Control', 's-maxage=5, stale-while-revalidate=15');
  res.status(200).json({ code, name: item.n || '', market: market === 'tse' ? '上市' : '上櫃', price, previous, change, changePct: previous ? change / previous * 100 : 0, open: number(item.o), high: number(item.h), low: number(item.l), volume: number(item.v), time: item.t || '', bids: item.b || '', asks: item.a || '', raw: item });
};
function number(v) { const x = Number(String(v ?? '').replace(/,/g, '')); return Number.isFinite(x) ? x : 0; }
