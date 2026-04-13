import org.apache.spark.sql.SparkSession
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