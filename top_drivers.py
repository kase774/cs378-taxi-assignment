from collections import defaultdict

BIG_CSV = "taxi-data-sorted-large-n.csv"
SMALL_CSV = "taxi-data-sorted-small.csv"
CSV = SMALL_CSV

CAR, DRIVER = 0, 1
FARE, SURCHARGE, MTA_TAX, TIP, TOLLS, TOTAL = 11, 12, 13, 14, 15, 16
CENTS = 100
MAX_TOTAL_CENTS = 50000
TOP_K = 10

cars = defaultdict(set)
cents = defaultdict(int)

for row in open(CSV):
    p = row.rstrip("\n").split(",")
    try:
        fare, sur, mta, tip, tolls, total = (round(float(p[i]) * CENTS) for i in range(FARE, TOTAL + 1))
    except (ValueError, IndexError):
        continue
    if fare + sur + mta + tip + tolls != total or total >= MAX_TOTAL_CENTS:
        continue
    cars[p[DRIVER]].add(p[CAR])
    cents[p[DRIVER]] += total

for driver, total in sorted(cents.items(), key=lambda kv: kv[1], reverse=True)[:TOP_K]:
    print(f"({driver}, {len(cars[driver])}, {total // CENTS}.{total % CENTS:02d})")
