module.exports = async function handler(req, res) {
  try {
    const requestedCode = String(req.query.code || '').trim().toUpperCase();
    const requestedIndex = String(req.query.index || '').trim().toLowerCase();
    const market = String(req.query.market || 'tse').toLowerCase() === 'otc' ? 'otc' : 'tse';
    const isTaiex = requestedIndex === 'taiex' || (requestedCode === '0050' && market === 'tse');

    if (!isTaiex && !/^\d{4,6}$/.test(requestedCode)) {
      return res.status(400).json({ error: 'invalid stock code' });
    }

    // TWSE MIS uses t00.tw for the actual Taiwan Weighted Index.
    const upstreamCode = isTaiex ? 't00' : requestedCode;
    const upstreamMarket = isTaiex ? 'tse' : market;
    const upstreamUrl = `https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=${upstreamMarket}_${upstreamCode}.tw&json=1`;
    const response = await fetch(upstreamUrl, { headers: { Accept: 'application/json' } });

    if (!response.ok) {
      return res.status(502).json({ error: 'upstream fetch failed', code: requestedCode, market, status: response.status });
    }

    const data = await response.json();
    const item = Array.isArray(data?.msgArray) ? data.msgArray[0] : null;
    if (!item) return res.status(404).json({ error: 'quote not found', code: requestedCode, market });

    const previous = number(item.y);
    const price = number(item.z || item.p || item.a || item.o || item.cl);
    const change = previous ? price - previous : number(item.d);

    res.setHeader('Cache-Control', 'no-store, max-age=0');
    return res.status(200).json({
      code: isTaiex ? 'TAIEX' : requestedCode,
      name: isTaiex ? '加權指數' : (item.n || item.nf || ''),
      market: isTaiex ? '指數' : (market === 'otc' ? '上櫃' : '上市'),
      price,
      previous,
      change,
      changePct: previous ? (change / previous) * 100 : 0,
      open: number(item.o),
      high: number(item.h),
      low: number(item.l),
      close: number(item.z || item.p || item.cl),
      volume: number(item.v || item.a || item.f || 0),
      time: item.t || new Date().toISOString(),
      delayed: false,
      source: 'TWSE MIS'
    });
  } catch (error) {
    return res.status(500).json({ error: 'server error', detail: String(error?.message || error) });
  }
};

function number(value) {
  const x = Number(String(value ?? '').replace(/,/g, ''));
  return Number.isFinite(x) ? x : 0;
}
