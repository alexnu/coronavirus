# coronavirus
Coronavirus Dashboard

./bin/spark-submit \
--class com.analytics.coronavirus.spark.CoronavirusJob \
--master "local[*]" \
/opt/coronavirus-spark/target/scala-2.11/coronavirus-spark_2.11-1.0.jar \
https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv \
https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv \
/tmp/filesystem/data_out
