package com.octopuz.platform.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
@Slf4j
public class UserContextUtil {
    //暂时没有登录功能
    public static String getCurrentUserEmpNo() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                // 尝试从请求头获取（如果有前端传的话）
                String empNo = request.getHeader("X-EmpNo");
                if (empNo != null && !empNo.isEmpty()) {
                    return empNo;
                }
            }
        } catch (Exception e) {
            log.error("获取当前用户empNo异常：{}", e.getMessage());
        }
        return "SYSTEM";
    }
    public static String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                //考虑反向代理
                String ipAddress = request.getHeader("X-Forwarded-For");
                if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("Proxy-Client-IP");
                }
                if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("WL-Proxy-Client-IP");
                }
                if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("HTTP_CLIENT_IP");
                }
                if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
                }
                if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getRemoteAddr();
                    if ("127.0.0.1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress)) {
                        try {
                            ipAddress = InetAddress.getLocalHost().getHostAddress();
                        } catch (UnknownHostException e) {
                            ipAddress = "UNKNOWN";
                        }
                    }
                }
                return ipAddress;
            }
        } catch (Exception e) {
            log.error("获取当前用户ip异常：{}", e.getMessage());
        }
        return "UNKNOWN";
    }
}
