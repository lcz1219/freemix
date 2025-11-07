import { ElMessage } from 'element-plus';
import store from '@/stores/index.js';

/**
 * 创建目标对象的工具函数
 * @param {Object} params - 目标参数
 * @param {string} params.title - 目标标题
 * @param {string} params.description - 目标描述
 * @param {Array} params.childGoals - 子目标数组
 * @param {Date} params.deadline - 截止日期
 * @param {string} params.level - 优先级
 * @param {Array} params.tags - 标签
 * @returns {Object} 完整的目标对象
 */
export const createGoalObject = (params) => {
  const { title, description, childGoals, deadline, level, tags } = params;
  
  // 从Vuex store获取当前用户信息
  const currentUser = store.state.user || {};
  
  // 计算截止日期（如果没有提供则默认为当前时间后30天）
  const finalDeadline = deadline || new Date();
  if (!deadline) {
    finalDeadline.setDate(finalDeadline.getDate() + 30);
  }
  
  // 创建目标对象
  const goal = {
    title,
    description,
    childGoals: childGoals.map((goal, index) => ({
      _id: `node-${Date.now()}-${index}`,
      message: goal.message || goal.title || '',
      finish: false,
      fileList: []
    })),
    fileList: [],
    assignee: currentUser.username || '', // 当前用户名
    owner: currentUser.username || '', // 所有者
    deadline: finalDeadline.toISOString(),
    priority: level || 'medium', // 优先级
    tags: tags || ['ai生成'], // 分类标签
    progress: 0,
    status: '进行中'
  };
  
  return goal;
};

/**
 * 模拟handleSubmit函数的逻辑来创建目标
 * @param {Object} goalObject - 完整的目标对象
 * @returns {Promise<boolean>} 创建是否成功
 */
export const createGoalViaAPI = async (goalObject) => {
  try {
    // 这里应该调用实际的API端点来创建目标
    // 模拟API调用
    const response = await fetch('/api/editGoal', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(goalObject)
    });
    
    if (response.ok) {
      const result = await response.json();
      if (result.success) {
        ElMessage.success('目标创建成功！');
        return true;
      } else {
        throw new Error(result.message || '创建目标失败');
      }
    } else {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
  } catch (error) {
    console.error('创建目标时发生错误:', error);
    ElMessage.error(`创建目标失败: ${error.message}`);
    return false;
  }
};