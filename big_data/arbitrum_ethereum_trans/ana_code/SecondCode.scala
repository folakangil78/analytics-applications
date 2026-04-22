import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.types._

// NOTE: THE PARQUET DATA DOES NOT INCLUDE ANYTHING FROM THE FTX COLLAPSE/DEFI FLIGHT
// VOLATILITY EVENT IN NOVEMBER 2022; THAT EVENT IS USED AS A CONTROL
// TO MAKE SURE THE CALCULATIONS FOR OTHER TWO VOLATILTY EVENTS
// (ARB TOKEN AIRDROP AND RED MONDAY FLASH CRASH) ARE ACCURATE.
// FTX COLLAPSE MEASUREMENTS SHOULD BE 0/EMPTY, FTX DATA ISN'T DOWNLOADED FROM BUCKET.

// THIS SCRIPT:
// Investigate insider trading / front-running by hunting for "extortionate bribes"
// paid to validators via MAX_PRIORITY_FEE_PER_GAS_GWEI (the validator tip).
// A front-runner wins transaction ordering inside a block by out-bidding other
// traders on the priority fee -- so the signal we're looking for is a SINGLE
// (block_number, to_address) pair where multiple addresses competed to call
// the same contract, and one address paid an abnormally high tip relative
// to the rest of that bucket.
//
// We use TO_ADDRESS (not CONTRACT_ADDRESS anymore) to identify the contract being
// targeted: in the ethereum-etl / Arbitrum parquet schema CONTRACT_ADDRESS
// is only populated for contract-deployment receipts, while TO_ADDRESS is
// the actual contract being called by swaps/bridges/mints. Section 5.5
// prints a schema diagnostic that confirms this in the run logs.

// Normalization, date formatting, and casting are repeated in case scripts are
// executed out of order.

object SecondCode {
    def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Arbitrum Front-Running Analytics v1")
      .enableHiveSupport()
      .getOrCreate()

    import spark.implicits._

    // =========================================================
    // 1. LOAD DATA FROM HIVE TABLE (cleaned data)
    // =========================================================
    val df = spark.table("arbitrum_db.arbitrum_cleaned")

    // =========================================================
    // 2. DATETIME FORMATTING (EVENT FILTERING)
    // =========================================================

    // Convert the Unix epoch timestamp (Long in seconds) to a full timestamp
    // (preserves both date and exact time of day, e.g. 2023-03-15 14:32:07)
    val dfWithDate = df.withColumn(
        "datetime_ts",
        col("DATETIME").cast("timestamp")
    )

    // =========================================================
    // 3. SAFE TEXT NORMALIZATION (ONLY ADDRESSES)
    // =========================================================
    val normalizedDF = dfWithDate
      .withColumn("from_address", lower(trim(col("from_address"))))
      .withColumn("to_address", lower(trim(col("to_address"))))
      .withColumn("contract_address", lower(trim(col("contract_address"))))

    // =========================================================
    // 4. CAST NUMERIC FIELDS SAFELY
    // =========================================================
    val cleanTypedDF = normalizedDF
      .withColumn("gas_used", col("gas_used").cast("double"))
      .withColumn("value", col("value").cast("double"))
      .withColumn("max_fee_per_gas_gwei", col("max_fee_per_gas_gwei").cast("double"))
      .withColumn("max_priority_fee_per_gas_gwei", col("max_priority_fee_per_gas_gwei").cast("double"))
      .withColumn("block_number", col("block_number").cast("long"))

    // Attach the pure calendar date -- this is the "event bucket" key we group on
    // (keeps day-level granularity without losing per-tx timestamp resolution)
    val datedDF = cleanTypedDF.withColumn(
        "event_date",
        to_date(col("datetime_ts"))
    )

    // =========================================================
    // 5. EVENT WINDOWS
    // =========================================================
    val airdropDates    = Seq("2023-03-15", "2023-03-16", "2023-03-23", "2023-03-24")
    val flashCrashDates = Seq("2024-08-01", "2024-08-04", "2024-08-05")
    val ftxDates        = Seq("2022-11-06", "2022-11-08", "2022-11-10")

    def filterEvent(dates: Seq[String]) =
        datedDF.filter(col("event_date").isin(dates: _*))

    val airdropDF = filterEvent(airdropDates)
    val flashDF   = filterEvent(flashCrashDates)
    val ftxDF     = filterEvent(ftxDates)

