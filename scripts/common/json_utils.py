import json
from decimal import Decimal
from datetime import date, datetime


class CustomJSONEncoder(json.JSONEncoder):
    """自定义 JSON 编码器，处理 Decimal、date、datetime 类型"""

    def default(self, obj):
        if isinstance(obj, Decimal):
            return float(obj)
        elif isinstance(obj, (date, datetime)):
            return obj.strftime("%Y-%m-%d %H:%M:%S")
        return super().default(obj)


def df_to_json(df):
    """DataFrame 转 JSON 字符串"""
    # 将 DataFrame 转为 Python 字典列表
    data = [row.asDict() for row in df.collect()]
    # 使用自定义编码器序列化
    return json.dumps(data, cls=CustomJSONEncoder, ensure_ascii=False)
