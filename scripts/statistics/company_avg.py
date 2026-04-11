import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from common.db_utils import get_spark_session, read_table
from common.json_utils import df_to_json


def run(*args):
    """公司平均分"""
    spark = None
    try:
        spark = get_spark_session()

        if len(args) < 2:
            return '{"error": "缺少参数，需要提供 year, quarter"}'

        year = int(args[0])
        quarter = int(args[1])

        perf_df = read_table(spark, "performance")
        perf_df.createOrReplaceTempView("performance")

        sql = f"""
            SELECT 
                ROUND(AVG(score), 2) AS avg_score
            FROM performance
            WHERE year = {year} 
              AND quarter = {quarter}
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
    if len(sys.argv) < 3:
        print('{"error": "用法: python3 company_avg.py <year> <quarter>"}')
        sys.exit(1)

    year = int(sys.argv[1])
    quarter = int(sys.argv[2])
    print(run(year, quarter))
