import sys
import os

# 添加项目根目录到 Python 路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from common.db_utils import get_spark_session, read_table
from common.json_utils import df_to_json


def run():
    """测试读取数据库表数据"""
    spark = None
    try:
        # 获取 Spark Session
        spark = get_spark_session()

        table_name = "employee"

        print(f"[INFO] 正在读取表: {table_name}", file=sys.stderr)
        df = read_table(spark, table_name)

        # 显示前几行数据（调试用，输出到 stderr）
        print(f"[INFO] 数据行数: {df.count()}", file=sys.stderr)
        print(f"[INFO] 数据列: {df.columns}", file=sys.stderr)

        # 转换为 JSON 并输出到 stdout（Java 会读取这个）
        result_json = df_to_json(df.limit(5))  # 只取前5条作为测试

        spark.stop()
        return result_json

    except Exception as e:
        error_msg = {"error": str(e), "type": type(e).__name__}
        import json
        print(json.dumps(error_msg), file=sys.stdout)
        print(f"[ERROR] {str(e)}", file=sys.stderr)
        spark.stop()
        raise
    finally:
        if spark:
            spark.stop()


if __name__ == "__main__":
    print(run())
