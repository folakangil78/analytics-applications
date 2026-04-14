import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.types._

object FirstCode {
    def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Arbitrum Event Analytics v1")
      .enableHiveSupport()
      .getOrCreate()

    import spark.implicits._

    // =========================================================
    // 1. LOAD DATA FROM HIVE TABLE (cleaned dataset)
    // =========================================================
    val df = spark.table("arbitrum_db.arbitrum_cleaned")

    // =========================================================
    // 2. DATETIME FORMATTING (CRITICAL FOR EVENT FILTERING)
    // =========================================================
    val dfWithDate = df.withColumn(
      "date",
      to_date(from_unixtime(col("datetime") / 1000))
    )

    // =========================================================
    // 3. SAFE TEXT NORMALIZATION (ONLY ADDRESSES)
    // =========================================================
    val normalizedDF = dfWithDate
      .withColumn("from_address", lower(trim(col("from_address"))))
      .withColumn("to_address", lower(trim(col("to_address"))))
      .withColumn("contract_address", lower(trim(col("contract_address"))))