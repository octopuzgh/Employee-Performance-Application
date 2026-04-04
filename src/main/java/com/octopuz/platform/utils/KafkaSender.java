package com.octopuz.platform.utils;

import com.alibaba.fastjson.JSON;
import com.octopuz.platform.entity.OperationLog;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaSender {
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    //发送日志到kafka
    public void sendOperationLog(String operation, String content) {
        String empNo = UserContextUtil.getCurrentUserEmpNo();
        String ipAddress = UserContextUtil.getClientIpAddress();
        OperationLog operationLog = OperationLog.builder()
                .empNo(empNo)
                .operation(operation)
                .content(content)
                .ipAddress(ipAddress)
                .build();
        sendMessage(operationLog);
    }
    //发送操作日志对象
    public void sendMessage(OperationLog operationLog) {
        try {
            String message = JSON.toJSONString(operationLog);
            kafkaTemplate.send("operation-log", message);
            log.info("发送操作日志：{}", message);
        } catch (Exception e) {
            log.error("发送操作日志异常：{}", operationLog,e);
        }
    }
}

