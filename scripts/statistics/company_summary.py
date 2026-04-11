import sys
import os
import json

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from common.db_utils import get_spark_session, read_table
from common.json_utils import df_to_json, CustomJSONEncoder


def run(*args):
    """公司整体大盘统计"""
    spark = None
    try:
        spark = get_spark_session()

        year = int(args[0]) if len(args) > 0 else None
        quarter = int(args[1]) if len(args) > 1 else None

        emp_df = read_table(spark, "employee")
        perf_df = read_table(spark, "performance")

        emp_df.createOrReplaceTempView("employee")
        perf_df.createOrReplaceTempView("performance")

        # 构建 WHERE 条件
        where_clause = ""
        if year and quarter:
            where_clause = f"WHERE p.year = {year} AND p.quarter = {quarter}"
        elif year:
            where_clause = f"WHERE p.year = {year}"

        # 基础统计
        sql = f"""
            SELECT 
                COUNT(DISTINCT e.emp_no) AS total_employees,
                ROUND(AVG(p.score), 2) AS avg_score,
                MAX(p.score) AS max_score,
                MIN(p.score) AS min_score,
                COUNT(p.id) AS total_records,
                COUNT(DISTINCT e.department) AS dept_count
            FROM employee e
            INNER JOIN performance p ON e.emp_no = p.emp_no
            {where_clause}
        """

        summary_df = spark.sql(sql)

        # 获取最高分员工
        max_sql = f"""
            SELECT e.emp_no, e.name, e.department, p.score
            FROM employee e
            INNER JOIN performance p ON e.emp_no = p.emp_no
            {where_clause}
            ORDER BY p.score DESC
            LIMIT 1
        """
        max_emp_df = spark.sql(max_sql)

        # 获取最低分员工
        min_sql = f"""
            SELECT e.emp_no, e.name, e.department, p.score
            FROM employee e
            INNER JOIN performance p ON e.emp_no = p.emp_no
            {where_clause}
            ORDER BY p.score ASC
            LIMIT 1
        """
        min_emp_df = spark.sql(min_sql)

        # 合并结果
        summary_data = [row.asDict() for row in summary_df.collect()][0]
        max_emp_data = [row.asDict() for row in max_emp_df.collect()][0] if max_emp_df.count() > 0 else {}
        min_emp_data = [row.asDict() for row in min_emp_df.collect()][0] if min_emp_df.count() > 0 else {}

        result = {
            **summary_data,
            "max_score_employee": max_emp_data,
            "min_score_employee": min_emp_data
        }

        return json.dumps(result, cls=CustomJSONEncoder, ensure_ascii=False)

    except Exception as e:
        print(json.dumps({"error": str(e)}), file=sys.stderr)
        raise
    finally:
        if spark is not None:
            spark.stop()


if __name__ == "__main__":
    if len(sys.argv) > 1:
        year = int(sys.argv[1]) if sys.argv[1] else None
        quarter = int(sys.argv[2]) if len(sys.argv) > 2 and sys.argv[2] else None
        print(run(year, quarter))
    else:
        print(run())