    // =========================================================
    // 5.5. SCHEMA DIAGNOSTIC (CONTRACT_ADDRESS vs TO_ADDRESS)
    // =========================================================
    // Sanity-check the shape of CONTRACT_ADDRESS vs TO_ADDRESS on each event
    // DataFrame BEFORE we group on either one. In the standard ethereum-etl
    // parquet schema, CONTRACT_ADDRESS is populated ONLY on contract CREATION
    // transactions (the deploy receipt's newly minted address), while
    // TO_ADDRESS holds the address a regular call is sent to (i.e. the
    // contract being used by swaps, bridges, mints). If CONTRACT_ADDRESS is
    // almost always null and TO_ADDRESS is almost always populated, that
    // confirms why grouping on TO_ADDRESS is the correct key for "multiple
    // wallets fighting over the same contract in the same block."
    //
    // Doubles as ongoing schema-sanity output in case the upstream S3 dump
    // changes or a new event window exposes different data characteristics.
    def schemaDiagnostic(df: org.apache.spark.sql.DataFrame, label: String): Unit = {

      println(s"\n==================== $label SCHEMA DIAGNOSTIC ====================")

      val totalRows        = df.count()
      val contractPop      = df.filter(col("contract_address").isNotNull).count()
      val contractDistinct = df.filter(col("contract_address").isNotNull)
                               .select("contract_address").distinct().count()
      val toPop            = df.filter(col("to_address").isNotNull).count()
      val toDistinct       = df.filter(col("to_address").isNotNull)
                               .select("to_address").distinct().count()

      // Guard against zero-row event DFs (e.g. FTX control) before dividing.
      val contractPct = if (totalRows > 0) (contractPop.toDouble / totalRows) * 100.0 else 0.0
      val toPct       = if (totalRows > 0) (toPop.toDouble      / totalRows) * 100.0 else 0.0

      println(f"total_rows:              $totalRows")
      println(f"contract_addr_populated: $contractPop  ($contractPct%.4f%% of rows)")
      println(f"contract_addr_distinct:  $contractDistinct")
      println(f"to_addr_populated:       $toPop  ($toPct%.4f%% of rows)")
      println(f"to_addr_distinct:        $toDistinct")

      // Sample the deploy-receipt rows so you can visually confirm they look
      // like contract creations (large INPUT bytecode, null TO_ADDRESS, etc.)
      // rather than regular calls.
      if (contractPop > 0) {
        println(s"\n-- Sample rows where contract_address IS NOT NULL (expect deployment receipts) --")
        df.filter(col("contract_address").isNotNull)
          .select("block_number", "from_address", "to_address", "contract_address", "input")
          .show(5, false)
      }
    }

    schemaDiagnostic(airdropDF, "ARB TOKEN AIRDROP (March 2023) --")
    schemaDiagnostic(flashDF,   "RED MONDAY FLASH CRASH (August 2024) --")
    schemaDiagnostic(ftxDF,     "FTX COLLAPSE & DEFI FLIGHT (November 2022) --")

