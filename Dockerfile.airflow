FROM mozilla/sbt:8u232_1.3.8
COPY spark /spark
RUN cd /spark && sbt package

FROM alexnu/airflow-spark-druid:1.10.10
COPY airflow/dags /usr/local/airflow/dags
COPY airflow/plugins /usr/local/airflow/plugins
COPY --from=0 /spark/target/scala-2.11/coronavirus-spark_2.11-1.0.jar /usr/local/airflow/dags/spark.jar
