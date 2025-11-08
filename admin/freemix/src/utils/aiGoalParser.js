/**
 * 解析AI响应并提取子目标
 * @param {string} aiResponse - AI助手的响应文本
 * @returns {Array} 子目标数组
 */
export function parseAIResponseToSubGoals(aiResponse) {
  // 如果响应是JSON格式，尝试解析
  try {
    const jsonData = JSON.parse(aiResponse);
    if (Array.isArray(jsonData)) {
      return jsonData.map((item, index) => ({
        _id: `node-${Date.now()}-${index}`,
        message: typeof item === 'string' ? item : (item.step || item.message || item.title || JSON.stringify(item)),
        finish: false,
        fileList: []
      }));
    }
  } catch (e) {
    // 不是JSON格式，继续处理文本
  }

  // 处理文本格式的响应
  const subGoals = [];
  
  // 移除常见的前缀和后缀
  let cleanResponse = aiResponse
    .replace(/^\s*```(?:json)?\s*/i, '')  // 移除开头的代码块标记
    .replace(/\s*```\s*$/i, '')          // 移除结尾的代码块标记
    .trim();

  // 检查是否是JSON数组格式
  if (cleanResponse.startsWith('[') && cleanResponse.endsWith(']')) {
    try {
      const jsonArray = JSON.parse(cleanResponse);
      if (Array.isArray(jsonArray)) {
        return jsonArray.map((item, index) => ({
          _id: `node-${Date.now()}-${index}`,
          message: typeof item === 'string' ? item : (item.step || item.message || item.title || JSON.stringify(item)),
          finish: false,
          fileList: []
        }));
      }
    } catch (e) {
      // 解析失败，按文本处理
    }
  }

  // 按行分割文本
  const lines = cleanResponse.split('\n');
  
  // 常见的步骤标识符
  const stepPatterns = [
    /^\s*(\d+\.|\d+\)|\(\d+\)|\d+\s*[、.])\s*(.+)/,  // 1. 1) (1) 等
    /^\s*[-\*]\s*(.+)/,  // 项目符号
    /^\s*[步骤步]\s*(\d+)[.、:：]?\s*(.+)/,  // 步骤1 步1等
    /^\s*[第]?\s*(\d+)\s*[步].*/  // 第1步等
  ];

  let stepCounter = 1;
  
  for (let line of lines) {
    line = line.trim();
    if (!line) continue;

    let isStep = false;
    let stepContent = line;

    // 检查是否匹配步骤模式
    for (const pattern of stepPatterns) {
      const match = line.match(pattern);
      if (match) {
        isStep = true;
        stepContent = match[match.length - 1].trim(); // 取最后一个捕获组
        break;
      }
    }

    // 如果没有明确的步骤标识符，但行内容看起来像步骤，也添加
    if (!isStep) {
      const lowerLine = line.toLowerCase();
      // 检查是否包含常见的步骤关键词
      const stepKeywords = ['首先', '然后', '接着', '最后', '第一步', '第二步', '第三步', '准备', '安装', '配置', '验证', '测试'];
      const hasStepKeyword = stepKeywords.some(keyword => lowerLine.includes(keyword));
      
      // 检查是否是完整的句子且不是太长
      const isReasonableLength = line.length > 3 && line.length < 200;
      
      if ((hasStepKeyword && isReasonableLength) || line.length > 5) {
        isStep = true;
        stepContent = line;
      }
    }

    // 添加识别出的步骤
    if (isStep && stepContent.length > 1) {
      // 清理步骤内容
      stepContent = stepContent
       .replace(/^[-\*]\s*/, '')  // 移除开头的符号
        .replace(/\*+/g, '')        // 移除所有星号（包括**强调**符号）
        .replace(/\n/g, ' ')        // 将换行符替换为空格
        .replace(/[`_~]/g, '')       // 移除其他Markdown符号：`代码`、_斜体_、~~删除线~~
        .replace(/\s+/g, ' ')       // 合并多个空格
        .replace(/^步骤\d+:\s*/, '') // 移除已有的"步骤X:"前缀，避免重复
        .trim();

      // 避免添加重复的步骤
      if (stepContent && !subGoals.some(goal => goal.message.includes(stepContent) || stepContent.includes(goal.message))) {
        subGoals.push({
          _id: `node-${Date.now()}-${stepCounter}`,
          message: `步骤${stepCounter}: ${stepContent}`,
          finish: false,
          fileList: []
        });
        stepCounter++;
      }
    }
  }

  // 如果没有识别出任何步骤，将整个响应作为一个步骤
  if (subGoals.length === 0 && cleanResponse.length > 0) {
    subGoals.push({
      _id: `node-${Date.now()}-1`,
      message: cleanResponse,
      finish: false,
      fileList: []
    });
  }

  return subGoals;
}

/**
 * 从AI响应中提取目标标题
 * @param {string} aiResponse - AI助手的响应文本
 * @param {string} userQuestion - 用户的问题
 * @returns {string} 目标标题
 */
export function extractGoalTitle(aiResponse, userQuestion) {
  // 尝试从用户问题中提取标题
  const questionTitle = userQuestion
    .replace(/^(如何|怎么|怎样)?(制定|创建|设立|设置)?(一个)?/i, '')
    .replace(/[？?]*$/, '')
    .trim();

  if (questionTitle.length > 0 && questionTitle.length <= 30) {
    return questionTitle;
  }

  // 尝试从AI响应中提取标题
  const lines = aiResponse.split('\n');
  for (let line of lines) {
    line = line.trim();
    if (line.length > 0 && line.length <= 30 && !line.match(/^[\d\s\-\*\.]*$/)) {
      return line.replace(/^#+\s*/, '').trim(); // 移除markdown标题标记
    }
  }

  // 默认标题
  return "AI助手创建的目标";
}

/**
 * 创建完整的目标对象
 * @param {string} title - 目标标题
 * @param {string} description - 目标描述
 * @param {Array} subGoals - 子目标数组
 * @param {Object} additionalFields - 其他字段
 * @returns {Object} 完整的目标对象
 */
export function createGoalObject(title, description, subGoals, additionalFields = {}) {
  const now = new Date();
  const oneWeekLater = new Date();
  oneWeekLater.setDate(now.getDate() + 7);

  return {
    _id: `goal-${Date.now()}`,
    title: title || "AI助手创建的目标",
    description: description || "",
    owner: additionalFields.owner || "", // 需要从用户信息中获取
    deadline: additionalFields.deadline || oneWeekLater,
    level: additionalFields.level || "medium",
    tags: additionalFields.tags || [],
    childGoals: subGoals || [],
    planTime: 0,
    progress: 0,
    status: "in-progress",
    del: 0,
    deltime: 0,
    fileList: [],
    disRecover: false,
    finish: false,
    collaborators: []
  };
}