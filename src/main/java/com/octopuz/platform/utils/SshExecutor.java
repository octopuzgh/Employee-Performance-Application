package com.octopuz.platform.utils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "ssh")
public class SshExecutor {
    private String REMOTE_HOST;
    private int REMOTE_PORT;
    private String REMOTE_USER ;
    private String PRIVATE_KEY_PATH;
    private int TIMEOUT;

    public String execute(String command) {
        Session session = null;
        ChannelExec channel = null;
        try {
            JSch jsch = new JSch();
            // 1. 加载私钥实现免密登录
            jsch.addIdentity(PRIVATE_KEY_PATH);

            session = jsch.getSession(REMOTE_USER, REMOTE_HOST, REMOTE_PORT);
            // 严格主机密钥检查设为 no（开发环境方便，生产建议设为 yes 并配置 known_hosts）
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(TIMEOUT);

            // 2. 打开执行通道
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            // 3. 获取输出流
            InputStream in = channel.getInputStream();
            channel.connect();

            // 4. 读取结果
            StringBuilder result = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            }

            // 5. 检查退出状态
            if (channel.getExitStatus() != 0) {
                log.error("Remote command failed with exit status: {}", channel.getExitStatus());
            }

            return result.toString().trim();

        } catch (Exception e) {
            log.error("SSH execution error: {}", e.getMessage(), e);
            throw new RuntimeException("SSH 执行失败", e);
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }
}
