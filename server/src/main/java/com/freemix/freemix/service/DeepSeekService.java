package com.freemix.freemix.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.configurer.DeepSeekConfig;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class DeepSeekService {

    @Autowired
    private DeepSeekConfig deepSeekConfig;

    /**
     * 建立到 DeepSeek 的流式连接，后续由控制器把结果转发给前端。
     */
    public HttpURLConnection createChatStreamConnection(String question, String currentUsername) throws IOException {
        if (deepSeekConfig.getApiKey() == null || deepSeekConfig.getApiKey().isBlank()) {
            throw new IllegalStateException("DeepSeek API Key 未配置");
        }

        String baseUrl = deepSeekConfig.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.deepseek.com";
        }

        String model = deepSeekConfig.getModel();
        if (model == null || model.isBlank()) {
            model = "deepseek-reasoner";
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("stream", true);

        JSONArray messages = new JSONArray();
        messages.add(createMessage("system", buildSystemPrompt(currentUsername)));
        messages.add(createMessage("user", question));
        requestBody.put("messages", messages);

        HttpURLConnection connection = (HttpURLConnection) URI.create(baseUrl + "/chat/completions").toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(0);
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Bearer " + deepSeekConfig.getApiKey());
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "text/event-stream");

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(requestBody.toJSONString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
        return connection;
    }

    /**
     * 把 DeepSeek 的 OpenAI 兼容流式结果转换成当前前端已在使用的消息结构。
     */
    public void forwardDeepSeekStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data:")) {
                    continue;
                }

                String data = line.substring(5).trim();
                if (data.isEmpty()) {
                    continue;
                }

                if ("[DONE]".equals(data)) {
                    writer.write("data: [DONE]\n\n");
                    writer.flush();
                    break;
                }

                try {
                    JSONObject jsonObject = JSONObject.parseObject(data);
                    JSONArray choices = jsonObject.getJSONArray("choices");
                    if (choices == null || choices.isEmpty()) {
                        continue;
                    }

                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject delta = choice.getJSONObject("delta");
                    if (delta == null || delta.isEmpty()) {
                        continue;
                    }

                    String content = delta.getString("content");
                    String reasoningContent = delta.getString("reasoning_content");

                    if ((content == null || content.isEmpty()) && (reasoningContent == null || reasoningContent.isEmpty())) {
                        continue;
                    }

                    JSONObject message = new JSONObject();
                    if (reasoningContent != null && !reasoningContent.isEmpty()) {
                        message.put("type", "thinking");
                        message.put("reasoning_content", reasoningContent);
                        message.put("content", content == null ? "" : content);
                    } else {
                        message.put("type", "answer");
                        message.put("content", content);
                    }

                    JSONObject wrapper = new JSONObject();
                    wrapper.put("message", message);

                    writer.write("data: " + wrapper.toJSONString() + "\n\n");
                    writer.flush();
                } catch (Exception ignored) {
                }
            }
        }
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
                """.formatted(username, LocalDateTime.now(), username);
    }
}
