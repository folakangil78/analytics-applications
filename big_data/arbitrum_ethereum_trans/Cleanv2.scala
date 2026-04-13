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