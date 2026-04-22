import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.types._

// NOTE: THE PARQUET DATA DOES NOT INCLUDE ANYTHING FROM THE FTX COLLAPSE/DEFI FLIGHT
// VOLATILITY EVENT IN NOVEMBER 2022; THAT EVENT IS USED AS A CONTROL
// TO MAKE SURE THE CALCULATIONS FOR OTHER TWO VOLATILTY EVENTS
// (ARB TOKEN AIRDROP AND RED MONDAY FLASH CRASH) ARE ACCURATE.
// FTX COLLAPSE MEASUREMENTS SHOULD BE 0/EMPTY, FTX DATA ISN'T DOWNLOADED FROM BUCKET.

object FirstCode {
    def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Arbitrum Event Analytics v1")
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

    // =========================================================
    // 5. EVENT WINDOWS
    // =========================================================
    val airdropDates = Seq("2023-03-15", "2023-03-16", "2023-03-23", "2023-03-24")
    val flashCrashDates = Seq("2024-08-01", "2024-08-04", "2024-08-05")
    val ftxDates = Seq("2022-11-06", "2022-11-08", "2022-11-10")

    def filterEvent(dates: Seq[String]) =
        cleanTypedDF.filter(to_date(col("datetime_ts")).isin(dates: _*))


    val airdropDF = filterEvent(airdropDates)
    val flashDF = filterEvent(flashCrashDates)
    val ftxDF = filterEvent(ftxDates)

    // =========================================================
    // 6. GLOBAL STATISTICS FUNCTION
    // =========================================================
    def computeStats(df: org.apache.spark.sql.DataFrame, label: String): Unit = {

      println(s"\n==================== $label STATS ====================")

      val cols = Seq("gas_used", "value")

      cols.foreach { c =>

        val stats = df.select(
          mean(col(c)).as("mean"),
          stddev(col(c)).as("stddev")
        ).collect()(0)

        val meanVal = stats.getAs[Double]("mean")
        val stdVal  = stats.getAs[Double]("stddev")

        println(s"$c -> mean: $meanVal | stddev: $stdVal")

        // True median approximation (1% relative error to prevent mem leak)
        val quantiles = df.stat.approxQuantile(c, Array(0.5), 0.01)

        val median =
            if (quantiles.nonEmpty) quantiles(0)
            else Double.NaN

        println(s"$c -> median: $median")
      }
    }

    computeStats(airdropDF, "ARB TOKEN AIRDROP (March 2023) --")
    computeStats(flashDF, "RED MONDAY FLASH CRASH (August 2024) --")
    computeStats(ftxDF, "FTX COLLAPSE & DEFI FLIGHT (November 2022) --")

    // =========================================================
    // 7. TOP 25 ADDRESSES BY GAS (FROM + TO COMBINED)
    // =========================================================
    def topAddressesByGas(df: org.apache.spark.sql.DataFrame, label: String): Unit = {

      println(s"\n==================== $label TOP 25 GAS ADDRESSES ====================")

      val fromAgg = df.groupBy("from_address")
        .agg(sum("gas_used").as("total_gas"))

      val toAgg = df.groupBy("to_address")
        .agg(sum("gas_used").as("total_gas"))

      val combined = fromAgg.union(toAgg)
        .groupBy("from_address")
        .agg(sum("total_gas").as("total_gas"))
        .orderBy(desc("total_gas"))
        .limit(25)

      combined.show(false)
    }

    // =========================================================
    // 8. TOP 25 ADDRESSES BY VALUE
    // =========================================================
    def topAddressesByValue(df: org.apache.spark.sql.DataFrame, label: String): Unit = {

      println(s"\n==================== $label TOP 25 VALUE ADDRESSES ====================")

      val fromAgg = df.groupBy("from_address")
        .agg(sum("value").as("total_value"))

      val toAgg = df.groupBy("to_address")
        .agg(sum("value").as("total_value"))

      val combined = fromAgg.union(toAgg)
        .groupBy("from_address")
        .agg(sum("total_value").as("total_value"))
        .orderBy(desc("total_value"))
        .limit(25)

      combined.show(false)
    }

    topAddressesByGas(airdropDF, "ARB TOKEN AIRDROP (March 2023) --")
    topAddressesByGas(flashDF, "RED MONDAY FLASH CRASH (August 2024) --")
    topAddressesByGas(ftxDF, "FTX COLLAPSE & DEFI FLIGHT (November 2022) --")

    topAddressesByValue(airdropDF, "ARB TOKEN AIRDROP (March 2023) --")
    topAddressesByValue(flashDF, "RED MONDAY FLASH CRASH (August 2024) --")
    topAddressesByValue(ftxDF, "FTX COLLAPSE & DEFI FLIGHT (November 2022) --")

    // =========================================================
    // 9. STATISTICAL OUTLIERS (Z-SCORE)
    // =========================================================
    def detectOutliers(df: org.apache.spark.sql.DataFrame, label: String): Unit = {

      println(s"\n==================== $label OUTLIERS ====================")

      val stats = df.select(
        mean("gas_used").as("mean_gas"),
        stddev("gas_used").as("std_gas"),
        mean("value").as("mean_val"),
        stddev("value").as("std_val")
      ).collect()(0)

      val meanGas = stats.getAs[Double]("mean_gas")
      val stdGas  = stats.getAs[Double]("std_gas")
      val meanVal = stats.getAs[Double]("mean_val")
      val stdVal  = stats.getAs[Double]("std_val")

      val withZ = df.withColumn("z_gas",
          (col("gas_used") - meanGas) / stdGas
        )
        .withColumn("z_value",
          (col("value") - meanVal) / stdVal
        )

      val outliers = withZ.filter(
        abs(col("z_gas")) >= 3 || abs(col("z_value")) >= 3
      )

      println(s"Total outliers: ${outliers.count()}")

      outliers.select(
        "from_address",
        "to_address",
        "gas_used",
        "value",
        "datetime_ts",
        "z_gas",
        "z_value"
      ).show(25, false)
    }

    detectOutliers(airdropDF, "ARB TOKEN AIRDROP (March 2023) --")
    detectOutliers(flashDF, "RED MONDAY FLASH CRASH (August 2024) --")
    detectOutliers(ftxDF, "FTX COLLAPSE & DEFI FLIGHT (November 2022) --")

    spark.stop()
  }
}