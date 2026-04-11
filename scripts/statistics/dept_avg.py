import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from common.db_utils import get_spark_session, read_table
from common.json_utils import df_to_json


def run(*args):
    """部门平均分"""
    spark = None
    try:
        spark = get_spark_session()

        if len(args) < 3:
            return '{"error": "缺少参数，需要提供 year, quarter, department"}'

        year = int(args[0])
        quarter = int(args[1])
        department = args[2]

        emp_df = read_table(spark, "employee")
        perf_df = read_table(spark, "performance")

        emp_df.createOrReplaceTempView("employee")
        perf_df.createOrReplaceTempView("performance")

        sql = f"""
            SELECT 
                ROUND(AVG(p.score), 2) AS avg_score
            FROM employee e
            INNER JOIN performance p ON e.emp_no = p.emp_no
            WHERE p.year = {year} 
              AND p.quarter = {quarter}
              AND e.department = '{department}'
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
    if len(sys.argv) < 4:
        print('{"error": "用法: python3 dept_avg.py <year> <quarter> <department>"}')
        sys.exit(1)

    year = int(sys.argv[1])
    quarter = int(sys.argv[2])
    department = sys.argv[3]
    print(run(year, quarter, department))
