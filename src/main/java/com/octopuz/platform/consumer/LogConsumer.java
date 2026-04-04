package com.octopuz.platform.consumer;

import com.alibaba.fastjson2.JSON;
import com.octopuz.platform.entity.OperationLog;
import com.octopuz.platform.mapper.OperationLogMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Data
@AllArgsConstructor
public class LogConsumer {
    private final OperationLogMapper operationLogMapper;

    @KafkaListener(topics = "operation-log", groupId = "operation-log-group")
    public void consume(String message, Acknowledgment ack) {
        try {
            log.info("消费日志：{}", message);
            // 检查是否为有效的 JSON 格式
            if (message == null || message.trim().isEmpty()) {
                log.warn("收到空消息，跳过处理");
                ack.acknowledge();
                return;
            }

            // 尝试解析为 JSON
            if (!message.trim().startsWith("{")) {
                log.warn("消息不是 JSON 格式，跳过处理：{}", message);
                ack.acknowledge();
                return;
            }
            OperationLog operationLog = JSON.parseObject(message, OperationLog.class);
            // 验证必要字段
            if (operationLog.getOperation() == null || operationLog.getContent() == null) {
                log.warn("消息缺少必要字段，跳过处理：{}", message);
                ack.acknowledge();
                return;
            }
            operationLogMapper.insert(operationLog);
            ack.acknowledge();
            log.info("消费日志成功：{}", operationLog.getId());
        } catch (Exception e) {
            log.error("消费日志异常：{}", message, e);
        }
    }
}
