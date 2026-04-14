import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.types._

object FirstCode {
    def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Arbitrum Event Analytics v1")
      .enableHiveSupport()
      .getOrCreate()

    import spark.implicits._

    // =========================================================
    // 1. LOAD DATA FROM HIVE TABLE (cleaned dataset)
    // =========================================================
    val df = spark.table("arbitrum_db.arbitrum_cleaned")

    // =========================================================
    // 2. DATETIME FORMATTING (CRITICAL FOR EVENT FILTERING)
    // =========================================================
    val dfWithDate = df.withColumn(
      "date",
      to_date(from_unixtime(col("datetime") / 1000))
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
      cleanTypedDF.filter(col("date").isin(dates.map(to_date(lit(_))): _*))

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

        // Median approximation (Spark exact median is expensive)
        val median = df.stat.approxQuantile(c, Array(0.5), 0.0)(0)
        println(s"$c -> median: $median")
      }
    }

    computeStats(airdropDF, "AIRDROP")
    computeStats(flashDF, "FLASH CRASH")
    computeStats(ftxDF, "FTX COLLAPSE")

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