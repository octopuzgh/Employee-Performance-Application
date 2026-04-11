import sys
import os
import importlib
import logging

# 添加项目根目录到 Python 路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('logs/app.log'),
        logging.StreamHandler(sys.stderr)
    ]
)
logger = logging.getLogger(__name__)

# 路由映射
handlers = {
    "dept_stats": "statistics.dept_stats",
    "emp_rank": "statistics.emp_rank",
    "dept_rank": "statistics.dept_rank",
    "emp_trend": "statistics.emp_trend",
    "company_summary": "statistics.company_summary",
    "anomaly_detect": "statistics.anomaly_detect"
}


def main():
    """入口函数：根据参数调用对应的统计模块"""
    if len(sys.argv) < 2:
        error_msg = {"error": "缺少参数 stat_type", "available": list(handlers.keys())}
        import json
        print(json.dumps(error_msg))
        sys.exit(1)

    stat_type = sys.argv[1]

    # 检查参数是否有效
    if stat_type not in handlers:
        error_msg = {"error": f"未知的统计类型: {stat_type}", "available": list(handlers.keys())}
        import json
        print(json.dumps(error_msg))
        sys.exit(1)

    try:
        logger.info(f"执行统计任务: {stat_type}")

        # 动态导入模块
        module_path = handlers[stat_type]
        module = importlib.import_module(module_path)

        # 调用 run() 方法，支持额外参数
        if len(sys.argv) > 2:
            result = module.run(*sys.argv[2:])
        else:
            result = module.run()

        # 输出结果到 stdout
        print(result)
        logger.info(f"统计任务完成: {stat_type}")

    except Exception as e:
        logger.error(f"统计任务失败: {stat_type}, 错误: {str(e)}")
        import json
        print(json.dumps({"error": str(e), "type": type(e).__name__}))
        sys.exit(1)


if __name__ == "__main__":
    main()
