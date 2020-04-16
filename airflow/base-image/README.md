This the base image that the project uses for airflow and is pushed here:
```
alexnu/airflow-spark-druid
```

To rebuild the image:
```shell script
$ docker build --build-arg AIRFLOW_DEPS="druid" -t alexnu/airflow-spark-druid:1.10.10 .
```