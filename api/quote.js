module.exports = async function handler(req, res) {
  try {
    const requestedCode = String(req.query.code || '').trim().toUpperCase();
    const market = String(req.query.market || 'tse').toLowerCase() === 'otc' ? 'otc' : 'tse';

    if (!/^\d{4,6}$/.test(requestedCode)) {
      return res.status(400).json({ error: 'invalid stock code' });
    }

    // The dashboard's index card historically requested 0050, but 0050 is an
    // ETF.  TAIEX is exposed by MIS as t00.tw. Keep 0050 as a compatibility
    // alias for the index card so it displays the actual weighted index.
    const isTaiexAlias = requestedCode === '0050' && market === 'tse';
    const upstreamCode = isTaiexAlias ? 't00' : requestedCode;
    const upstreamUrl = `https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=${market}_${upstreamCode}.tw&json=1`;
    const response = await fetch(upstreamUrl, {
      headers: { Accept: 'application/json' }
    });

    if (!response.ok) {
      return res.status(502).json({ error: 'upstream fetch failed', code: requestedCode, market, status: response.status });
    }

    const data = await response.json();
    const item = Array.isArray(data?.msgArray) ? data.msgArray[0] : null;
    if (!item) return res.status(404).json({ error: 'quote not found', code: requestedCode, market });

    const price = number(item.z || item.p || item.a || item.o || 0);
    const previous = number(item.y || 0);
    const change = previous ? price - previous : number(item.d || 0);

    return res.status(200).json({
      code: requestedCode,
      name: isTaiexAlias ? '加權指數' : (item.n || item.nf || ''),
      market: '上市',
      price,
      previous,
      change,
      changePct: previous ? (change / previous) * 100 : 0,
      open: number(item.o),
      high: number(item.h),
      low: number(item.l),
      close: number(item.z || item.p),
      volume: number(item.v || item.a || item.f || 0),
      time: item.t || new Date().toISOString(),
      delayed: false,
      source: 'TWSE MIS'
    });
  } catch (error) {
    return res.status(500).json({ error: 'server error', detail: String(error?.message || error) });
  }
};

function number(v) {
  const x = Number(String(v ?? '').replace(/,/g, ''));
  return Number.isFinite(x) ? x : 0;
}
