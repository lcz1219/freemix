package com.freemix.freemix.util;

import com.freemix.freemix.enetiy.AgentModel;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginLogUtil {

    /**
     * 获取用户真实IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理的情况，第一个IP为真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        // 本地地址处理
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                ip = "127.0.0.1";
            }
        }
        return ip;
    }

    /**
     * 解析用户代理信息，获取设备类型
     */
    public static String getDeviceType(String userAgent) {
        if (StringUtils.isEmpty(userAgent)) {
            return "Unknown";
        }
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("mobile") || userAgent.contains("android") || 
            userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "Mobile";
        } else {
            return "PC";
        }
    }

    /**
     * 解析用户代理信息，获取浏览器信息
     */
    public static String getBrowser(String userAgent) {
        if (StringUtils.isEmpty(userAgent)) {
            return "Unknown";
        }

        if (userAgent.contains(AgentModel.Chrome)) {
            return getBrowserName(userAgent, AgentModel.Chrome);
        } else if (userAgent.contains(AgentModel.Firefox)) {
            return getBrowserName(userAgent, AgentModel.Firefox);
        } else if (userAgent.contains(AgentModel.Safari)) {
            return getBrowserName(userAgent, AgentModel.Safari);
        } else if (userAgent.contains(AgentModel.Edge)) {
            return getBrowserName(userAgent, AgentModel.Edge);
        } else if (userAgent.contains(AgentModel.MSIE)) {
            return getBrowserName(userAgent, AgentModel.InternetExplorer);
        }else if (userAgent.contains(AgentModel.Electron)) {
            return "桌面端";
        } else {
            return "Unknown";
        }
    }

    /**
     * 解析用户代理信息，获取操作系统信息
     */
    public static String getOperatingSystem(String userAgent) {
        if (StringUtils.isEmpty(userAgent)) {
            return "Unknown";
        }
        
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("windows")) {
            return "Windows";
        } else if (userAgent.contains("macintosh") || userAgent.contains("mac os")) {
            return "macOS";
        } else if (userAgent.contains("linux")) {
            return "Linux";
        } else if (userAgent.contains("android")) {
            return "Android";
        } else if (userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "iOS";
        } else {
            return "Unknown";
        }
    }

    /**
     * 提取浏览器名称和版本
     */
    private static String getBrowserName(String userAgent, String browserName) {
        Pattern pattern = Pattern.compile(browserName + "[\\s\\/]+([\\d.]+)");
        Matcher matcher = pattern.matcher(userAgent);
        if (matcher.find()) {
            return browserName + " " + matcher.group(1);
        }
        return browserName;
    }
}