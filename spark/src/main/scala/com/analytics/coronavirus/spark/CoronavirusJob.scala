package com.analytics.coronavirus.spark

import com.analytics.coronavirus.spark.CsvManager.readCsvFromSource
import org.apache.spark.sql.{SaveMode, SparkSession}

import scala.io.BufferedSource
import scala.io.Source._

object CoronavirusJob {

  def main(args: Array[String]) {
    if (args.length != 3) {
      System.err.println("Usage: CoronavirusJob <confirmed_url> <deaths_url> <output_path>")
      System.exit(1)
    }

    val spark = SparkSession.builder()
      .appName("Coronavirus Job")
      .master("local")
      .getOrCreate()

    val confirmedUrl = args(0)
    val deathsUrl = args(1)
    val outputPath = args(2)

    val confirmedSource: BufferedSource = fromURL(confirmedUrl)
    val deathsSource: BufferedSource = fromURL(deathsUrl)

    val confirmedDf = readCsvFromSource(spark, confirmedSource)
    val deathsDf = readCsvFromSource(spark, deathsSource)

    new CoronavirusQuery(spark)
      .run(confirmedDf, deathsDf)
      .write
      .mode(SaveMode.Overwrite)
      .option("header", value = true)
      .csv(outputPath)
  }
}
