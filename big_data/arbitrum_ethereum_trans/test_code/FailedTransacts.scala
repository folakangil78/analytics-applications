import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object Failed {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Arbitrum Failed Transactions Count")
      .getOrCreate()

    import spark.implicits._

    val inputPath = "hdfs:///user/fjo2015_nyu_edu/arbitrum_sample_cleaned/"

    val df = spark.read.parquet(inputPath)

    val failedCount = df.filter($"STATUS" =!= 1).count()

    println(s"Failed transactions (STATUS != 1): $failedCount")

    spark.stop()
  }
}
