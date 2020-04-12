package com.analytics.coronavirus.spark

import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

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
      to_date($"date_str", "MM/dd/yy").as("date"),
      evaluateCol($"date_str", array(df_expanded_colNames.map(col): _*)).as(colName)
    )
  }

  def run(confirmed: DataFrame, deaths: DataFrame): DataFrame = {
    val confirmed_unpivot = unpivotDF(confirmed, "confirmed_cum")
    val deaths_unpivot = unpivotDF(deaths, "deaths_cum")

    val windowSpec = Window.partitionBy("country_region", "province_state").orderBy("date")
    confirmed_unpivot.as("cu")
      .join(deaths_unpivot.as("du"),
        $"cu.date" === $"du.date" &&
          $"cu.country_region" === $"du.country_region" &&
          $"cu.province_state" <=> $"du.province_state"
      )
      .select(
        $"cu.date",
        $"cu.country_region",
        $"cu.province_state",
        $"cu.confirmed_cum",
        $"du.deaths_cum"
      )
      .withColumn("confirmed_prev", coalesce(
        lag("confirmed_cum", 1).over(windowSpec),
        lit(0)
      ))
      .withColumn("deaths_prev", coalesce(
        lag("deaths_cum", 1).over(windowSpec),
        lit(0)
      ))
      .withColumn("confirmed", $"confirmed_cum".minus($"confirmed_prev"))
      .withColumn("deaths", $"deaths_cum".minus($"deaths_prev"))
      .orderBy("date", "country_region", "province_state")
  }
}
