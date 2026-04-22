# Arbitrum Volatility-Event Transaction Analytics

A Spark/Scala pipeline that analyzes on-chain behavior on the Arbitrum (Ethereum L2) around two real volatility events — the **ARB token airdrop** (March 2023) and the **Red Monday flash crash** (August 2024) — to investigate suspicious capital flows via gas/priority-fee expenditure with the goal of identifying malicious actors and/or insider traders. The **FTX collapse & DeFi Flight** (November 2022) event is used as a built-in control: its data is deliberately absent from the S3 download, so every downstream analytic should produce zero/NULL signal for that event.

All code is Scala, targeted at Spark on a Dataproc HPC cluster. Raw data comes from a publicly available AWS S3 bucket of parquet files recording these Ethereum transactions taking place on the Arbitrum layer.

See `data_ingest/how-to-ingest.md` for full instructions on data download, HDFS upload, and Hive setup.

---

## Repository layout

```
arbitrum_ethereum_trans/
├── README.md                    (this file)
├── ana_code/                    primary analytics scripts
├── etl_code/                    data cleaning / transformation scripts
├── profiling_code/              exploratory profiling & record counts
├── test_code/                   test scripts (primarily) and unused / deprecated code
├── data_ingest/                 docs for reproducing the ingest pipeline
└── screenshots/                 Dataproc output screenshots
```

### `ana_code/` — primary analytics

The deliverable analytics that produce the per-event findings.

- **`FirstCode.scala`** — Global statistics per volatility event. Computes mean / stddev / approximate median of `gas_used` and `value` per event window, ranks the top 25 addresses by total gas and by total value (FROM + TO combined), and flags global statistical outliers using a z-score threshold of 3 on `gas_used` or `value`. Reads from the Hive table `arbitrum_db.arbitrum_cleaned`.
- **`SecondCode.scala`** — Front-running / extortionate-bribe detection. Groups the cleaned data by `(event_date, block_number, to_address)`, computes per-group mean/stddev on `max_priority_fee_per_gas_gwei` (the validator tip), and flags any transaction whose per-group tip z-score ≥ 2.0 as a suspected front-runner. Aggregates flags up to the wallet level to surface repeat offenders (MEV bots / insiders / malicious actors) ranked by offense count. Uses `TO_ADDRESS` (not `CONTRACT_ADDRESS`) as the target-contract key — this was a post-first-run correction and is a **major iteration** of this script; the original `CONTRACT_ADDRESS` grouping is kept commented out in Section 6 as an audit trail, and Section 5.5 prints a schema diagnostic that confirms the correct column choice on every run. See **Technical audit** below for the full story.

---

### Technical audit — corrections made during development

Two meaningful course-corrections happened during development. Both are preserved inline in the relevant scripts as commented-out code so the original (incorrect or abandoned) approach stays on the record as an audit trail.

#### 1. Grouping key: `CONTRACT_ADDRESS` → `TO_ADDRESS` pivot

`ana_code/SecondCode.scala` originally grouped transactions by `(event_date, block_number, contract_address)` under the assumption that `CONTRACT_ADDRESS` identified the smart contract being called in each transaction. On the first run this produced only **5,684 raw groups**, **zero** of which had more than one transaction, and every downstream stage (flagged front-runners, repeat offenders) came back empty / all NULL.

Root cause: in the standard ethereum-etl / Arbitrum parquet schema, `CONTRACT_ADDRESS` is populated **only on contract-creation receipts** — i.e. the newly-minted address of a just-deployed contract — *not* on regular calls to an already-deployed contract. The contract being called in a normal transaction lives in `TO_ADDRESS`.

Output from `profiling_code/CountRecsv2.scala` confirms the gap cleanly:

| column             | distinct values | what it actually represents                 |
|--------------------|----------------:|---------------------------------------------|
| `CONTRACT_ADDRESS` |           9,112 | contract **deployment** events, ever        |
| `TO_ADDRESS`       |         988,474 | contracts actually being **interacted with**|

Fix: the grouping key (and the Section 7 join key, and the Section 8 aggregate) was swapped to `TO_ADDRESS`. The original `contract_address`-based grouping is kept commented out in Section 6 of `SecondCode.scala` with an `ITERATION NOTE` header documenting the mistake. Section 5.5 of `SecondCode.scala` prints a per-event schema diagnostic on every run (populated % per column, sample rows) that re-proves the pivot is still the right call in case the upstream S3 dump ever changes shape.

#### 2. Abandoned sandwich-attack analytic on failed transactions

