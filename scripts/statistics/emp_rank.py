import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from common.db_utils import get_spark_session, read_table
from common.json_utils import df_to_json


def run(top_n=10):
    """员工个人排名 Top N"""
    spark = None
    try:
        spark = get_spark_session()

        # 读取表并注册为临时视图
        emp_df = read_table(spark, "employee")
        perf_df = read_table(spark, "performance")

        emp_df.createOrReplaceTempView("employee")
        perf_df.createOrReplaceTempView("performance")

        # 使用 SQL 查询：按员工平均绩效排名
        sql = f"""
            SELECT 
                e.emp_no,
                e.name,
                e.department,
                e.position,
                ROUND(AVG(p.score), 2) AS avg_score,
                COUNT(p.score) AS record_count
            FROM employee e
            INNER JOIN performance p ON e.emp_no = p.emp_no
            GROUP BY e.emp_no, e.name, e.department, e.position
            ORDER BY avg_score DESC
            LIMIT {top_n}
        """

        result_df = spark.sql(sql)

        return df_to_json(result_df)

    except Exception as e:
        import json
        print(json.dumps({"error": str(e)}), file=sys.stderr)
        raise
    finally:
        if spark is not None:
            spark.stop()


if __name__ == "__main__":
    # 支持命令行参数指定 Top N，默认 10
    top_n = int(sys.argv[1]) if len(sys.argv) > 1 else 10
    print(run(top_n))
