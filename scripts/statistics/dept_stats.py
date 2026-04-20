import sys
import os
import json
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from common.db_utils import get_spark_session, read_table
from common.json_utils import df_to_json


def run(*args):
    """部门统计：平均分、最高分、最低分"""
    spark = None
    try:
        spark = get_spark_session()

        # 读取表并注册为临时视图
        emp_df = read_table(spark, "employee")
        perf_df = read_table(spark, "performance")

        emp_df.createOrReplaceTempView("employee")
        perf_df.createOrReplaceTempView("performance")

        # 使用 SQL 查询
        sql = """
            SELECT 
                e.department,
                ROUND(AVG(p.score), 2) AS avg_score,
                MAX(p.score) AS max_score,
                MIN(p.score) AS min_score
            FROM employee e
            INNER JOIN performance p ON e.emp_no = p.emp_no
            GROUP BY e.department
            ORDER BY avg_score DESC
        """

        result_df = spark.sql(sql)
        # result_df = result_df.repartition(2,"department")
        # print(f"分区数：{result_df.rdd.getNumPartitions()}", file=sys.stderr)
        return df_to_json(result_df)

    except Exception as e:

        print(json.dumps({"error": str(e)}), file=sys.stderr)
        raise
    finally:
        if spark is not None:
            spark.stop()


if __name__ == "__main__":
    print(run())
