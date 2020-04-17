package com.analytics.coronavirus.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import scala.io.BufferedSource

object CsvManager {

  def downloadCsv(source: BufferedSource): List[String] = {
    val csvList: List[String] = source.mkString.stripMargin.lines.toList
    source.close()
    csvList
  }

  def convertCsvToDf(spark: SparkSession, csv: List[String]): DataFrame = {
    import spark.implicits._

    val rdd: RDD[String] = spark.sparkContext.parallelize(csv)
    val csvData: Dataset[String] = rdd.toDS()
    spark.read
      .option("header", value = true)
      .option("inferSchema", value = true)
      .csv(csvData)
  }

  def readCsvFromSource(spark: SparkSession, source: BufferedSource): DataFrame = {
    val csv = downloadCsv(source)
    convertCsvToDf(spark, csv)
  }

}
