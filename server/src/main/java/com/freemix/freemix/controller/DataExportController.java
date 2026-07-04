package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.*;
import com.freemix.freemix.util.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据导出控制器
 * 所有格式的转换逻辑都在服务端完成，前端只负责触发下载
 * 支持格式：CSV、Excel (.xlsx)、JSON、XML、SQL、DMP (Oracle兼容SQL)
 */
@RestController
public class DataExportController extends BaseController {

    /** 时间戳格式 */
    private static final SimpleDateFormat TS_FMT = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取当前用户的目标相关数据（不含AI对话/通知/成就等）
     */
    private JSONObject getGoalData(String username) {
        JSONObject data = new JSONObject();

        // 1. 用户信息（排除敏感字段）
        User user = mongoTemplate.findOne(
                new Query(Criteria.where("username").is(username)), User.class);
        if (user != null) {
            JSONObject profile = new JSONObject();
            profile.put("username", user.getUsername());
            profile.put("chinesename", user.getChinesename());
            profile.put("email", user.getEmail());
            profile.put("avatarUrl", user.getAvatarUrl());
            profile.put("fashionTitle", user.getFashionTitle());
            profile.put("githubId", user.getGithubId());
            profile.put("qqOpenId", user.getQqOpenId());
            data.put("profile", profile);
        }

        // 2. 所有目标（含子目标、协作人）
        List<Goal> goals = mongoTemplate.find(
                new Query(Criteria.where("owner").is(username)), Goal.class);
        data.put("goals", goals);

        // 3. 协作关系
        List<Relation> relations = mongoTemplate.find(
                new Query(Criteria.where("username").is(username)), Relation.class);
        data.put("relations", relations);

        // 4. 循环目标
        List<RecurringGoal> recurringGoals = mongoTemplate.find(
                new Query(Criteria.where("owner").is(username)), RecurringGoal.class);
        data.put("recurringGoals", recurringGoals);

        return data;
    }

    // ========================================================================
    // 预览接口：只返回统计信息
    // ========================================================================

    @PostMapping("/exportUserData")
    @CheckToken
    public ApiResponse exportUserData() {
        String username = getCurrentUser().getUsername();
        JSONObject data = getGoalData(username);
        return ApiResponse.success(data, "导出成功");
    }

    // ========================================================================
    // 根据格式下载
    // ========================================================================

    @PostMapping("/downloadUserData/{format}")
    @CheckToken
    public void downloadUserData(@PathVariable String format, HttpServletResponse response) {
        String username = getCurrentUser().getUsername();
        JSONObject data = getGoalData(username);

        String ts = TS_FMT.format(new Date());
        String baseName = "freemix-backup-" + username + "-" + ts;

        try {
            switch (format.toLowerCase()) {
                case "csv":
                    downloadCsv(data, baseName, response);
                    break;
                case "excel":
                    downloadExcel(data, baseName, response);
                    break;
                case "json":
                    downloadJson(data, baseName, response);
                    break;
                case "xml":
                    downloadXml(data, baseName, response);
                    break;
                case "sql":
                    downloadSql(data, baseName, response);
                    break;
                case "dmp":
                    downloadDmp(data, baseName, response);
                    break;
                default:
                    response.setStatus(400);
                    response.getWriter().write("Unsupported format: " + format);
            }
        } catch (Exception e) {
            // log.error("导出失败", e);
        }
    }

    // ========================================================================
    // JSON 导出
    // ========================================================================

    private void downloadJson(JSONObject data, String baseName, HttpServletResponse response) throws Exception {
        String jsonStr = JSON.toJSONString(data, JSONWriter.Feature.PrettyFormat);
        byte[] bytes = jsonStr.getBytes(StandardCharsets.UTF_8);

        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(baseName + ".json", "UTF-8") + "\"");
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    // ========================================================================
    // CSV 导出：目标拍平后转CSV，使用 Apache POI 的 SheetToCSV 逻辑
    // ========================================================================

