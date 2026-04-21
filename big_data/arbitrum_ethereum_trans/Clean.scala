import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object Clean {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Clean Arbitrum Data")
      .getOrCreate()

    val inputPath = "file:///home/fjo2015_nyu_edu/Arbitrum/arbitrum_sample"
    val outputPath = "hdfs:///user/cleaned_arbitrum/"

    
    import org.apache.hadoop.fs.{FileSystem, Path}

    // Build list of real Parquet files
    val fs = FileSystem.get(spark.sparkContext.hadoopConfiguration)
    val root = new Path(inputPath)

    // Recursively collect paths if nested
    def collectParquetFiles(path: Path): Seq[String] = {
    val status = fs.listStatus(path)
    status.flatMap { s =>
        val p = s.getPath
        if (s.isDirectory) {
        collectParquetFiles(p)
        } else if (p.getName.endsWith(".parquet")) {
        Seq(p.toString)
        } else {
        Seq.empty
        }
    }
    }

    val parquetFiles: Seq[String] = collectParquetFiles(root)
    println(s"Number of valid Parquet files: ${parquetFiles.size}")

    val df = spark.read.parquet(parquetFiles: _*)


    // -----------------------------
    // SELECT ONLY RELEVANT COLUMNS
    // -----------------------------
    val cleaned = df.select(
      col("FROM_ADDRESS"),
      col("TO_ADDRESS"),
      col("VALUE"),
      col("DATETIME"),
      col("TRANSACTION_HASH"),
      col("STATUS"),
      col("CONTRACT_ADDRESS")
    )

    // -----------------------------
    // DROP NULLS
    // -----------------------------
    val nonNull = cleaned.na.drop()

    // -----------------------------
    // FILTER FAILED TRANSACTIONS
    // STATUS == 1 means success
    // -----------------------------
    val validTx = nonNull.filter(col("STATUS") === 1)

    // -----------------------------
    // REMOVE ZERO VALUE TX (noise)
    // -----------------------------
    val meaningful = validTx.filter(col("VALUE") > 0)

    // -----------------------------
    // ADD FEATURE: TIMESTAMP (full date + time of day, for grouping later)
    // -----------------------------
    val enriched = meaningful.withColumn(
      "DATETIME_TS",
      col("DATETIME").cast("timestamp")
    )

    // -----------------------------
    // WRITE TO HDFS
    // -----------------------------
    enriched.write
      .mode("overwrite")
      .parquet(outputPath)

    println("Cleaned data written to HDFS.")

    spark.stop()
  }
}