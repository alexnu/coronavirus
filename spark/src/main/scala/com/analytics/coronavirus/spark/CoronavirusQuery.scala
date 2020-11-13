package com.analytics.coronavirus.spark

import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable

class CoronavirusQuery(spark: SparkSession) {

  import spark.implicits._

  val excludedCols: List[String] = List("Province/State", "Country/Region", "Lat", "Long")

  private def unpivot(df: DataFrame, newColName: String): DataFrame = {
    val datesDf: DataFrame = df.schema.names
      .filter(col => !excludedCols.contains(col)).toList
      .toDF("date_str")

    val df_expanded = df.crossJoin(datesDf)
    val df_expanded_colNames = df_expanded.columns

    def selectCol =
      udf((colName: String, cols: mutable.WrappedArray[String]) => cols(df_expanded_colNames.indexOf(colName)))

    df_expanded.select(
      $"Province/State".as("province_state"),
      $"Country/Region".as("country"),
      to_timestamp($"date_str", "MM/dd/yy").as("date"),
      selectCol($"date_str", array(df_expanded_colNames.map(i => col(i)): _*)).as(newColName)
    )
      .groupBy("date", "country")
      .agg(sum(newColName).as(newColName))
  }

  def run(confirmed: DataFrame, deaths: DataFrame, population: DataFrame): DataFrame = {
    val confirmedUnpivot = unpivot(confirmed, "confirmed_cum")
    val deathsUnpivot = unpivot(deaths, "deaths_cum")

    val countryWindow = Window
      .partitionBy("country")
      .orderBy("date")

    val lastWeek = countryWindow.rowsBetween(-6, 0)

    confirmedUnpivot.as("cu")
      .join(deathsUnpivot.as("du"),
        $"cu.date" === $"du.date" &&
          $"cu.country" === $"du.country"
      )
      .join(population.as("po"),
        $"cu.country" === $"po.Combined_Key"
      )
      .select(
        $"cu.date",
        $"cu.country",
        $"cu.confirmed_cum",
        $"du.deaths_cum",
        $"po.Population".as("population")
      )
      .withColumn("confirmed_prev", coalesce(
        lag("confirmed_cum", 1).over(countryWindow),
        lit(0)
      ))
      .withColumn("deaths_prev", coalesce(
        lag("deaths_cum", 1).over(countryWindow),
        lit(0)
      ))
      .withColumn("cases", $"confirmed_cum" -$"confirmed_prev")
      .withColumn("deaths", $"deaths_cum" - $"deaths_prev")
      .withColumn("cases_7d", round(avg("cases").over(lastWeek), 2))
      .withColumn("deaths_7d", round(avg("deaths").over(lastWeek), 2))
      .withColumn("cases_per_mil", round($"cases" * lit(1000000) / $"population", 2))
      .withColumn("deaths_per_mil", round($"deaths" * lit(1000000) / $"population", 2))
      .withColumn("cases_7d_per_mil", round($"cases_7d" * lit(1000000) / $"population", 2))
      .withColumn("deaths_7d_per_mil", round($"deaths_7d" * lit(1000000) / $"population", 2))
      .withColumn("cases_7d_per_mil_prev", coalesce(
        lag("cases_7d_per_mil", 7).over(countryWindow),
        lit(0)
      ))
      .withColumn("deaths_7d_per_mil_prev", coalesce(
        lag("deaths_7d_per_mil", 7).over(countryWindow),
        lit(0)
      ))
      .withColumn("cases_7d_per_mil_inc", coalesce(
        round(($"cases_7d_per_mil" - $"cases_7d_per_mil_prev") / $"cases_7d_per_mil_prev", 2),
        lit(0)
      ))
      .withColumn("deaths_7d_per_mil_inc", coalesce(
        round(($"deaths_7d_per_mil" - $"deaths_7d_per_mil_prev") / $"deaths_7d_per_mil_prev", 2),
        lit(0)
      ))
      .select(
        $"date",
        $"country",
        $"cases",
        $"deaths",
        $"cases_7d",
        $"deaths_7d",
        $"cases_per_mil",
        $"deaths_per_mil",
        $"cases_7d_per_mil",
        $"deaths_7d_per_mil",
        $"cases_7d_per_mil_inc",
        $"deaths_7d_per_mil_inc",
        $"population"
      )
      .orderBy("date", "country")
  }

}
