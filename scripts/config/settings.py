
# MySQL 配置
MYSQL_CONFIG = {
    "url": "jdbc:mysql://192.168.152.131:3306/employee_performance?useSSL=false",
    "user": "octopuz_remote",
    "password": "Octopuz@123",
    "driver": "com.mysql.cj.jdbc.Driver"
}

# Spark 配置
SPARK_CONFIG = {
    "appName": "绩效统计API",
    "master": "local[*]",
    "config": {
        "spark.sql.adaptive.enabled": "true",
        "spark.sql.adaptive.coalescePartitions.enabled": "true",
        "spark.jars": "/opt/spark-4.1.1-bin-hadoop3/jars/mysql-connector-j-8.0.33.jar"
    }
}

# JDBC 通用配置（不含表名）
JDBC_CONFIG = {
    "url": MYSQL_CONFIG["url"],
    "user": MYSQL_CONFIG["user"],
    "password": MYSQL_CONFIG["password"],
    "driver": MYSQL_CONFIG["driver"]
}