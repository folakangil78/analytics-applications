import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object CountRecs {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Arbitrum Count Records")
      .getOrCreate()

    import spark.implicits._

    // to parquet directory
    val inputPath = "/home/fjo2015_nyu_edu/Arbitrum/arbitrum_sample"

    // 1. READ ALL PARQUET FILES
    val df = spark.read.parquet(inputPath)

    // Select only relevant columns
    val selectedDF = df.select(
      $"MAX_PRIORITY_FEE_PER_GAS_GWEI",
      $"MAX_FEE_PER_GAS_GWEI",
      $"GAS_USED",
      $"FROM_ADDRESS",
      $"TO_ADDRESS",
      $"CONTRACT_ADDRESS",
      $"DATETIME",
      $"BLOCK_NUMBER",
      $"STATUS",
      $"INPUT"
    )

    // 2. COUNT TOTAL RECORDS
    val totalCount = selectedDF.count()
    println(s"Total Records: $totalCount")

    // 3. MAP TO KEY-VALUE PAIRS (RDD)
    val kvRDD = selectedDF.rdd.map(row => {
      val key = (
        row.getAs[String]("FROM_ADDRESS"),
        row.getAs[String]("TO_ADDRESS"),
        row.getAs[String]("CONTRACT_ADDRESS")
      )

    