    // =========================================================
    // 6. CONTESTED-BLOCK GROUPING
    // =========================================================
    // A "contested bucket" = (event_date, block_number, to_address) where
    // more than one successful transaction landed in the same block targeting
    // the same smart contract (TO_ADDRESS = the contract being called).
    // That is the only situation where front-running via a priority-fee
    // bribe is mechanically possible -- you cannot front-run yourself, and
    // you cannot front-run across blocks.
    //
    // For each bucket we also surface bribe-related summary stats so the
    // next stage of the script can rank the "most extortionate" buckets.
    def contestedBuckets(df: org.apache.spark.sql.DataFrame, label: String): org.apache.spark.sql.DataFrame = {

      println(s"\n==================== $label CONTESTED BLOCK / TARGET-CONTRACT BUCKETS ====================")

      // ---------------------------------------------------------------
      // ITERATION NOTE -- CONTRACT_ADDRESS -> TO_ADDRESS PIVOT
      // ---------------------------------------------------------------
      // The FIRST version of this analytic grouped by CONTRACT_ADDRESS
      // under the (wrong) assumption that CONTRACT_ADDRESS identified
      // the smart contract being called in each transaction. In the
      // ethereum-etl / Arbitrum parquet schema CONTRACT_ADDRESS is
      // actually populated ONLY on contract-CREATION receipts (the
      // newly-minted address of a just-deployed contract), NOT on
      // regular calls to an already-deployed contract. TO_ADDRESS is
      // the field that carries the contract being CALLED in swaps,
      // bridges, mints, etc. -- i.e. what this analytic actually wants.
      //
      // Symptoms that surfaced the bug on the first run:
      //   Raw block+contract_address groups:   5684   (not ~16.7M)
      //   -> with more than 1 tx:                 0
      //   every downstream stage came back empty / all NULL.
      //
      // profiling_code/CountRecsv2.scala quantified the gap:
      //   contract_address distinct: 9,112     (deploy events, ever)
      //   to_address       distinct: 988,474   (contracts actually used)
      //
      // The live code below reflects the fix (groups on TO_ADDRESS).
      // The commented block beneath it is the ORIGINAL grouping call,
      // kept verbatim as an audit trail of the correction. Section 5.5
      // above prints a schema diagnostic that confirms the fix on every
      // subsequent run.
      // ---------------------------------------------------------------
      val grouped = df
        .filter(col("to_address").isNotNull)
        .filter(col("block_number").isNotNull)
        .groupBy("event_date", "block_number", "to_address")
        .agg(
          count(lit(1)).as("tx_count"),
          countDistinct("from_address").as("distinct_senders"),
          max("max_priority_fee_per_gas_gwei").as("max_tip_gwei"),
          min("max_priority_fee_per_gas_gwei").as("min_tip_gwei"),
          avg("max_priority_fee_per_gas_gwei").as("avg_tip_gwei"),
          stddev("max_priority_fee_per_gas_gwei").as("stddev_tip_gwei")
        )
        .cache() // reused for multiple progression summaries below

      // ---- ORIGINAL (buggy) grouping, replaced after first run ------
      // val grouped = df
      //   .filter(col("contract_address").isNotNull)
      //   .filter(col("block_number").isNotNull)
      //   .groupBy("event_date", "block_number", "contract_address")
      //   .agg(
      //     count(lit(1)).as("tx_count"),
      //     countDistinct("from_address").as("distinct_senders"),
      //     max("max_priority_fee_per_gas_gwei").as("max_tip_gwei"),
      //     min("max_priority_fee_per_gas_gwei").as("min_tip_gwei"),
      //     avg("max_priority_fee_per_gas_gwei").as("avg_tip_gwei"),
      //     stddev("max_priority_fee_per_gas_gwei").as("stddev_tip_gwei")
      //   )
      // ---------------------------------------------------------------

      // Drop any bucket with <= 1 transaction -- front-running requires a victim.
      // Also require > 1 distinct sender, otherwise the "competition" is just
      // one address batching its own txs into the same block.
      val contested = grouped
        .filter(col("tx_count") > 1)
        .filter(col("distinct_senders") > 1)
        .orderBy(desc("max_tip_gwei"))
        .cache()

      // ---------- PROGRESSION: funnel of groups that survive each filter ----------
      val totalGroups    = grouped.count()
      val multiTxGroups  = grouped.filter(col("tx_count") > 1).count()
      val contestedCount = contested.count()

      println(s"Raw block+to_address groups:           $totalGroups")
      println(s"  -> with more than 1 tx:              $multiTxGroups   (dropped ${totalGroups - multiTxGroups} single-tx groups)")
      println(s"  -> with more than 1 distinct sender: $contestedCount   (dropped ${multiTxGroups - contestedCount} self-batched groups)")

      // ---------- PROGRESSION: shape of surviving contested buckets ----------
      println(s"\n-- Distribution of contested-bucket summary stats --")
      contested
        .describe("tx_count", "distinct_senders", "avg_tip_gwei", "max_tip_gwei", "stddev_tip_gwei")
        .show(false)

      println(s"\n-- Top 25 contested buckets by max tip paid --")
      contested.show(25, false)

      contested
    }

    val airdropContested = contestedBuckets(airdropDF, "ARB TOKEN AIRDROP (March 2023) --")
    val flashContested   = contestedBuckets(flashDF,   "RED MONDAY FLASH CRASH (August 2024) --")
    val ftxContested     = contestedBuckets(ftxDF,     "FTX COLLAPSE & DEFI FLIGHT (November 2022) --")

