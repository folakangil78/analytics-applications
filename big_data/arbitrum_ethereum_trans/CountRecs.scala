import org.apache.spark.sql.SparkSession

object CountRecs {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Count Records and Explore")
      .getOrCreate()

    val sc = spark.sparkContext

    // Path to your downloaded parquet files
    val inputPath = "arbitrum_sample/"

    val df = spark.read.parquet(inputPath)

    // -----------------------------
    // COUNT RECORDS USING MAP
    // -----------------------------
    val rdd = df.rdd

    val count = rdd
      .map(_ => ("count", 1))
      .reduceByKey(_ + _)
      .collect()

    println("Total Records:")
    count.foreach(println)