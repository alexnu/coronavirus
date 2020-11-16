from datetime import timedelta
from airflow import DAG
from airflow.operators.bash_operator import BashOperator
from airflow.operators.druid_plugin import DruidOperator
from airflow.utils.dates import days_ago

default_args = {
    'owner': 'airflow',
    'depends_on_past': False,
    'start_date': days_ago(2),
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
}
dag = DAG(
    'coronavirus',
    default_args=default_args,
    schedule_interval='0 */12 * * *',
    catchup=False
)

spark_command = """
/spark/bin/spark-submit \
--class com.analytics.coronavirus.spark.CoronavirusJob \
--master "local" \
/usr/local/airflow/deps/spark.jar \
https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv \
https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv \
https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/UID_ISO_FIPS_LookUp_Table.csv \
/tmp/filesystem/spark_out
"""

spark = BashOperator(
    task_id='spark',
    bash_command=spark_command,
    dag=dag
)

druid = DruidOperator(
    task_id='druid',
    json_index_file='druid_spec.json',
    druid_ingest_conn_id='druid_ingest',
    dag=dag
)

spark >> druid
