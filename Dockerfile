ARG AIRFLOW_VERSION=1.10.12

FROM mozilla/sbt:8u232_1.3.8 AS build-spark
COPY spark /spark
RUN cd /spark && sbt clean package

FROM apache/airflow:$AIRFLOW_VERSION
ARG AIRFLOW_VERSION
ARG OPENJDK_VERSION=8
ARG SPARK_VERSION=2.4.7

USER root
RUN echo "deb http://security.debian.org/debian-security stretch/updates main" >> /etc/apt/sources.list

# Install java
RUN mkdir -p /usr/share/man/man1 \
  && apt-get update \
  && apt-get install -y --no-install-recommends \
         openjdk-${OPENJDK_VERSION}-jdk \
         unzip \
         wget \
  && apt-get autoremove -yqq --purge \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

# Download spark
RUN wget https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop2.7.tgz \
    && tar -xvzf spark-${SPARK_VERSION}-bin-hadoop2.7.tgz \
    && mv spark-${SPARK_VERSION}-bin-hadoop2.7 /spark \
    && rm spark-${SPARK_VERSION}-bin-hadoop2.7.tgz

USER airflow
RUN pip install --no-cache-dir --user apache-airflow[crypto,druid,postgres,jdbc]==$AIRFLOW_VERSION

COPY airflow/dags /opt/airflow/dags
COPY airflow/plugins /opt/airflow/plugins
COPY --from=build-spark /spark/target/scala-2.11/coronavirus-spark_2.11-1.0.jar /usr/local/airflow/deps/spark.jar
RUN mkdir /tmp/filesystem && chown airflow:airflow /tmp/filesystem