    // =========================================================
    // 7. FLAG SUSPECTED FRONT-RUNNERS (PER-GROUP Z-SCORE ON TIP)
    // =========================================================
    // For every transaction inside a contested bucket, I get a z-score for
    // MAX_PRIORITY_FEE_PER_GAS_GWEI using that bucket's OWN mean/stddev
    // (already produced by contestedBuckets). A from_address whose tip is
    // many stddevs above its peers in the SAME block-contract pair is
    // paying an "extortionate bribe" to jump the queue: front-runner signal.
    //
    // Threshold note:
    //   Since z-score is mathematically bounded by sqrt(n-1) and most block+contract
    //   buckets on Arbitrum carry only 2-5 txs.
    //   So I establish z-threshold at 2.0 (reachable at n >= 5) and ALSO enforce a minimum
    //   bucket size so the stddev we divide by isn't noise from 2-3 rows.
    val Z_THRESHOLD     = 2.0
    val MIN_GROUP_SIZE  = 5

    def flagFrontRunners(
        df:        org.apache.spark.sql.DataFrame,
        contested: org.apache.spark.sql.DataFrame,
        label:     String
    ): org.apache.spark.sql.DataFrame = {

      println(s"\n==================== $label FLAGGED FRONT-RUNNERS (z >= $Z_THRESHOLD, n >= $MIN_GROUP_SIZE) ====================")

      // Only keep buckets with enough transactions for stddev to be stable
      // AND for the z-score threshold to even be mathematically reachable.
      val sizeQualified = contested.filter(col("tx_count") >= MIN_GROUP_SIZE).cache()

      // Join per-tx rows back with their bucket-level stats so every tx
      // carries its group's mean/stddev alongside its own priority fee.
      // Grouping key uses TO_ADDRESS -- the contract being called -- for the
      // same schema reason documented in Section 6.
      val joined = df
        .filter(col("to_address").isNotNull)
        .filter(col("block_number").isNotNull)
        .join(
          sizeQualified,
          Seq("event_date", "block_number", "to_address"),
          "inner"
        )

      // Per-tx z-score = (this tip - bucket mean tip) / bucket stddev tip.
      // Guard against null/zero stddev (flat groups) so we don't divide by 0.
      val withZ = joined.withColumn(
        "tip_z_score",
        when(col("stddev_tip_gwei").isNull || col("stddev_tip_gwei") === 0.0, lit(0.0))
          .otherwise(
            (col("max_priority_fee_per_gas_gwei") - col("avg_tip_gwei")) / col("stddev_tip_gwei")
          )
      ).cache()

      // A front-runner is an address whose tip sits >= Z_THRESHOLD stddevs
      // above the rest of its block-contract peers. We select the columns
      // needed to manually audit each flag (addresses, block, tip paid,
      // what the bucket looked like around them).
      // NOTE: contract_address dropped from the projection -- after the
      // pivot it is null on virtually every row (it was only non-null for
      // contract DEPLOY receipts, which aren't in scope here). The contract
      // being targeted is now carried by to_address.
      val flagged = withZ
        .filter(col("tip_z_score") >= Z_THRESHOLD)
        .select(
          col("event_date"),
          col("block_number"),
          col("to_address").as("target_contract"),
          col("from_address"),
          col("max_priority_fee_per_gas_gwei").as("tip_gwei"),
          col("avg_tip_gwei"),
          col("stddev_tip_gwei"),
          col("tip_z_score"),
          col("tx_count"),
          col("distinct_senders")
        )
        .orderBy(desc("tip_z_score"))
        .cache()

      // ---------- PROGRESSION: how much of the contested set made it into the z-score pipeline ----------
      val sizeQualifiedCount = sizeQualified.count()
      val totalTxInScope     = withZ.count()
      val flaggedCount       = flagged.count()
      val bucketsWithFlag    = flagged
        .select("event_date", "block_number", "target_contract")
        .distinct()
        .count()

      println(s"Size-qualified buckets (tx_count >= $MIN_GROUP_SIZE): $sizeQualifiedCount")
      println(s"Total transactions inside those buckets:          $totalTxInScope")
      println(s"Transactions flagged as front-runners (z >= $Z_THRESHOLD): $flaggedCount")
      println(s"Unique buckets containing >=1 flagged tx:         $bucketsWithFlag / $sizeQualifiedCount")

      // ---------- PROGRESSION: shape of z-scores across ALL in-scope txs (not just flagged) ----------
      println(s"\n-- Distribution of tip z-scores and tips across all in-scope txs --")
      withZ
        .describe("tip_z_score", "max_priority_fee_per_gas_gwei", "avg_tip_gwei", "stddev_tip_gwei")
        .show(false)

      // ---------- PROGRESSION: shape of the FLAGGED subset specifically ----------
      if (flaggedCount > 0) {
        println(s"\n-- Distribution of tip/z-score among FLAGGED txs only --")
        flagged
          .describe("tip_gwei", "avg_tip_gwei", "stddev_tip_gwei", "tip_z_score", "tx_count")
          .show(false)
      }

      println(s"\n-- Top 50 flagged front-runner transactions (sorted by z-score) --")
      flagged.show(50, false)

      flagged
    }

