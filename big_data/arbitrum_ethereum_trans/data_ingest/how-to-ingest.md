# How to Ingest the Arbitrum Ethereum Transaction Data

End-to-end walkthrough for pulling the raw Arbitrum L2 transaction parquet files from AWS S3 into the Dataproc cluster, landing them in HDFS, running the Spark ETL, and exposing the cleaned result to Spark SQL via a Hive external table for downstream analytics.

> The data can also be queried and downloaded almost entirely through the AWS Athena API. For simplicity, Athena is not used in this walkthrough.

---

## 1. Source data (AWS S3)

Make sure the AWS CLI is installed so you can pull parquet files from the public S3 bucket directly into your terminal instance.

The parquet files in the bucket (under the `transactions` table) are partitioned by date, so you can download all parquet files for a specific day into a new local directory called `arbitrum_sample/`:

```bash
aws s3 cp \
  --no-sign-request \
  --recursive \
  s3://aws-public-blockchain/v1.1/sonarx/arbitrum/transactions/date=YYYY-MM-DD/ \
  ./arbitrum_sample/
```

Run the above command once per day for each of the days listed below (each day is either just before or during one of the chosen volatility events):

```
2023-03-15
2023-03-16
2023-03-23
2023-03-24
2024-08-01
2024-08-04
2024-08-05
```

Check the size of the directory at any point with:

```bash
du -sh arbitrum_sample/
```

If you downloaded data from exactly those days correctly, the size-check should return roughly **5.4 GB**.

You can inspect an individual parquet's schema with `parquet-tools`:

```bash
pip install parquet-tools
parquet-tools schema file.parquet
# NOTE: `schema` may be deprecated in newer versions; if so use `show` or `inspect` instead.
```

> **Do NOT run** `aws s3 cp --recursive s3://aws-public-blockchain/arbitrum/... .` (as suggested in various StackOverflow threads) — it will attempt to pull the entire 10 TB+ bucket and will almost certainly crash your environment.

---

## 2. Transferring parquets from local FS into HDFS

Assumes you have a local directory `arbitrum_sample/` populated from Section 1.

Enter the directory and push the parquets into HDFS:

```bash
cd arbitrum_sample/
hdfs dfs -put *.parquet
```

This inserts all the parquet files (~60) from the selected volatility days into your HDFS home directory. Create a dedicated HDFS directory to mirror the local layout and move the files into it:

```bash
hdfs dfs -mkdir -p /user/<username>/arbitrum_sample
```

(Replace `<username>` with your own Dataproc user)

---

## 3. Running the Spark jobs against the ingested data

The Scala scripts themselves do not need to live in HDFS to execute — they can be loaded straight from the local filesystem into the Spark shell.

Launch the shell:

```bash
spark-shell --deploy-mode client
```

Inside the shell, load and run a script:

```scala
scala> :load <filename>.scala
scala> <ObjectName>.main(Array())
```

> `<ObjectName>` is the `object` defined inside the script and will NOT include any `v2` suffix — e.g. `Cleanv2.scala` defines `object Clean`, so you call `Clean.main(Array())`.

### Recommended run order on the Dataproc cluster

Execute the scripts in this order. **Pause between step 2 and step 3** to create the Hive database and external table (Section 4 below) — steps 3 and 4 read from the Hive table, not from HDFS directly.

1. **`profiling_code/CountRecsv2.scala`** — runs on the raw `arbitrum_sample/` data; gives an initial profile of the parquet transaction data (row count, distinct cardinality per key column).
2. **`etl_code/Cleanv2.scala`** — full ETL pipeline: drops unused columns, normalizes `DATETIME`, casts values, lowercases addresses, deduplicates. Output is written to the new HDFS directory `arbitrum_sample_cleaned/`.

> → Now jump to **Section 4** to create the Hive database and external table on top of `arbitrum_sample_cleaned/`, then come back here for steps 3 and 4.

3. **`ana_code/FirstCode.scala`** — basic summary statistics and global outlier/anomaly analysis; reads from the Hive external table.
4. **`ana_code/SecondCode.scala`** — primary analytic: surfaces malicious actors / insider traders by investigating per-block priority-fee expenditure around the chosen high-volatility events.

It is **highly recommended** to run scripts and view terminal outputs on a larger screen/monitor for easiest readability. Otherwise, reference screenshots/.

---

## 4. Creating the Hive database and external table

This section assumes the cleaned parquets already exist in HDFS at `arbitrum_sample_cleaned/` — i.e. that you have run `etl_code/Cleanv2.scala` from Section 3. If you have not, do that first.

Create the Hive database's backing directory in HDFS:

```bash
hdfs dfs -mkdir -p /user/<username>/arbitrum_hive_db
```

Enter the Hive shell:

```bash
hive
```

Create the database and the external table that points at the cleaned parquet directory:

```sql
hive> CREATE DATABASE arbitrum_db
    > LOCATION 'hdfs:///user/<username>/arbitrum_hive_db';

hive> CREATE EXTERNAL TABLE arbitrum_db.arbitrum_cleaned (
    >   value                         DOUBLE,
    >   gas_used                      BIGINT,
    >   from_address                  STRING,
    >   to_address                    STRING,
    >   contract_address              STRING,
    >   datetime                      BIGINT,
    >   block_number                  BIGINT,
    >   status                        INT,
    >   input                         STRING,
    >   max_fee_per_gas_gwei          DOUBLE,
    >   max_priority_fee_per_gas_gwei DOUBLE
    > )
    > STORED AS PARQUET
    > LOCATION 'hdfs:///user/<username>/arbitrum_sample_cleaned/';
```

That creates a Hive database inside `hive_db/` in HDFS and an external table pointing at the cleaned data in `arbitrum_sample_cleaned/`.

> **Important:** the Hive database's location (`arbitrum_hive_db/`) and the external table's location (`arbitrum_sample_cleaned/`) are NOT the same thing. The database location is for Hive metadata + optional storage; the external table's `LOCATION` clause points directly at the cleaned parquet dataset. The scripts themselves don't need to be in HDFS to execute.

---

## 5. Verifying the ingest

Verify the cleaned parquet content (schema should differ from an unprocessed `arbitrum_sample/` parquet):

```bash
hdfs dfs -get <hdfs_src> <local_dest>
parquet-tools inspect file.parquet
```

Verify the Hive external table from inside the Hive shell:

```sql
hive> DESCRIBE arbitrum_db.arbitrum_cleaned;
```
