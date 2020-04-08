package com.analytics.coronavirus.spark

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

import scala.collection.mutable

class CoronavirusQuery(spark: SparkSession) {

  import spark.implicits._

  val excludedCols: List[String] = List("Province/State", "Country/Region", "Lat", "Long")

  private def unpivotDF(df: DataFrame, colName: String): DataFrame = {
    val datesDf: DataFrame = df.schema.names
      .filter(col => !excludedCols.contains(col)).toList
      .toDF("date_str")

    val df_expanded = df.crossJoin(datesDf)
    val df_expanded_colNames = df_expanded.columns
    def evaluateCol =
      udf((colName: String, array: mutable.WrappedArray[String]) => array(df_expanded_colNames.indexOf(colName)))
    df_expanded.select(
      $"Province/State".as("province_state"),
      $"Country/Region".as("country_region"),
      to_date($"date_str","MM/dd/yy").as("date"),
      evaluateCol($"date_str", array(df_expanded_colNames.map(col): _*)).as(colName)
    )
  }

  def run(confirmed: DataFrame, deaths: DataFrame): DataFrame = {
    val confirmed_unpivot = unpivotDF(confirmed, "confirmed")
    val deaths_unpivot = unpivotDF(deaths, "deaths")
    confirmed_unpivot.join(deaths_unpivot, Seq("date", "country_region", "province_state"))
      .orderBy("date", "country_region", "province_state")
  }
}
