package com.analytics.coronavirus.spark

import com.analytics.coronavirus.spark.CsvManager.readCsvFromSource
import org.apache.spark.sql.{SaveMode, SparkSession}

import scala.io.BufferedSource
import scala.io.Source._

object CoronavirusJob {

  def main(args: Array[String]) {
    if (args.length != 4) {
      System.err.println("Usage: CoronavirusJob <confirmed_url> <deaths_url> <population_url> <output_path>")
      System.exit(1)
    }

    val spark = SparkSession.builder()
      .appName("Coronavirus Job")
      .master("local")
      .getOrCreate()

    val confirmedUrl = args(0)
    val deathsUrl = args(1)
    val populationUrl = args(2)
    val outputPath = args(3)

    val confirmedSource: BufferedSource = fromURL(confirmedUrl)
    val deathsSource: BufferedSource = fromURL(deathsUrl)
    val populationSource: BufferedSource = fromURL(populationUrl)

    val confirmedDf = readCsvFromSource(spark, confirmedSource)
    val deathsDf = readCsvFromSource(spark, deathsSource)
    val populationDf = readCsvFromSource(spark, populationSource)

    new CoronavirusQuery(spark)
      .run(confirmedDf, deathsDf, populationDf)
      .write
      .mode(SaveMode.Overwrite)
      .option("header", value = true)
      .csv(outputPath)
  }
}
