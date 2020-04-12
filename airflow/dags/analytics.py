from datetime import timedelta
from airflow import DAG
from airflow.operators.bash_operator import BashOperator
from airflow.operators.druid_plugin import EnhancedDruidOperator
from airflow.utils.dates import days_ago

default_args = {
    'owner': 'airflow',
    'depends_on_past': False,
    'start_date': days_ago(2),
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
}
dag = DAG(
    'coronavirus-analytics',
    default_args=default_args,
    description='A simple tutorial DAG',
    schedule_interval='@daily',
)

spark_command = """
/spark/bin/spark-submit \
--class com.analytics.coronavirus.spark.CoronavirusJob \
--master "local[*]" \
/usr/local/airflow/dags/spark.jar \
https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv \
https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv \
/tmp/filesystem/spark_out
"""

# t1, t2 and t3 are examples of tasks created by instantiating operators
spark = BashOperator(
    task_id='spark',
    bash_command=spark_command,
    dag=dag,
)

druid = EnhancedDruidOperator(
    task_id='druid',
    json_index_file='druid_spec.json',
    druid_ingest_conn_id='druid_ingest',
    dag=dag
)

spark >> druid
