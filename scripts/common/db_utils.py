from pyspark.sql import SparkSession

from config.settings import SPARK_CONFIG, MYSQL_CONFIG


def get_spark_session():
    """获取 Spark Session（单例模式）"""
    spark = (SparkSession.builder
        .appName(SPARK_CONFIG["appName"])
        .master(SPARK_CONFIG["master"])
        .config("spark.sql.adaptive.enabled", SPARK_CONFIG["config"]["spark.sql.adaptive.enabled"])
        .config("spark.sql.adaptive.coalescePartitions.enabled",
                SPARK_CONFIG["config"]["spark.sql.adaptive.coalescePartitions.enabled"])
        .config("spark.jars", SPARK_CONFIG["config"]["spark.jars"])

        .getOrCreate())

    spark.sparkContext.setLogLevel("WARN")
    return spark


def read_table(spark, table_name):
    """从 MySQL 读取表数据"""
    df = (spark.read.format("jdbc")
        .option("url", MYSQL_CONFIG["url"])
        .option("dbtable", table_name)
        .option("user", MYSQL_CONFIG["user"])
        .option("password", MYSQL_CONFIG["password"])
        .option("driver", MYSQL_CONFIG["driver"])
        .load())

    return df