    val airdropFlags = flagFrontRunners(airdropDF, airdropContested, "ARB TOKEN AIRDROP (March 2023) --")
    val flashFlags   = flagFrontRunners(flashDF,   flashContested,   "RED MONDAY FLASH CRASH (August 2024) --")
    val ftxFlags     = flagFrontRunners(ftxDF,     ftxContested,     "FTX COLLAPSE & DEFI FLIGHT (November 2022) --")

    // =========================================================
    // 8. REPEAT OFFENDERS (AGGREGATE FLAGS BY FROM_ADDRESS)
    // =========================================================
    // A single flagged transaction could just be a one-off spike. The real
    // signal of an MEV bot / insider / malicious actor is an address that
    // repeatedly shows up as the top bidder across many different blocks
    // and contracts during the volatility event. We collapse the per-tx
    // flags down to the wallet level and rank by offense count.
    //
    // Output columns per suspect address:
    //   offense_count      -- # of flagged txs (main ranking key)
    //   distinct_blocks    -- # of distinct blocks they paid extortionate tips in
    //   distinct_contracts -- # of distinct contracts they front-ran
    //   max_z_score        -- their single most extreme tip vs its peers
    //   avg_z_score        -- their typical "how many stddevs above peers"
    //   total_tip_gwei     -- cumulative bribe paid across flagged txs
    //   avg_tip_gwei       -- average bribe size across flagged txs
    def repeatOffenders(flagged: org.apache.spark.sql.DataFrame, label: String): org.apache.spark.sql.DataFrame = {

      println(s"\n==================== $label REPEAT OFFENDERS (RANKED BY OFFENSE COUNT) ====================")

      val offenders = flagged
        .groupBy("from_address")
        .agg(
          count(lit(1)).as("offense_count"),
          countDistinct("block_number").as("distinct_blocks"),
          countDistinct("target_contract").as("distinct_contracts"),
          max("tip_z_score").as("max_z_score"),
          avg("tip_z_score").as("avg_z_score"),
          sum("tip_gwei").as("total_tip_gwei"),
          avg("tip_gwei").as("avg_tip_gwei")
        )
        // Primary sort: who offended most often.
        // Tiebreaker: who paid the most extortionate tip on average.
        .orderBy(desc("offense_count"), desc("avg_z_score"))
        .cache()

      // ---------- PROGRESSION: scope of the offender set ----------
      val totalOffenders = offenders.count()
      val repeaters      = offenders.filter(col("offense_count") > 1).count()

      println(s"Unique addresses with >=1 flagged tx: $totalOffenders")
      println(s"Addresses with more than 1 offense:   $repeaters   (true repeat offenders)")

      // ---------- PROGRESSION: distribution of offense behavior across suspects ----------
      if (totalOffenders > 0) {
        println(s"\n-- Distribution of offense_count / z-scores / tips across all suspects --")
        offenders
          .describe("offense_count", "distinct_blocks", "distinct_contracts", "max_z_score", "avg_z_score", "avg_tip_gwei")
          .show(false)
      }

      // ---------- FINAL OUTPUT: ranked suspect wallets ----------
      println(s"\n-- Top 50 repeat offenders (ranked by offense_count, then avg_z_score) --")
      offenders.show(50, false)

      offenders
    }

    val airdropOffenders = repeatOffenders(airdropFlags, "ARB TOKEN AIRDROP (March 2023) --")
    val flashOffenders   = repeatOffenders(flashFlags,   "RED MONDAY FLASH CRASH (August 2024) --")
    val ftxOffenders     = repeatOffenders(ftxFlags,     "FTX COLLAPSE & DEFI FLIGHT (November 2022) --")

    // Final output persists as airdropOffenders / flashOffenders / ftxOffenders --
    // these are the ranked suspect wallets (MEV bots, insiders, malicious actors)
    // that repeatedly paid extortionate priority-fee bribes during each volatility
    // event. FTX should come back essentially empty (control).

    spark.stop()
  }
}
