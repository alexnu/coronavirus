package com.analytics.coronavirus.spark

import com.analytics.coronavirus.spark.CsvManager.readCsvFromSource
import com.holdenkarau.spark.testing.DataFrameSuiteBase
import org.apache.spark.sql.types._
import org.scalatest.FunSuite

import scala.io.BufferedSource
import scala.io.Source._

class CoronavirusQuerySpec extends FunSuite with DataFrameSuiteBase {

  // this is needed so that order and types do not get messed up
  val resultSchema = StructType(
    List(
      StructField("date", DateType, nullable = true),
      StructField("country_region", StringType, nullable = true),
      StructField("province_state", StringType, nullable = true),
      StructField("confirmed", DoubleType, nullable = true),
      StructField("deaths", DoubleType, nullable = true)
    )
  )

  test("Coronavirus query result is correct") {
    val confirmedSource: BufferedSource = fromFile("src/test/resources/fixtures/confirmed.csv")
    val deathsSource: BufferedSource = fromFile("src/test/resources/fixtures/deaths.csv")
    val expectedSource: BufferedSource = fromFile("src/test/resources/fixtures/expected.csv")

    val confirmed = readCsvFromSource(spark, confirmedSource)
    val deaths = readCsvFromSource(spark, deathsSource)
    val expected = readCsvFromSource(spark, expectedSource)
    val expectedCast = expected.withColumn("date", expected("date").cast(DateType))

    val actual = new CoronavirusQuery(spark).run(confirmed, deaths)
    assertDataFrameEquals(actual, expectedCast)
  }

}
