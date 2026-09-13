package com.freemix.freemix.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.configurer.DeepSeekConfig;
import com.mongodb.client.AggregateIterable;
import org.bson.BsonArray;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DeepSeekService {

    @Autowired
    private DeepSeekConfig deepSeekConfig;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 从 MongoDB DeepSeek 集合中读取 apiKey
     */
    private String getApiKey() {
        JSONObject config = mongoTemplate.findOne(
                new Query().limit(1),
                JSONObject.class,
                "DeepSeek"
        );
        if (config == null || !config.containsKey("apiKey")) {
            throw new IllegalStateException("MongoDB DeepSeek 集合中未找到 apiKey");
        }
        String apiKey = config.getString("apiKey");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("MongoDB DeepSeek 集合中的 apiKey 为空");
        }
        return apiKey;
    }

    /**
     * 建立到 DeepSeek 的流式连接，后续由控制器把结果转发给前端。
     */
    public HttpURLConnection createChatStreamConnection(String question, String currentUsername) throws IOException {
        JSONArray messages = new JSONArray();
        messages.add(createMessage("system", buildSystemPrompt(currentUsername)));
        messages.add(createMessage("user", question));
        return openStreamConnection(messages);
    }

    /**
     * 用给定的 messages 建立 DeepSeek 流式连接（第二轮总结复用同一套连接配置）
     */
    private HttpURLConnection openStreamConnection(JSONArray messages) throws IOException {
        String apiKey = getApiKey();

        String baseUrl = deepSeekConfig.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.deepseek.com";
        }

        String model = deepSeekConfig.getModel();
//        if (model == null || model.isBlank()) {
//            model = "deepseek-reasoner";
//        }
            model = "deepseek-v4-flash";


        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("stream", true);
        requestBody.put("messages", messages);

        HttpURLConnection connection = (HttpURLConnection) URI.create(baseUrl + "/chat/completions").toURL().openConnection();
        connection.setRequestMethod("POST");
