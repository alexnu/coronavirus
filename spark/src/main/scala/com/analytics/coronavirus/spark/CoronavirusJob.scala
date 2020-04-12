package com.analytics.coronavirus.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, SaveMode, SparkSession}

import scala.io.BufferedSource
import scala.io.Source._

object CoronavirusJob {

  def readCsvFromUrl(spark: SparkSession, url: String): DataFrame = {
    import spark.implicits._

    val urlSource: BufferedSource = fromURL(url)
    val csvList: List[String] = urlSource.mkString.stripMargin.lines.toList
    urlSource.close()
    val rdd: RDD[String] = spark.sparkContext.parallelize(csvList)
    val csvData: Dataset[String] = rdd.toDS()
    spark.read
      .option("header", value = true)
      .option("inferSchema", value = true)
      .csv(csvData)
  }

  def buildSparkSession(runLocal: Boolean): SparkSession = {
    var ssBuilder = SparkSession.builder()
      .appName("Coronavirus Job")

    if (runLocal) {
      ssBuilder = ssBuilder.master("local[*]")
    }

    ssBuilder.getOrCreate()
  }

  def main(args: Array[String]) {
    if (args.length < 3) {
      System.err.println("Usage: CoronavirusJob <confirmed_csv> <deaths_csv> <output_path> [<run_local>]")
      System.exit(1)
    }

    val confirmedUrl = args(0)
    val deathsUrl = args(1)
    val outputPath = args(2)
    val runLocal: Boolean = args.length >= 4

    val spark = buildSparkSession(runLocal)

    val confirmedDf = readCsvFromUrl(spark, confirmedUrl)
    val deathsDf = readCsvFromUrl(spark, deathsUrl)

    new CoronavirusQuery(spark).run(confirmedDf, deathsDf)
      .write
      .mode(SaveMode.Overwrite)
      .option("header", value = true)
      .csv(outputPath)
  }
}
