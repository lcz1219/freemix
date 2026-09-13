package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/ai-messages")
@CrossOrigin(origins = "*")
public class AIChatController extends BaseController {

    @Autowired
    private DeepSeekService deepSeekService;

    /**
     * 统一的 AI 流式代理接口，前端只需要连自己的后端即可。
     */
    @PostMapping(value = "/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @CheckToken
    public ResponseEntity<StreamingResponseBody> chatStream(@RequestBody JSONObject body) {
        String question = body.getString("question");
        User currentUser = getCurrentUser();
        String currentUsername = currentUser == null ? "" : currentUser.getUsername();

        try {
            HttpURLConnection connection = deepSeekService.createChatStreamConnection(question, currentUsername);
            int statusCode = connection.getResponseCode();
            InputStream inputStream = statusCode >= 200 && statusCode < 300 ? connection.getInputStream() : connection.getErrorStream();

            if (statusCode < 200 || statusCode >= 300) {
                String errorMessage = readAll(inputStream);
                connection.disconnect();
                return ResponseEntity.status(statusCode)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(outputStream -> outputStream.write(errorMessage.getBytes(StandardCharsets.UTF_8)));
            }

            StreamingResponseBody responseBody = outputStream -> {
                try (InputStream stream = inputStream) {
                    // 第一轮：生成回答或 MQL；MQL 段不下发前端，只在服务端保留管道语句
                    String mqlPipeline = deepSeekService.forwardDeepSeekStream(stream, outputStream);

                    // 命中数据查询：服务端执行聚合 + 第二轮总结，原始数据全程不经过浏览器
                    if (mqlPipeline != null && !mqlPipeline.isBlank()) {
                        deepSeekService.streamMqlSummary(mqlPipeline, question, currentUsername, outputStream);
                    }

                    // 整个编排只写一次结束标记，避免第二轮流被前端提前中断
                    deepSeekService.writeDone(outputStream);
                } finally {
                    connection.disconnect();
                }
            };

            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue())
                    .header(HttpHeaders.CONNECTION, "keep-alive")
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body(responseBody);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(outputStream -> outputStream.write(e.getMessage().getBytes(StandardCharsets.UTF_8)));
        }
    }

    private String readAll(InputStream inputStream) {
        if (inputStream == null) {
            return "DeepSeek 请求失败";
        }
        try (InputStream stream = inputStream;
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            stream.transferTo(outputStream);
            return outputStream.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "DeepSeek 请求失败";
        }
    }
}
