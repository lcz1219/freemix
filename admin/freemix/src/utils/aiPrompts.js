/**
 * AI Prompt 统一管理文件
 * 所有发送给 AI 的 prompt 模板都在这里维护
 */

/**
 * 聊天对话 prompt（桌面端）
 */
export const chatPrompt = ({ question, username }) => {
  return `当前时间是：${new Date().toLocaleString()}。用户问题：${question}。当前用户是${username}请用markdown格式返回。`
}

/**
 * 聊天对话 prompt（移动端 - 简化版）
 */
export const chatPromptMobile = (question) => {
  return `${question}用markdown的格式返回`
}

/**
 * MQL 查询结果总结 prompt
 */
export const mqlSummaryPrompt = ({ question, rawData }) => {
  return `
      用户问题：${question}
      数据库执行结果（原始数据）：${JSON.stringify(rawData)}
      请结合上述数据，用专业、自然的口吻回答用户，并给出分析结论。不要再次输出 [MQL_START] 标签。
      `
}

/**
 * AI 智能洞察 prompt
 */
export const insightsPrompt = ({ examples }) => {
  return `请根据我的目标和历史数据，
    生成三条简短的智能建议。类似这种这么简短的建议：${examples} 用一句话描述（
    不超过90字,总字数不能超过250个字,直接引用具体目标名称,给出可执行的行动建议`
}

/**
 * 子目标提取 prompt（桌面端 - JSON 格式）
 */
export const extractSubGoalsPrompt = (content) => {
  return `请根据以下内容提取具体的执行步骤划分为 5-8 个核心阶段或里程碑，严禁细碎化，请将相关的学习点合并为一个大项：
  ${content}
  
  要求：
  1. 必须返回一个标准的 JSON 数组格式，例如: ["第一步内容", "第二步内容"]
  2. 只提取具体的、可执行的操作步骤
  3. 忽略所有的标题、层级说明（如"第一阶段"）、资源链接、时间描述或开场白
  4. 尽可能保留原始回复中具体的行动描述
  5. 不要返回任何 Markdown 标记或额外的文字解释，只返回 JSON 数组本身
  `
}

/**
 * 子目标提取 prompt（移动端 - 序号列表格式）
 */
export const extractSubGoalsPromptMobile = (content) => {
  return `请分析以下内容并提取步骤，以序号列表形式返回：\n${content}\n\n要求：\n1. 只返回步骤列表，不要额外解释\n2. 格式为：(1) 第一步 (2) 第二步 ...\n3.不需要概括，尽可能的复制AI回复中的内容\n4. 步骤的序号必须连续且递增`
}
