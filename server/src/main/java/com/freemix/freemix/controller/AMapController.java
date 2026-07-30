package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 高德地图控制器
 * Key 存放在后端配置文件中，前端通过本接口获取，避免 Key 暴露在客户端代码中
 */
@Slf4j
@RestController
@RequestMapping("/amap")
public class AMapController {

    @Value("${amap.key}")
    private String amapKey;

    /**
     * 获取高德地图 JS SDK 所需的配置（Key 和安全密钥）
     * 前端动态加载 AMap SDK 时调用此接口获取 Key
     */
    @GetMapping("/config")
    public ApiResponse getAMapConfig() {
        JSONObject config = new JSONObject();
        config.put("key", amapKey);
        // 安全密钥（jscode）：高德 2021年底之后的新 Key 需要配合 jscode 使用
        // 如果你的 Key 是旧版不需要，可以返回空字符串
        config.put("jscode", "");
        config.put("version", "2.0");
        return ApiResponse.success(config);
    }

    /**
     * 代理高德地理编码 / 逆地理编码请求
     * 前端发起搜索时，请求走后端代理，Key 不暴露在浏览器网络请求中
     *
     * @param keywords 搜索关键词（如"望京SOHO"）
     * @param city     限定城市（可选）
     */
    @GetMapping("/geocode")
    public ApiResponse proxyGeocode(
            @RequestParam String keywords,
            @RequestParam(required = false, defaultValue = "") String city) {
        try {
            String encodedKeywords = URLEncoder.encode(keywords, StandardCharsets.UTF_8);
            String encodedCity = city.isEmpty() ? "" : URLEncoder.encode(city, StandardCharsets.UTF_8);

            String apiUrl = String.format(
                "https://restapi.amap.com/v3/place/text?key=%s&keywords=%s&city=%s&citylimit=false&output=json",
                amapKey, encodedKeywords, encodedCity
            );

            String response = httpGet(apiUrl);
            JSONObject result = JSONObject.parseObject(response);

            if ("1".equals(result.getString("status"))) {
                return ApiResponse.success(result.getJSONArray("pois"));
            } else {
                return ApiResponse.failure("地理编码查询失败: " + result.getString("info"));
            }
        } catch (Exception e) {
            log.error("高德地理编码代理失败:", e);
            return ApiResponse.failure("地理编码代理失败: " + e.getMessage());
        }
    }

    /**
     * 代理高德逆地理编码（坐标转地址）
     */
    @GetMapping("/regeo")
    public ApiResponse proxyRegeo(
            @RequestParam String location) {
        try {
            String apiUrl = String.format(
                "https://restapi.amap.com/v3/geocode/regeo?key=%s&location=%s&output=json",
                amapKey, location
            );

            String response = httpGet(apiUrl);
            JSONObject result = JSONObject.parseObject(response);

            if ("1".equals(result.getString("status"))) {
                return ApiResponse.success(result.getJSONObject("regeocode"));
            } else {
                return ApiResponse.failure("逆地理编码查询失败: " + result.getString("info"));
            }
        } catch (Exception e) {
            log.error("高德逆地理编码代理失败:", e);
            return ApiResponse.failure("逆地理编码代理失败: " + e.getMessage());
        }
    }

    private String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }
}
