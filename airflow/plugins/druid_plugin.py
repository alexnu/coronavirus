# -*- coding: utf-8 -*-
import json

from airflow.plugins_manager import AirflowPlugin
from airflow.hooks.druid_hook import DruidHook
from airflow.models import BaseOperator
from airflow.utils.decorators import apply_defaults


# Will show up under airflow.operators.druid_plugin.EnhancedDruidOperator
class EnhancedDruidOperator(BaseOperator):
    """
    Allows to submit a task directly to druid
    :param json_index_file: The filepath to the druid index specification
    :type json_index_file: str
    :param druid_ingest_conn_id: The connection id of the Druid overlord which
        accepts index jobs
    :type druid_ingest_conn_id: str
    """
    template_fields = ('json_index_file',)
    template_ext = ('.json',)

    @apply_defaults
    def __init__(self,
                 json_index_file,
                 druid_ingest_conn_id='druid_ingest_default',
                 max_ingestion_time=None,
                 *args,
                 **kwargs):
        super(EnhancedDruidOperator, self).__init__(*args, **kwargs)
        self.json_index_file = json_index_file
        self.druid_ingest_conn_id = druid_ingest_conn_id
        self.max_ingestion_time = max_ingestion_time

    def execute(self, context):
        hook = DruidHook(
            druid_ingest_conn_id=self.druid_ingest_conn_id,
            max_ingestion_time=self.max_ingestion_time
        )
        self.log.info("Submitting %s", self.json_index_file)
        hook.submit_indexing_job(self.json_index_file)


# Defining the plugin class
class DruidPlugin(AirflowPlugin):
    name = "druid_plugin"
    operators = [EnhancedDruidOperator]
