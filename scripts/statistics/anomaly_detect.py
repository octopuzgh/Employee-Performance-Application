import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from common.db_utils import get_spark_session, read_table
from common.json_utils import df_to_json


def run(*args):
    """异常波动预警"""
    spark = None
    try:
        spark = get_spark_session()

        threshold = int(args[0]) if len(args) > 0 else 20

        emp_df = read_table(spark, "employee")
        perf_df = read_table(spark, "performance")

        emp_df.createOrReplaceTempView("employee")
        perf_df.createOrReplaceTempView("performance")

        # 使用窗口函数检测相邻季度分差
        sql = f"""
                    SELECT 
                        emp_no,
                        name,
                        department,
                        year,
                        quarter,
                        current_score,
                        prev_score,
                        score_diff
                    FROM (
                        SELECT 
                            e.emp_no,
                            e.name,
                            e.department,
                            p.year,
                            p.quarter,
                            ROUND(p.score, 2) AS current_score,
                            ROUND(LAG(p.score, 1) OVER (PARTITION BY p.emp_no ORDER BY p.year, p.quarter), 2) AS prev_score,
                            ROUND(ABS(p.score - LAG(p.score, 1) OVER (PARTITION BY p.emp_no ORDER BY p.year, p.quarter)), 2) AS score_diff
                        FROM employee e
                        INNER JOIN performance p ON e.emp_no = p.emp_no
                    ) t
                    WHERE score_diff > {threshold}
                    ORDER BY score_diff DESC
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
    threshold = int(sys.argv[1]) if len(sys.argv) > 1 else 20
    print(run(threshold))
