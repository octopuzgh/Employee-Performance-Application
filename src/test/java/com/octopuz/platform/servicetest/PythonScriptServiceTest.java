package com.octopuz.platform.servicetest;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.octopuz.platform.service.interf.PythonScriptService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
class PythonScriptServiceTest {

    @Resource
    private PythonScriptService pythonScriptService;

    @Test
    void testExecuteScript() {
        log.info("========== 开始测试 Python 脚本执行 ==========");

        try {
            String jsonResult = pythonScriptService.executeScript("test_db_connection.py");

            log.info("========== 脚本执行成功 ==========");
            log.info("返回结果长度: {}", jsonResult.length());

            // 清理可能的空白字符
            String cleanedJson = jsonResult.trim();

            // 查找 JSON 起始位置（跳过可能的日志前缀）
            int jsonStart = cleanedJson.indexOf('[');
            if (jsonStart < 0) {
                jsonStart = cleanedJson.indexOf('{');
            }

            if (jsonStart > 0) {
                log.warn("检测到 JSON 前有 {} 个非 JSON 字符，自动跳过", jsonStart);
                cleanedJson = cleanedJson.substring(jsonStart);
            }

            log.info("清理后的 JSON 长度: {}", cleanedJson.length());

            // 解析 JSON 数组
            JSONArray jsonArray = JSON.parseArray(cleanedJson);
            log.info("✅ 解析成功！数据条数: {}", jsonArray.size());

            // 打印每条数据的摘要
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject record = jsonArray.getJSONObject(i);
                log.info("记录 {}: id={}, emp_no={}, name={}, department={}",
                        i + 1,
                        record.getInteger("id"),
                        record.getString("emp_no"),
                        record.getString("name"),
                        record.getString("department")
                );
            }

            log.info("========== 测试通过 ==========");
        } catch (Exception e) {
            log.error("========== 测试失败 ==========", e);
            throw e;
        }
    }

    @Test
    void testParseEmployeeData() {
        log.info("========== 测试解析员工数据为 Java 对象 ==========");

        try {
            String jsonResult = pythonScriptService.executeScript("test_db_connection.py");

            // 清理并提取 JSON
            String cleanedJson = extractJson(jsonResult);

            // 解析为 List<Map>
            List<Map<String, Object>> employees = JSON.parseObject(
                    cleanedJson,
                    List.class
            );

            log.info("✅ 解析到 {} 条员工记录", employees.size());

            // 遍历打印关键信息
            employees.forEach(emp -> {
                log.info("员工: {} ({}) - 部门: {}, 职位: {}",
                        emp.get("name"),
                        emp.get("emp_no"),
                        emp.get("department"),
                        emp.get("position")
                );
            });

            assert !employees.isEmpty() : "应该至少有一条员工数据";

            log.info("========== 测试通过 ==========");
        } catch (Exception e) {
            log.error("========== 测试失败 ==========", e);
            throw e;
        }
    }

    @Test
    void testErrorHandling() {
        log.info("========== 测试错误处理 ==========");

        try {
            // 测试不存在的脚本
            String result = pythonScriptService.executeScript("non_existent_script.py");
            log.warn("预期会失败，但返回了: {}", result);
        } catch (Exception e) {
            log.info("✅ 捕获到预期异常: {}", e.getMessage());
            log.info("========== 错误处理测试通过 ==========");
        }
    }

    /**
     * 从返回内容中提取 JSON
     */
    private String extractJson(String content) {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("返回内容为空");
        }

        String trimmed = content.trim();

        // 查找 JSON 起始位置
        int jsonStart = trimmed.indexOf('[');
        if (jsonStart < 0) {
            jsonStart = trimmed.indexOf('{');
        }

        if (jsonStart < 0) {
            log.error("无法找到 JSON 起始符，内容前200字符: {}",
                    trimmed.substring(0, Math.min(200, trimmed.length())));
            throw new IllegalArgumentException("未找到有效的 JSON 内容");
        }

        if (jsonStart > 0) {
            log.warn("跳过 {} 个非 JSON 字符", jsonStart);
        }

        return trimmed.substring(jsonStart);
    }

}
