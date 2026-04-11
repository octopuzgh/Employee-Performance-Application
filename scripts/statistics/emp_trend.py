import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from common.db_utils import get_spark_session, read_table
from common.json_utils import df_to_json


def run(*args):
    """员工绩效趋势"""
    spark = None
    try:
        spark = get_spark_session()

        if len(args) < 1:
            return '{"error": "缺少参数 emp_no"}'

        emp_no = args[0]
        emp_df = read_table(spark, "employee")
        perf_df = read_table(spark, "performance")
        emp_df.createOrReplaceTempView("employee")
        perf_df.createOrReplaceTempView("performance")

        # 使用窗口函数计算环比变化
        sql = f"""
                    SELECT 
                        p.emp_no,
                        e.name,
                        p.year,
                        p.quarter,
                        ROUND(p.score, 2) AS score,
                        ROUND(
                            (p.score - LAG(p.score, 1) OVER (ORDER BY p.year, p.quarter)) 
                            / LAG(p.score, 1) OVER (ORDER BY p.year, p.quarter) * 100,
                            2
                        ) AS growth_rate
                    FROM performance p
                    INNER JOIN employee e ON p.emp_no = e.emp_no
                    WHERE p.emp_no = '{emp_no}'
                    ORDER BY p.year, p.quarter
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
    emp_no = sys.argv[1] if len(sys.argv) > 1 else None
    if not emp_no:
        print('{"error": "用法: python3 emp_trend.py <emp_no>"}')
        sys.exit(1)
    print(run(emp_no))
