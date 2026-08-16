from __future__ import annotations

import csv
import json
from datetime import date, timedelta
from pathlib import Path

SOURCE = Path('/tmp/lbma_gold_am.json')
OUTPUT = Path('app/src/main/assets/lbma_gold_am_usd_daily.csv')
START = date(2025, 8, 16)
END = date(2026, 8, 16)

records = json.loads(SOURCE.read_text())
rows = []
for item in records:
    try:
        day = date.fromisoformat(item['d'])
        price = item['v'][0]
    except (KeyError, TypeError, ValueError, IndexError):
        continue
    if START <= day <= END and isinstance(price, (int, float)):
        rows.append((day.isoformat(), float(price)))
rows.sort()

OUTPUT.parent.mkdir(parents=True, exist_ok=True)
with OUTPUT.open('w', newline='') as handle:
    writer = csv.writer(handle)
    writer.writerow(['date', 'price_usd'])
    writer.writerows(rows)

print(f'wrote {len(rows)} rows to {OUTPUT}')
print(f'first={rows[0] if rows else None} last={rows[-1] if rows else None}')
