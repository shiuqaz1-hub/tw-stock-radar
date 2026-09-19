module.exports = async function handler(req, res) {
  try {
    const code = String(req.query.code || '').trim().toUpperCase();
    const market = String(req.query.market || 'tse').toLowerCase() === 'otc' ? 'otc' : 'tse';

    if (!/^\d{4,6}$/.test(code)) {
      return res.status(400).json({ error: 'invalid stock code' });
    }

    const upstreamUrl = `https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=${market}_${code}.tw`;
    const response = await fetch(upstreamUrl, {
      headers: { 'User-Agent': 'tw-stock-radar/1.0' }
    });

    if (!response.ok) {
      return res.status(502).json({
        error: 'upstream fetch failed',
        code,
        market,
        status: response.status
      });
    }

    const data = await response.json();
    const item = Array.isArray(data?.msgArray) ? data.msgArray[0] : null;

    if (!item) {
      return res.status(404).json({ error: 'quote not found', code, market });
    }

    const price = number(item.z || item.p || item.o || 0);
    const previous = number(item.y || item.p || item.o || 0);
    const change = price - previous;

    return res.status(200).json({
      code,
      name: item.n || '',
      market: market === 'tse' ? '上市' : '上櫃',
      price,
      previous,
      change,
      changePct: previous ? (change / previous) * 100 : 0,
      open: number(item.o),
      high: number(item.h),
      low: number(item.l),
      close: number(item.z),
      volume: number(item.v || item.a || item.f || 0),
      time: item.t || new Date().toISOString()
    });
  } catch (error) {
    return res.status(500).json({
      error: 'server error',
      detail: String(error?.message || error)
    });
  }
};

function number(v) {
  const x = Number(String(v ?? '').replace(/,/g, ''));
  return Number.isFinite(x) ? x : 0;
}
