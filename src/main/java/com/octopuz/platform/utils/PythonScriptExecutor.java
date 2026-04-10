package com.octopuz.platform.utils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PythonScriptExecutor {

    @Resource
    private SshExecutor sshExecutor;

    private static final String REMOTE_SCRIPT_BASE_PATH = "/mnt/hgfs/share_files/platform/scripts";

    /**
     * 执行远程 Python 脚本
     */
    public String execute(String scriptPath) {
        return executeWithArgs(scriptPath);
    }

    /**
     * 执行带参数的 Python 脚本
     * @param scriptPath 脚本路径
     * @param args 参数列表
     * @return 脚本输出结果
     */
    public String executeWithArgs(String scriptPath, String... args) {
        String fullPath = REMOTE_SCRIPT_BASE_PATH + "/" + scriptPath;
        String command = buildCommand(fullPath, args);

        log.debug("执行远程 Python 脚本: {}", command);

        try {
            String result = sshExecutor.execute(command);
            log.debug("脚本执行成功，返回长度: {}", result.length());
            return result;
        } catch (Exception e) {
            log.error("脚本执行失败: {}", e.getMessage(), e);
            throw new RuntimeException("Python 脚本执行失败: " + e.getMessage(), e);
        }
    }

    private String buildCommand(String scriptPath, String... args) {
        StringBuilder command = new StringBuilder();

        String scriptDir = scriptPath.substring(0, scriptPath.lastIndexOf('/'));
        String scriptName = scriptPath.substring(scriptPath.lastIndexOf('/') + 1);

        command.append("cd ").append(scriptDir).append(" && ");
        command.append("python3 ").append(scriptName);

        if (args != null && args.length > 0) {
            command.append(" ").append(Arrays.stream(args)
                    .map(arg -> "\"" + arg + "\"")
                    .collect(Collectors.joining(" ")));
        }

        return command.toString();
    }

    public String extractJson(String content) {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("返回内容为空");
        }

        String trimmed = content.trim();

        int jsonStart = trimmed.indexOf('[');
        if (jsonStart < 0) {
            jsonStart = trimmed.indexOf('{');
        }

        if (jsonStart < 0) {
            throw new IllegalArgumentException("未找到有效的 JSON 内容");
        }

        return trimmed.substring(jsonStart);
    }
}
