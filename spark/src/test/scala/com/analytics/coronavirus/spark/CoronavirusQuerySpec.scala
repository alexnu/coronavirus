package com.analytics.coronavirus.spark

import com.analytics.coronavirus.spark.CsvManager.readCsvFromSource
import com.holdenkarau.spark.testing.DataFrameSuiteBase
import org.apache.spark.sql.types._
import org.scalatest.FunSuite

import scala.io.BufferedSource
import scala.io.Source._

class CoronavirusQuerySpec extends FunSuite with DataFrameSuiteBase {

  val schema: StructType = StructType(
    Seq(
      StructField("date", TimestampType, nullable = true),
      StructField("country", StringType, nullable = true),
      StructField("cases", DoubleType, nullable = true),
      StructField("deaths", DoubleType, nullable = true),
      StructField("cases_7d", DoubleType, nullable = true),
      StructField("deaths_7d", DoubleType, nullable = true),
      StructField("cases_per_mil", DoubleType, nullable = true),
      StructField("deaths_per_mil", DoubleType, nullable = true),
      StructField("cases_7d_per_mil", DoubleType, nullable = true),
      StructField("deaths_7d_per_mil", DoubleType, nullable = true),
      StructField("cases_7d_per_mil_inc", DoubleType, nullable = false),
      StructField("deaths_7d_per_mil_inc", DoubleType, nullable = false),
      StructField("population", IntegerType, nullable = true)
    ))

  test("Coronavirus query result is correct") {
    val confirmedSource: BufferedSource = fromFile("src/test/resources/fixtures/confirmed.csv")
    val deathsSource: BufferedSource = fromFile("src/test/resources/fixtures/deaths.csv")
    val populationSource: BufferedSource = fromFile("src/test/resources/fixtures/population.csv")
    val expectedSource: BufferedSource = fromFile("src/test/resources/fixtures/expected.csv")

    val confirmed = readCsvFromSource(spark, confirmedSource)
    val deaths = readCsvFromSource(spark, deathsSource)
    val population = readCsvFromSource(spark, populationSource)
    val expected = readCsvFromSource(spark, expectedSource)
    val expectedCast = spark.createDataFrame(expected.rdd, schema)

    val actual = new CoronavirusQuery(spark).run(confirmed, deaths, population)
    assertDataFrameEquals(actual, expectedCast)
  }

}
