import org.apache.spark.sql.SparkSession
spark.conf.set("spark.sql.codegen.wholeStage", "false")
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object Clean {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Clean Arbitrum Data")
      .getOrCreate()

    import spark.implicits._

    val inputPath = "hdfs:///user/fjo2015_nyu_edu/arbitrum_sample/"
    val outputPath = "hdfs:///user/fjo2015_nyu_edu/arbitrum_sample_cleaned/"

    // -------------------------------
    // 1. READ DATA
    // -------------------------------
    val df = spark.read.parquet(inputPath)

    // -------------------------------
    // 2. SELECT ONLY RELEVANT COLUMNS
    // -------------------------------
    val selectedDF = df.select(
      $"MAX_PRIORITY_FEE_PER_GAS_GWEI",
      $"MAX_FEE_PER_GAS_GWEI",
      $"GAS_USED",
      $"VALUE",
      $"FROM_ADDRESS",
      $"TO_ADDRESS",
      $"CONTRACT_ADDRESS",
      $"DATETIME",
      $"BLOCK_NUMBER",
      $"STATUS",
      $"INPUT"
    )

    // -------------------------------
    // 3. TYPE CLEANING + NORMALIZATION
    // -------------------------------
    val cleanedDF = selectedDF

      // VALUE → fix precision
      .withColumn("VALUE",
        col("VALUE").cast("double")
      )

      // GAS_USED → long
      .withColumn("GAS_USED",
        col("GAS_USED").cast("long")
      )

      // Fees → keep as double (fine for analysis)
      .withColumn("MAX_FEE_PER_GAS_GWEI",
        col("MAX_FEE_PER_GAS_GWEI").cast("double")
      )
      .withColumn("MAX_PRIORITY_FEE_PER_GAS_GWEI",
        col("MAX_PRIORITY_FEE_PER_GAS_GWEI").cast("double")
      )

      // BLOCK_NUMBER → long
      .withColumn("BLOCK_NUMBER",
        col("BLOCK_NUMBER").cast("long")
      )

      // DATETIME → ensure long
      .withColumn("DATETIME",
        col("DATETIME").cast("long")
      )

      // STATUS → int
      .withColumn("STATUS",
        col("STATUS").cast("int")
      )

      // Normalize addresses (lowercase)
      .withColumn("FROM_ADDRESS",
        lower(col("FROM_ADDRESS"))
      )
      .withColumn("TO_ADDRESS",
        lower(col("TO_ADDRESS"))
      )
      .withColumn("CONTRACT_ADDRESS",
        lower(col("CONTRACT_ADDRESS"))
      )

      // INPUT cleanup
      .withColumn("INPUT",
        when(col("INPUT").isNull || col("INPUT") === "" || col("INPUT") === "0x", null)
        .otherwise(col("INPUT"))
      )

    // -------------------------------
    // 4. HANDLE MISSING / BAD DATA
    // -------------------------------
    val filteredDF = cleanedDF

      // Drop rows missing critical fields
      .filter(
        col("FROM_ADDRESS").isNotNull &&
        col("TO_ADDRESS").isNotNull &&
        col("DATETIME").isNotNull
      )

      // Optional: remove failed transactions
      .filter(col("STATUS") === 1)

    // -------------------------------
    // 5. REMOVE DUPLICATES
    // -------------------------------
    val dedupedDF = filteredDF.dropDuplicates(
      "FROM_ADDRESS",
      "TO_ADDRESS",
      "DATETIME",
      "VALUE",
      "GAS_USED"
    )

    // -------------------------------
    // 6. WRITE CLEANED DATA
    // -------------------------------
    dedupedDF.write
      .mode("overwrite")
      .parquet(outputPath)

    println(s"Cleaned data written to: $outputPath")

    spark.stop()
  }
}