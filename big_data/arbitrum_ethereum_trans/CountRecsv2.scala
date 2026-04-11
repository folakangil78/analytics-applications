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
    
