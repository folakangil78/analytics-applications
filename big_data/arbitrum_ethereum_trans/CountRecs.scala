import org.apache.spark.sql.SparkSession

object CountRecs {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Count Records and Explore")
      .getOrCreate()

    val sc = spark.sparkContext