`etl_code/Cleanv2.scala` originally included a filter that dropped rows with `STATUS != 1` (failed transactions). The intent was two-sided: the cleaned dataset would only carry successful txs for the main analytic, while a *separate* sandwich-attack script would mine the failed transactions as potential **victims** — the theory being that an attacker's front-run + back-run pair can squeeze a victim's swap so severely that the victim's own transaction runs out of gas and reverts (`STATUS = 0`).

`test_code/FailedTransacts.scala` was written to profile the failed-tx population. It returned **zero** rows with `STATUS != 1` across the entire cleaned dataset. With no failed transactions in the data, there were no sandwich victims to analyze, and the separate sandwich-attack analytic was never built.

Consequences: (a) the `STATUS == 1` filter in `Cleanv2.scala` was a no-op on this dataset (every row already satisfied it), so it was removed — the cleaner is now simpler and doesn't claim to do something that has no effect; (b) the investigation pivoted to per-block priority-fee z-score analysis on successful transactions (now `ana_code/SecondCode.scala`). The original filter is kept commented out between Sections 4 and 5 of `Cleanv2.scala` with notes documenting both the no-op finding and the scrapped analytic.

---

### `etl_code/` — data cleaning

Scripts that transform raw S3 parquet into the cleaned dataset all downstream analytics read from and Hive table points to.

- **`Clean.scala`** *(v1, kept for reference, not intended for execution)* — Minimal early version of the cleaner. Note: uses `.na.drop()`, which silently discards any row with a null column (including `CONTRACT_ADDRESS`, which is null on ~99.97% of rows). Do not run in production; superseded by `Cleanv2.scala`.
- **`Cleanv2.scala`** *(production)* — The production cleaner. Reduces the raw schema to 11 working columns, casts numeric types safely, lowercases all addresses, normalizes `INPUT = "0x"` / empty strings to null, and deduplicates on `(FROM_ADDRESS, TO_ADDRESS, DATETIME, VALUE, GAS_USED)`. Reads from `hdfs:///user/fjo2015_nyu_edu/arbitrum_sample/` and writes to `hdfs:///user/fjo2015_nyu_edu/arbitrum_sample_cleaned/`. A `STATUS == 1` filter that originally sat between Sections 4 and 5 was removed once profiling showed zero failed transactions in the dataset; the filter is kept commented-out in-place as an audit trail — see **Technical audit** above.

### `profiling_code/` — exploratory profiling

Quick scripts used to get a baseline read on the dataset (record counts, distinct cardinality per key column, schema inspection). Numbers surfaced here were the basis for several architectural decisions downstream (e.g. confirming `CONTRACT_ADDRESS` is sparse vs. `TO_ADDRESS` is dense).

- **`CountRecs.scala`** *(v1)* — Local-filesystem version (`file:///` paths). Useful for iterating on a small parquet sample on a laptop for low-overhead/latency before promoting to the cluster.
- **`CountRecsv2.scala`** *(HDFS-ready)* — Same profile, repointed at the HDFS paths used on Dataproc. Reports total record count plus distinct counts for `CONTRACT_ADDRESS`, `BLOCK_NUMBER`, and `TO_ADDRESS`, among others.

### `test_code/` — tests and unused code

Scratch / one-shot scripts that are not part of the main pipeline but were useful during development. Kept for transparency and potential reuse.

- **`FailedTransacts.scala`** — One-off profiling job that counts failed transactions (`STATUS != 1`) in the cleaned parquet directory. Used once to confirm that failed transactions didn't exist in the data (which could otherwise have provided useful signal for a sandwich-attack analytic). The zero result is what killed the originally-planned sandwich-attack analytic and drove the removal of `Cleanv2.scala`'s `STATUS == 1` filter — see **Technical audit** above.

### `data_ingest/` — ingest documentation

- **`how-to-ingest.md`** — Step-by-step walkthrough of the ingest pipeline: pulling parquet from the public AWS S3 bucket into HDFS on Dataproc, creating the `arbitrum_db` Hive database, and registering the external table that the analytics in `ana_code/` read from.

### `screenshots/`

Reserved for screenshots of Dataproc job output. Referenced by the results section below.

---

## How to build and run the code

All scripts are compiled and executed through the spark-scala shell. See Section 5 of data_ingest/how-to-ingest.md

It is **highly recommended** to run scripts and view terminal outputs on a larger screen/monitor for easiest readability. Otherwise, reference screenshots/.

---

## Where to find results of a run

All results are outputted in spark-scala shell as output of the various scripts.

---

## Where to find the input data that was used

Through the publicly available AWS Public-Blockchain-Data S3 bucket. HDFS access has been provided to the relevant accounts. See data_ingest/how-to-ingest.md for more info on data download and reproducibility.
