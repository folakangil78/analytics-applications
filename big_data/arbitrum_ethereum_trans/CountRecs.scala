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

    // -----------------------------
    // DISTINCT VALUES (IMPORTANT COLUMNS)
    // -----------------------------

    val fromDistinct = df.select("FROM_ADDRESS").distinct().count()
    val toDistinct = df.select("TO_ADDRESS").distinct().count()
    val contractDistinct = df.select("CONTRACT_ADDRESS").distinct().count()

    println(s"Distinct FROM_ADDRESS: $fromDistinct")
    println(s"Distinct TO_ADDRESS: $toDistinct")
    println(s"Distinct CONTRACT_ADDRESS: $contractDistinct")

    // -----------------------------
    // BASIC AGG (for intuition)
    // -----------------------------

    df.groupBy("FROM_ADDRESS")
      .count()
      .orderBy(org.apache.spark.sql.functions.desc("count"))
      .show(10)

    spark.stop()
  }
}