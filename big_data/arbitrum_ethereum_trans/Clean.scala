import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object Clean {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Clean Arbitrum Data")
      .getOrCreate()

    val inputPath = "arbitrum_sample/"
    val outputPath = "hdfs:///user/cleaned_arbitrum/"

    val df = spark.read.parquet(inputPath)
