package com.octopuz.platform.utilstest;

import com.octopuz.platform.utils.SshExecutor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class SshExecutorTest {
    @Resource
    private SshExecutor sshExecutor;
    @Test
    public void test() {
        String result = sshExecutor.execute("echo 'Hello from Linux' && hostname");
        log.info("执行结果：{}", result);

    }
}
