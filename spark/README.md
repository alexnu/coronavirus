# Coronavirus Spark job
Spark job which loads Johns Hopkins dataset and prepares it to be loaded into Druid.

## Usage
Run CoronavirusJob class with the following arguments:

```shell script
CoronavirusJob <confirmed_url> <deaths_url> <output_path>
```

where:
- confirmed_url: https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv
- deaths_url: https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv
- output_path: ../.filesystem/spark_out

Also, be sure to include dependencies with "provided" scope (in Intellij it's just a click inside the run configuration).

## Tests
You can run tests with:
```shell script
$ sbt test
```

## Package
You can package the project into a Jar with:
```shell script
$ sbt clean package
```
