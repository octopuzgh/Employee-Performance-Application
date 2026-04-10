package com.octopuz.platform.service.impl;
import com.octopuz.platform.service.interf.PythonScriptService;
import com.octopuz.platform.utils.SshExecutor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PythonScriptServiceImpl implements PythonScriptService {

    @Resource
    private SshExecutor sshExecutor;

    private static final String REMOTE_SCRIPT_BASE_PATH = "/mnt/hgfs/share_files/platform/scripts";

    @Override
    public String executeScript(String scriptPath) {
        return executeScriptWithArgs(scriptPath);
    }

    @Override
    public String executeScriptWithArgs(String scriptPath, String... args) {
        String fullPath = REMOTE_SCRIPT_BASE_PATH + "/" + scriptPath;

        // 构建命令：cd 到脚本目录并执行
        String command = buildCommand(fullPath, args);
        log.info("执行远程 Python 脚本: {}", command);

        try {
            String result = sshExecutor.execute(command);
            log.info("脚本执行成功，返回结果长度: {}", result.length());
            return result;
        } catch (Exception e) {
            log.error("脚本执行失败: {}", e.getMessage(), e);
            throw new RuntimeException("Python 脚本执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建 SSH 执行命令
     */
    private String buildCommand(String scriptPath, String... args) {
        StringBuilder command = new StringBuilder();

        // 切换到脚本所在目录
        String scriptDir = scriptPath.substring(0, scriptPath.lastIndexOf('/'));
        String scriptName = scriptPath.substring(scriptPath.lastIndexOf('/') + 1);

        command.append("cd ").append(scriptDir).append(" && ");

        // 执行 Python 脚本
        command.append("python3 ").append(scriptName);

        // 添加参数
        if (args != null && args.length > 0) {
            command.append(" ").append(Arrays.stream(args)
                    .map(arg -> "\"" + arg + "\"")
                    .collect(Collectors.joining(" ")));
        }

        return command.toString();
    }
}