    private void downloadCsv(JSONObject data, String baseName, HttpServletResponse response) throws Exception {
        StringBuilder sb = new StringBuilder();
        // BOM 使 Excel 正确识别 UTF-8
        sb.append('\uFEFF');

        JSONArray goals = data.getJSONArray("goals");
        if (goals == null || goals.isEmpty()) {
            sb.append("无目标数据\n");
        } else {
            // 收集所有字段名
            Set<String> allKeys = new LinkedHashSet<>();
            List<Map<String, String>> flatRows = new ArrayList<>();
            for (int i = 0; i < goals.size(); i++) {
                JSONObject g = goals.getJSONObject(i);
                Map<String, String> flat = flattenGoal(g);
                flatRows.add(flat);
                allKeys.addAll(flat.keySet());
            }
            // 写列头
            List<String> headers = new ArrayList<>(allKeys);
            sb.append(headers.stream().map(this::escapeCsv).collect(Collectors.joining(",")));
            sb.append("\n");
            // 写数据行
            for (Map<String, String> row : flatRows) {
                sb.append(headers.stream().map(h -> escapeCsv(row.getOrDefault(h, ""))).collect(Collectors.joining(",")));
                sb.append("\n");
            }
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(baseName + ".csv", "UTF-8") + "\"");
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    /** 拍平一个目标对象，数组字段转 JSON 字符串 */
    private Map<String, String> flattenGoal(JSONObject g) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String key : g.keySet()) {
            Object val = g.get(key);
            if (val == null) {
                map.put(key, "");
            } else if (val instanceof JSONArray || val instanceof JSONObject) {
                map.put(key, val.toString());
            } else if (val instanceof Date) {
                map.put(key, DATE_FMT.format((Date) val));
            } else {
                map.put(key, val.toString());
            }
        }
        return map;
    }

    /** CSV 转义（双引号包裹含逗号/引号/换行的值） */
    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    // ========================================================================
    // Excel 导出：多 sheet 工作簿
    // ========================================================================

    private void downloadExcel(JSONObject data, String baseName, HttpServletResponse response) throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            // profile sheet
            JSONObject profile = data.getJSONObject("profile");
            if (profile != null && !profile.isEmpty()) {
                Sheet sheet = wb.createSheet("profile");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("field");
                header.createCell(1).setCellValue("value");
                int r = 1;
                for (String key : profile.keySet()) {
                    Row row = sheet.createRow(r++);
                    row.createCell(0).setCellValue(key);
                    row.createCell(1).setCellValue(profile.get(key) != null ? profile.get(key).toString() : "");
                }
                sheet.autoSizeColumn(0);
                sheet.autoSizeColumn(1);
            }

            // goals sheet（拍平后）
            JSONArray goals = data.getJSONArray("goals");
            if (goals != null && !goals.isEmpty()) {
                Sheet sheet = wb.createSheet("goals");
                List<Map<String, String>> flatRows = new ArrayList<>();
                Set<String> allKeys = new LinkedHashSet<>();
                for (int i = 0; i < goals.size(); i++) {
                    Map<String, String> flat = flattenGoal(goals.getJSONObject(i));
                    flatRows.add(flat);
                    allKeys.addAll(flat.keySet());
                }
                List<String> headers = new ArrayList<>(allKeys);
                Row headerRow = sheet.createRow(0);
                for (int c = 0; c < headers.size(); c++) {
                    headerRow.createCell(c).setCellValue(headers.get(c));
                }
                for (int r = 0; r < flatRows.size(); r++) {
                    Row row = sheet.createRow(r + 1);
                    Map<String, String> flat = flatRows.get(r);
                    for (int c = 0; c < headers.size(); c++) {
                        row.createCell(c).setCellValue(flat.getOrDefault(headers.get(c), ""));
                    }
                }
                for (int c = 0; c < headers.size(); c++) {
                    sheet.autoSizeColumn(c);
                }
            }

            // relations sheet
            JSONArray relations = data.getJSONArray("relations");
            writeArrayToSheet(wb, "relations", relations);

            // recurringGoals sheet
            JSONArray recurringGoals = data.getJSONArray("recurringGoals");
            writeArrayToSheet(wb, "recurringGoals", recurringGoals);

            // 输出
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(baseName + ".xlsx", "UTF-8") + "\"");
            wb.write(response.getOutputStream());
        }
    }

    /** 通用：将 JSONArray 写入一个 sheet */
    private void writeArrayToSheet(Workbook wb, String sheetName, JSONArray arr) {
        if (arr == null || arr.isEmpty()) return;
        Sheet sheet = wb.createSheet(sheetName.length() > 31 ? sheetName.substring(0, 31) : sheetName);
        // 收集所有字段
        Set<String> keys = new LinkedHashSet<>();
        List<JSONObject> rows = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            rows.add(obj);
            keys.addAll(obj.keySet());
        }
        List<String> headers = new ArrayList<>(keys);
        Row headerRow = sheet.createRow(0);
        for (int c = 0; c < headers.size(); c++) {
            headerRow.createCell(c).setCellValue(headers.get(c));
        }
        for (int r = 0; r < rows.size(); r++) {
            Row row = sheet.createRow(r + 1);
            JSONObject obj = rows.get(r);
            for (int c = 0; c < headers.size(); c++) {
                Object val = obj.get(headers.get(c));
                row.createCell(c).setCellValue(val != null ? val.toString() : "");
            }
        }
        for (int c = 0; c < headers.size(); c++) {
            sheet.autoSizeColumn(c);
        }
    }

    // ========================================================================
    // XML 导出
    // ========================================================================

    private void downloadXml(JSONObject data, String baseName, HttpServletResponse response) throws Exception {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<freemixBackup>\n");
        xml.append(xmlObject("", data, 1));
        xml.append("</freemixBackup>\n");

        byte[] bytes = xml.toString().getBytes(StandardCharsets.UTF_8);
        response.setContentType("application/xml;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(baseName + ".xml", "UTF-8") + "\"");
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    private String xmlObject(String key, Object value, int indent) {
        String pad = "  ".repeat(indent);
        StringBuilder sb = new StringBuilder();
        if (value instanceof JSONArray arr) {
            sb.append(pad).append("<").append(key).append(">\n");
            for (int i = 0; i < arr.size(); i++) {
                Object item = arr.get(i);
                if (item instanceof JSONObject obj) {
                    sb.append(pad).append("  <item index=\"").append(i).append("\">\n");
                    for (String k : obj.keySet()) {
                        sb.append(xmlObject(k, obj.get(k), indent + 2));
                    }
                    sb.append(pad).append("  </item>\n");
                } else {
                    sb.append(pad).append("  <item>").append(escapeXml(item)).append("</item>\n");
                }
            }
            sb.append(pad).append("</").append(key).append(">\n");
        } else if (value instanceof JSONObject obj) {
            sb.append(pad).append("<").append(key).append(">\n");
            for (String k : obj.keySet()) {
                sb.append(xmlObject(k, obj.get(k), indent + 1));
            }
            sb.append(pad).append("</").append(key).append(">\n");
        } else {
            sb.append(pad).append("<").append(key).append(">").append(escapeXml(value)).append("</").append(key).append(">\n");
        }
        return sb.toString();
    }

    private String escapeXml(Object v) {
        if (v == null) return "";
        String s = v.toString();
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    // ========================================================================
    // SQL 导出（MySQL 风格）
    // ========================================================================

    private void downloadSql(JSONObject data, String baseName, HttpServletResponse response) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("-- ========================================\n");
        sql.append("-- Freemix 目标数据备份\n");
        sql.append("-- 导出时间: ").append(DATE_FMT.format(new Date())).append("\n");
        sql.append("-- ========================================\n\n");
        sql.append("SET NAMES utf8mb4;\n\n");

        // goals 表
        sql.append(buildCreateTableSql("goals", data.getJSONArray("goals")));
        sql.append(buildInsertSql("goals", data.getJSONArray("goals")));

        // relations 表
        sql.append(buildCreateTableSql("relations", data.getJSONArray("relations")));
        sql.append(buildInsertSql("relations", data.getJSONArray("relations")));

        // recurring_goals 表
        sql.append(buildCreateTableSql("recurring_goals", data.getJSONArray("recurringGoals")));
        sql.append(buildInsertSql("recurring_goals", data.getJSONArray("recurringGoals")));

        byte[] bytes = sql.toString().getBytes(StandardCharsets.UTF_8);
        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(baseName + ".sql", "UTF-8") + "\"");
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    /** 从 JSONArray 推断字段生成 CREATE TABLE */
    private String buildCreateTableSql(String tableName, JSONArray arr) {
        StringBuilder sb = new StringBuilder();
        sb.append("DROP TABLE IF EXISTS `").append(tableName).append("`;\n");
        sb.append("CREATE TABLE `").append(tableName).append("` (\n");
        sb.append("  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,\n");

        if (arr != null && !arr.isEmpty()) {
            // 收集所有字段
            Set<String> keys = new LinkedHashSet<>();
            for (int i = 0; i < arr.size(); i++) {
                keys.addAll(arr.getJSONObject(i).keySet());
            }
            List<String> keyList = new ArrayList<>(keys);
            for (int i = 0; i < keyList.size(); i++) {
                String col = keyList.get(i);
                String type = inferSqlType(arr, col);
                sb.append("  `").append(col).append("` ").append(type);
                if (i < keyList.size() - 1) sb.append(",");
                sb.append("\n");
            }
        }

        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n\n");
        return sb.toString();
    }

    /** 根据数据推断列的 SQL 类型 */
    private String inferSqlType(JSONArray arr, String col) {
        for (int i = 0; i < Math.min(arr.size(), 100); i++) {
            Object val = arr.getJSONObject(i).get(col);
            if (val instanceof Number) {
                double v = ((Number) val).doubleValue();
                if (v == Math.floor(v) && !Double.isInfinite(v)) {
                    if (Math.abs(v) > 2147483647L) return "BIGINT";
                    return "INT";
                }
                return "DOUBLE";
            }
            if (val instanceof Boolean) return "TINYINT(1)";
            if (val != null) {
                String s = val.toString();
                if (s.length() > 65535) return "LONGTEXT";
                if (s.length() > 255) return "TEXT";
                return "VARCHAR(255)";
            }
        }
        return "TEXT";
    }

    /** 生成 INSERT 语句 */
    private String buildInsertSql(String tableName, JSONArray arr) {
        if (arr == null || arr.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();

        // 收集所有字段
        Set<String> keys = new LinkedHashSet<>();
        for (int i = 0; i < arr.size(); i++) {
            keys.addAll(arr.getJSONObject(i).keySet());
        }
        List<String> keyList = new ArrayList<>(keys);

        sb.append("INSERT INTO `").append(tableName).append("` (\n");
        sb.append("  `").append(String.join("`, `", keyList)).append("`\n) VALUES\n");

        List<String> valueRows = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            List<String> vals = new ArrayList<>();
            for (String col : keyList) {
                vals.add(escapeSql(obj.get(col)));
            }
            valueRows.add("(" + String.join(", ", vals) + ")");
        }
        sb.append(String.join(",\n", valueRows));
        sb.append(";\n\n");
        return sb.toString();
    }

    private String escapeSql(Object v) {
        if (v == null) return "NULL";
        if (v instanceof Number || v instanceof Boolean) return v.toString();
        String s = v.toString().replace("'", "''");
        return "'" + s + "'";
    }

    // ========================================================================
    // DMP 导出（Oracle 兼容 SQL）
    // ========================================================================

    private void downloadDmp(JSONObject data, String baseName, HttpServletResponse response) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("-- ========================================\n");
        sb.append("-- Freemix 数据重建脚本 (DMP 替代)\n");
        sb.append("-- 说明：Oracle DMP 为专有二进制格式，无法在浏览器端生成。\n");
        sb.append("--       此文件包含完整的数据重建 SQL，可用于恢复目标数据。\n");
        sb.append("-- 导出时间: ").append(DATE_FMT.format(new Date())).append("\n");
        sb.append("-- ========================================\n\n");
        sb.append("DEFINE SCHEMA_NAME = 'FREEMIX';\n\n");

        // goals 表
        buildOracleTable(sb, "GOALS", data.getJSONArray("goals"));
        // relations 表
        buildOracleTable(sb, "RELATIONS", data.getJSONArray("relations"));
        // recurring_goals 表
        buildOracleTable(sb, "RECURRING_GOALS", data.getJSONArray("recurringGoals"));

        sb.append("COMMIT;\n");

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(baseName + ".dmp", "UTF-8") + "\"");
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    private void buildOracleTable(StringBuilder sb, String tableName, JSONArray arr) {
        sb.append("-- --------------------------------------------------\n");
        sb.append("-- 表结构: ").append(tableName).append("\n");
        sb.append("-- --------------------------------------------------\n\n");

        sb.append("CREATE TABLE &SCHEMA_NAME..").append(tableName).append(" (\n");

        if (arr != null && !arr.isEmpty()) {
            Set<String> keys = new LinkedHashSet<>();
            for (int i = 0; i < arr.size(); i++) {
                keys.addAll(arr.getJSONObject(i).keySet());
            }
            List<String> keyList = new ArrayList<>(keys);
            for (int i = 0; i < keyList.size(); i++) {
                String col = keyList.get(i).toUpperCase();
                String type = inferOracleType(arr, keyList.get(i));
                sb.append("  ").append(col).append(" ").append(type);
                if (i < keyList.size() - 1) sb.append(",");
                sb.append("\n");
            }
        }
        sb.append(");\n\n");

        // 主索引
        sb.append("CREATE INDEX IDX_").append(tableName).append("_OWNER ON &SCHEMA_NAME..").append(tableName).append("(OWNER);\n\n");

        // 数据
        if (arr != null && !arr.isEmpty()) {
            Set<String> keys = new LinkedHashSet<>();
            for (int i = 0; i < arr.size(); i++) {
                keys.addAll(arr.getJSONObject(i).keySet());
            }
            List<String> keyList = new ArrayList<>(keys);

            sb.append("-- 数据插入: ").append(tableName).append("\n\n");
            for (int i = 0; i < arr.size(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                sb.append("INSERT INTO &SCHEMA_NAME..").append(tableName).append(" (");
                sb.append(keyList.stream().map(String::toUpperCase).collect(Collectors.joining(", ")));
                sb.append(") VALUES (");
                List<String> vals = new ArrayList<>();
                for (String col : keyList) {
                    vals.add(escapeOracleValue(obj.get(col)));
                }
                sb.append(String.join(", ", vals));
                sb.append(");\n");
            }
            sb.append("\n");
        }
    }

    private String inferOracleType(JSONArray arr, String col) {
        for (int i = 0; i < Math.min(arr.size(), 100); i++) {
            Object val = arr.getJSONObject(i).get(col);
            if (val instanceof Number) {
                double v = ((Number) val).doubleValue();
                if (v == Math.floor(v) && !Double.isInfinite(v)) {
                    if (Math.abs(v) > 2147483647L) return "NUMBER(20)";
                    return "NUMBER(10)";
                }
                return "FLOAT";
            }
            if (val instanceof Boolean) return "NUMBER(1)";
            if (val != null) {
                String s = val.toString();
                if (s.length() > 4000) return "CLOB";
                return "VARCHAR2(4000)";
            }
        }
        return "CLOB";
    }

    private String escapeOracleValue(Object v) {
        if (v == null) return "NULL";
        if (v instanceof Number || v instanceof Boolean) return v.toString();
        String s = v.toString().replace("'", "''");
        return "'" + s + "'";
    }
}
