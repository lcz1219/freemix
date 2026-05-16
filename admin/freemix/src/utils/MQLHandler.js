/**
 * MQL 处理工具：解析文本中的 [MQL_START] 标签并执行
 */
import { postM } from '@/utils/request'
export const handleMQLResponse = async (text,question) => {
  const MQL_START = '[MQL_START]';
  const MQL_END = '[MQL_END]';
  
  const startIndex = text.indexOf(MQL_START);
  const endIndex = text.indexOf(MQL_END);
  if (startIndex !== -1 && endIndex !== -1 && endIndex > startIndex) {
    const pipelineStr = text.substring(startIndex + MQL_START.length, endIndex).trim();
    try {
      // 1. 调用后端接口执行 MQL
      
      const response = await postM('/ai-messages/query-mql', JSON.stringify({
          pipeline: pipelineStr,
          collection: 'goal',
          question:question
        }));
      
      const result = response;
      if (result.data.code === 200) {
        // 2. 将结果返回，用于二次喂给 AI
        return {
          success: true,
          rawData: result.data.data,
          mql: pipelineStr
        };
      } else {
         return {
          success: false,
          rawData: result.data.msg,
          mql: pipelineStr
        };
        console.error('MQL Execution Failed:', result.msg);
      }
    } catch (error) {
      console.error('MQL Network Error:', error);
    }
  }
  return null;
};
