# Data source findings

## Gold API
- Official docs: https://gold-api.com/docs
- Current endpoint: `GET https://api.gold-api.com/price/XAU` (existing app usage).
- Historical endpoint: `GET https://api.gold-api.com/history`.
- Required query parameters: `symbol=XAU`, `startTimestamp`, `endTimestamp`; optional `groupBy=year|month|week|day|hour`, `aggregation=max|min|avg`, `orderBy=asc|desc`.
- Historical endpoint requires an `x-api-key` header and is rate-limited to 10 requests/hour on the free tier. Hourly grouping is premium-only according to the docs, so this cannot be embedded in a public APK without a protected API key.
- Docs claim provider fallback for real-time data but do not make the historical endpoint key-free.

## LBMA
- Official page: https://www.lbma.org.uk/prices-and-data/lbma-precious-metal-prices
- LBMA describes international gold prices and provides a public interactive chart with 1m, 3m, 6m, YTD, 1y and All ranges, including USD and AM/PM selections.
- The page is authoritative for benchmark context, but the public page is dynamically rendered and does not expose a documented anonymous JSON endpoint suitable for direct Android integration.

## Implementation implication
- Do not ship a private Gold API key inside the APK.
- Use a checked-in, attributed one-year daily historical seed dataset sourced from a public authoritative historical download/API, then merge it with live hourly snapshots from the existing current endpoint. Future points remain locally stored by WorkManager.
- If a live historical endpoint is required later, put the API key behind a small server/proxy rather than embedding it in the Android client.

## FRED verification update
The requested FRED series URL currently returns 404. The St. Louis Federal Reserve announcement at https://news.research.stlouisfed.org/2022/01/ice-benchmark-administration-ltd-iba-data-to-be-removed-from-fred/ states that ICE Benchmark Administration data, including LBMA daily gold prices, was removed from FRED services from January 31, 2022. Therefore the FRED series must not be used as a live or newly downloaded seed source.

## LBMA JSON feed verification
- Official chart page loads `https://prices.lbma.org.uk/json/limited/gold_am.json` for a recent limited window and `https://prices.lbma.org.uk/json/gold_am.json` for the full historical series.
- The full `gold_am.json` endpoint returned HTTP 200 and about 923 KB, beginning in 1968. Each item has `d` (ISO date) and `v` (value array); the first value is the USD AM gold benchmark used by the official chart.
- The full LBMA JSON feed is the selected historical source. The app will bundle only the last year extracted from this official feed, then append live gold-api.com snapshots for current/future points.