//        connection.setConnectTimeout(90000);
//        connection.setReadTimeout(0);
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "text/event-stream");

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(requestBody.toJSONString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
        return connection;
    }

    /**
     * 把 DeepSeek 的 OpenAI 兼容流式结果转换成前端消息结构。
     * 正文即时推送，但把可能是 [推荐问题] / [MQL_START] 标记前缀的尾部扣在缓冲里，确认后再发，
     * 避免标记被切成多个分片时泄漏到正文。
     * MQL 段不会下发给前端，只在服务端解析出管道语句用于后续查询。
     *
     * @return 本轮回答中截获到的 MQL 管道语句；没有则返回 null
     */
    public String forwardDeepSeekStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        StringBuilder fullContent = new StringBuilder();
        // 扣留缓冲：暂存可能是标记前缀的尾部，未确认前不发送
        StringBuilder lookbehind = new StringBuilder();
        boolean followUpStarted = false;
        boolean mqlStarted = false;
        final String MARKER = "[推荐问题]";
        final String MQL_START = "[MQL_START]";
        final String MQL_END = "[MQL_END]";

        // writer 不能放进 try-with-resources：close 会连带关闭调用方的输出流，
        // 导致编排时无法继续写第二轮内容和统一的 [DONE]
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data:")) continue;
                String data = line.substring(5).trim();
                if (data.isEmpty()) continue;
                if ("[DONE]".equals(data)) break;

                try {
                    JSONObject jsonObject = JSONObject.parseObject(data);
                    JSONArray choices = jsonObject.getJSONArray("choices");
                    if (choices == null || choices.isEmpty()) continue;

                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject delta = choice.getJSONObject("delta");
                    if (delta == null || delta.isEmpty()) continue;

                    String content = delta.getString("content");
                    String reasoningContent = delta.getString("reasoning_content");

                    if (content != null && !content.isEmpty()) {
                        fullContent.append(content);
                    }

                    boolean hasContent = content != null && !content.isEmpty();
                    boolean hasReasoning = reasoningContent != null && !reasoningContent.isEmpty();
                    if (!hasContent && !hasReasoning) continue;

                    if (followUpStarted || mqlStarted) continue;

                    // 思考过程即时转发
                    if (hasReasoning) {
                        JSONObject msg = new JSONObject();
                        msg.put("type", "thinking");
                        msg.put("reasoning_content", reasoningContent);
                        JSONObject wrapper = new JSONObject();
                        wrapper.put("message", msg);
                        writer.write("data: " + wrapper.toJSONString() + "\n\n");
                        writer.flush();
                    }

                    // 正文：即时推送，但把可能是标记前缀的尾部扣在缓冲里
                    if (hasContent) {
                        // combined = 上一轮扣留的内容 + 当前分片
                        String combined = lookbehind + content;
                        int followIdx = combined.indexOf(MARKER);
                        int mqlIdx = combined.indexOf(MQL_START);

                        if (followIdx >= 0 || mqlIdx >= 0) {
                            // 命中标记：只发标记之前的内容，标记及其之后的内容不下发前端
                            int cut = followIdx < 0 ? mqlIdx : (mqlIdx < 0 ? followIdx : Math.min(followIdx, mqlIdx));
                            if (cut > 0) {
                                writeImmediate(combined.substring(0, cut), writer);
                            }
                            if (followIdx >= 0) {
                                followUpStarted = true;
                            }
                            if (mqlIdx >= 0) {
                                mqlStarted = true;
                            }
                            lookbehind.setLength(0);
                        } else {
                            // 找出末尾可能是标记前缀的长度（如 "[推荐问题" / "[MQL_START"）
                            int partialLen = 0;
                            for (String marker : new String[]{MARKER, MQL_START}) {
                                for (int i = marker.length() - 1; i >= 1; i--) {
                                    if (combined.endsWith(marker.substring(0, i))) {
                                        partialLen = Math.max(partialLen, i);
                                        break;
                                    }
                                }
                            }
                            int safeLen = combined.length() - partialLen;
                            if (safeLen > 0) {
                                writeImmediate(combined.substring(0, safeLen), writer);
                            }
                            // 扣住可能是标记开头的尾部，等下一分片一起判断
                            lookbehind.setLength(0);
                            lookbehind.append(combined.substring(safeLen));
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            // 流结束仍未出现标记，把缓冲里扣住的尾部补发，避免正文丢字
            if (!followUpStarted && !mqlStarted && lookbehind.length() > 0) {
                writeImmediate(lookbehind.toString(), writer);
            }

            // 从累积内容中解析 [推荐问题] 并单独推送
            String contentText = fullContent.toString();
            if (contentText.contains(MARKER)) {
                List<String> questions = parseFollowUpQuestions(contentText);
                if (!questions.isEmpty()) {
                    JSONObject followUpMsg = new JSONObject();
                    followUpMsg.put("type", "follow_up");
                    followUpMsg.put("content", JSONObject.toJSONString(questions));
                    JSONObject wrapper = new JSONObject();
                    wrapper.put("message", followUpMsg);
                    writer.write("data: " + wrapper.toJSONString() + "\n\n");
                    writer.flush();
                }
            }

            // 截获 MQL 管道语句（仅服务端使用，不下发前端）
            String pipeline = null;
            int mqlStart = contentText.indexOf(MQL_START);
            if (mqlStart >= 0) {
                int mqlEnd = contentText.indexOf(MQL_END, mqlStart + MQL_START.length());
                if (mqlEnd > mqlStart) {
                    pipeline = contentText.substring(mqlStart + MQL_START.length(), mqlEnd).trim();
                }
            }

            writer.flush();
            return pipeline;
        }
    }

    /**
     * 写 SSE 结束标记；由编排方在所有轮次结束后统一调用一次
     */
    public void writeDone(OutputStream outputStream) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        writer.write("data: [DONE]\n\n");
        writer.flush();
    }

    /**
     * 第二阶段编排：服务端执行 MQL 拿数据，再让 DeepSeek 基于真实数据做总结。
     * 原始数据全程只在服务端流转，浏览器只能拿到最终的自然语言结论。
     */
    public void streamMqlSummary(String pipeline, String question, String currentUsername, OutputStream outputStream) throws IOException {
        HttpURLConnection connection = null;
        try {
            List<Document> rawData = executeMqlPipeline(pipeline, "goal");
            JSONArray messages = new JSONArray();
            messages.add(createMessage("system", buildSystemPrompt(currentUsername)));
            messages.add(createMessage("user", buildMqlSummaryPrompt(question, rawData)));
            connection = openStreamConnection(messages);

            int statusCode = connection.getResponseCode();
            if (statusCode < 200 || statusCode >= 300) {
                throw new IOException("总结阶段调用 DeepSeek 失败，状态码：" + statusCode);
            }
            try (InputStream stream = connection.getInputStream()) {
                // 第二轮不再使用截获结果，避免再次触发查询造成死循环
                forwardDeepSeekStream(stream, outputStream);
            }
        } catch (Exception e) {
            // 降级提示：避免前端一直等待
            writeAnswerChunk("AI正在处理您的数据，请重新刷新试试", outputStream);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 执行 AI 生成的 MQL（MongoDB 聚合管道），返回查询结果
     */
    public List<Document> executeMqlPipeline(String pipelineJson, String collectionName) {
        if (collectionName == null || collectionName.isEmpty()) {
            collectionName = "goal";
        }
        String cleanedMql = pipelineJson
                .replaceAll("ISODate\\(\"([^\"]+)\"\\)", "{\"\\$date\": \"$1\"}")
                .replaceAll("new Date\\(\"([^\"]+)\"\\)", "{\"\\$date\": \"$1\"}");
        // 处理算术表达式 (例如 1000 * 60 * 60 * 24)
        cleanedMql = evaluateArithmetic(cleanedMql);

        List<Document> pipeline = BsonArray.parse(cleanedMql).stream()
                .map(v -> Document.parse(v.asDocument().toJson()))
                .collect(Collectors.toList());
        AggregateIterable<Document> results = mongoTemplate.getCollection(collectionName).aggregate(pipeline);
        List<Document> output = new ArrayList<>();
        results.forEach(output::add);
        return output;
    }

    /**
     * 与前端 mqlSummaryPrompt 保持一致的第二阶段提问模板
     */
    private String buildMqlSummaryPrompt(String question, List<Document> rawData) {
        StringBuilder dataJson = new StringBuilder("[");
        for (int i = 0; i < rawData.size(); i++) {
            if (i > 0) {
                dataJson.append(",");
            }
            // 用 MongoDB 扩展 JSON 输出，日期格式与系统提示词里的要求保持一致
            dataJson.append(rawData.get(i).toJson());
        }
        dataJson.append("]");

        return "\n      用户问题：" + question
                + "\n      数据库执行结果（原始数据）：" + dataJson
                + "\n      请结合上述数据，用专业、自然的口吻回答用户，并给出分析结论。不要再次输出 [MQL_START] 标签。\n      ";
    }

    /**
     * 简单的算术表达式评估器，专门用于处理 JSON 中的数字乘法 (如 1000 * 60 * 60)
     */
    private String evaluateArithmetic(String input) {
        if (input == null || !input.contains("*")) return input;

        // 匹配数字之间的乘号，支持多级乘法
        Pattern pattern = Pattern.compile("(\\d+(?:\\s*\\*\\s*\\d+)+)");
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String expression = matcher.group(1);
            try {
                long result = 1;
                String[] parts = expression.split("\\*");
                for (String part : parts) {
                    result *= Long.parseLong(part.trim());
                }
                matcher.appendReplacement(sb, String.valueOf(result));
            } catch (Exception e) {
                // 如果解析失败，保留原样
                matcher.appendReplacement(sb, Matcher.quoteReplacement(expression));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 直接向下发流写一条 answer 消息（用于失败降级提示）
     */
    private void writeAnswerChunk(String content, OutputStream outputStream) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        writeImmediate(content, writer);
    }

    private void writeImmediate(String content, BufferedWriter writer) throws IOException {
        JSONObject message = new JSONObject();
        message.put("type", "answer");
        message.put("content", content);
        JSONObject wrapper = new JSONObject();
        wrapper.put("message", message);
        writer.write("data: " + wrapper.toJSONString() + "\n\n");
        writer.flush();
    }

    /**
     * 从回答文本中提取 [推荐问题] 之后的问题列表
     */
    private List<String> parseFollowUpQuestions(String content) {
        List<String> questions = new ArrayList<>();
        int markerIndex = content.indexOf("[推荐问题]");
        if (markerIndex < 0) {
            return questions;
        }

        String afterMarker = content.substring(markerIndex + "[推荐问题]".length());
        String[] lines = afterMarker.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            // 跳过空行和 markdown 分隔线
            if (!trimmed.isEmpty() && !trimmed.matches("^[-*_=]+$")) {
                questions.add(trimmed);
            }
            if (questions.size() >= 3) {
                break;
            }
        }
        return questions;
    }

    private JSONObject createMessage(String role, String content) {
        JSONObject message = new JSONObject();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    /**
     * 这里把原来扣子空间里的角色设定落到后端，避免前端泄露配置。
     */
    private String buildSystemPrompt(String currentUsername) {
        String username = currentUsername == null || currentUsername.isBlank() ? "" : currentUsername;
        return """
                角色
                你是一名资深目标管理专家，专注于帮助个人和团队通过科学方法设定、跟踪和优化目标，提升执行效率与成果质量。
                你的目标是协助用户完成目标管理的全流程，包括目标生成、进度监控、数据分析和反馈优化，最终实现目标的可视化、可衡量和可持续达成。

                工作步骤
                先判断是否需要生成sql查数据才得以分析的，如果不用则按1实现，否则按2实现。

                1.目标设定与智能规划
                进度跟踪与数据整合
                反馈生成与迭代优化

                第一步 目标设定与智能规划
                在此步骤中，你需要根据用户输入的业务场景、个人需求或组织战略，生成结构化、可执行的目标方案。
                具体要求包括：
                目标生成：结合用户提供的愿景、政策要求或行业基准，使用SMART原则生成初步目标清单。
                行动分解：将宏观目标拆解为可操作的任务步骤。
                资源匹配：推荐所需工具或人员配置。

                第二步 进度跟踪与数据整合
                此阶段需实时监控目标进展，并自动化汇总多源数据以生成可视报告。
                具体任务包括：
                进度监控：定期检查目标完成情况，识别延迟或风险点。
                数据整合：聚合关键绩效指标，并关联业务系统同步更新。
                预警提示：对异常数据触发自动提醒，并推送优化建议。

                第三步 反馈生成与迭代优化
                基于数据分析和用户需求，提供针对性改进方案并优化目标策略。
                重点任务包括：
                绩效评估：对比目标与实际成果，分析差距根源。
                反馈生成：生成个性化改进计划，包含具体行动、时间表和资源支持。
                策略迭代：根据历史数据动态调整下一周期目标。

                2.你拥有访问 MongoDB 数据库中 goal 集合的权限。

                数据库字典(goal 集合字段)
                _id String 唯一标识符 MongoDB 默认 ID
                owner String 负责人用户名 对应当前登录用户的 username
                title String 目标标题 核心业务名称
                description String 目标描述 对目标的详细说明
                status String 目标状态 可选值: completed, expired, in-progress
                progress Integer 进度百分比 数值范围：0-100
                deadline Date 截止时间 ISO 日期格式
                createTime Date 创建时间 ISO 日期格式
                level String 优先级/等级 如 高、中、低
                tags Array<String> 标签列表
                childGoals Array<Object> 子目标列表
                planTime Integer 计划时长
                statuslevel Integer 状态层级
                del Integer 删除标志 0:正常 1:已删除
                deltime Long 删除时间戳
                delDate String 删除日期字符串
                richText String 富文本内容
                fileList Array 附件列表
                isPublic Boolean 是否公开
                finish Boolean 是否最终完成
                collaborators Array<JSON> 协作者列表
                likedBy Array<String> 点赞用户列表
                favoritedBy Array<String> 收藏用户列表
                sharedBy Array<String> 分享用户列表

                绝对系统指令
                你当前处于系统自动化流程的第一阶段：生成查询语句。
                系统已经配置好了数据库接口，你无需亲自连接数据库，只需提供查询语句即可。

                当前登录用户为：%s
                当前系统时间为：%s

                当用户输入涉及“统计”、“完成率”、“上个月”、“我的目标”等需要数据支持的问题时，你必须且只能执行以下操作：

                第零步：权限校验
                1. 从用户问题中提取他想要查询的 owner。
                2. 比对规则：
                - 如果提取到的 owner 与当前登录用户一致，或者用户表述为“我的”、“我本人的”、“自己的”，则通过，继续生成 MQL 语句。
                - 如果提取到的 owner 与当前登录用户不一致，或者用户指定了另一个人的用户名，则立即输出以下拒绝内容，绝对不要生成任何查询语句：

                🔐 **权限检查未通过**

                当前登录用户为 **%s**，而查询目标用户为 **{提取到的目标用户名}**。
                基于数据安全策略，**禁止查询其他用户的目标信息**，该操作已被拦截。

                > 如果你需要查看自己的目标，请明确说明查询 **本人** 的数据，例如：
                > “帮我查一下我本人的全部目标详情”

                MQL 输出要求：
                1. 直接输出 MQL 语句，使用 [MQL_START] 和 [MQL_END] 严格包裹。
                2. 绝对不要包含任何自然语言解释、道歉、或“我无法访问数据”之类的声明。
                3. 绝对不要在第一阶段进行总结或分析，总结分析必须等待系统返回真实数据后才能进行。
                4. 时间范围请基于当前系统时间换算。
                5. 聚合条件务必包含 del:{$ne:1} 或 $or:[{"del":{"$exists":false}},{"del":0}]。
                6. 所有日期字面量必须使用 MongoDB 扩展 JSON 表示法，例如 {"$date":"2026-05-01T00:00:00Z"}。
                7. 严禁使用 ISODate()、new Date() 等 JS 函数。
                8. 所有数字表达式必须提前计算成最终数值。
                9. 输出的整个管道必须是一个合法的 JSON 数组，能够被 MongoDB Java Driver 的 Document.parse() 直接解析。
                10. 在输出最终 MQL 前再次自检 MongoDB 关键字和 JSON 语法是否正确。

                如果用户的问题不需要数据库查询，则按普通目标管理专家身份直接回答。
                默认使用中文回答，必要时可使用 Markdown 提高可读性。

                推荐问题要求：
                在你每次回答的末尾，必须单独起一行添加 [推荐问题] 标记，然后紧接2-3个用户可能追问的相关问题，每行一个问题。
                格式示例：
                [推荐问题]
                如何制定每周的执行计划？
                有哪些工具可以帮助跟踪进度？
                如果目标未达成应该怎么办？
                注意：[推荐问题] 和后面的问题必须放在回答的最末尾，中间不要有空行。
                """.formatted(username, LocalDateTime.now(), username);
    }
}
