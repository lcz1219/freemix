import { getToken } from './tokenUtils.js';
import { isDesktop } from './device.js';
import { getLocalStorageDesktopToken } from './desktopToken.js';

const API_ENDPOINT = `${import.meta.env.PROD ? 'https://gofreemix.com' : ''}/freemix/ai-messages/chat-stream`;

/**
 * 统一走后端 AI 代理，避免在前端暴露第三方模型密钥。
 */
export const callCozeAPI = async (message, onUpdate) => {
  try {
    const token = await getToken();
    if (!token) {
      throw new Error('缺少认证Token');
    }

    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream'
    };

    if (isDesktop()) {
      const desktopToken = getLocalStorageDesktopToken();
      headers['X-Desktop-Token'] = desktopToken || token;
    } else {
      headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(API_ENDPOINT, {
      method: 'POST',
      headers,
      body: JSON.stringify({
        question: message
      })
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || `AI call failed: ${response.statusText}`);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let fullResponse = '';
    let thinkingContent = '';
    let followUpQuestions = [];
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      const chunk = decoder.decode(value, { stream: true });
      buffer += chunk;

      const lines = buffer.split('\n');
      buffer = lines.pop() || '';

      for (const line of lines) {
        if (!line.startsWith('data:')) continue;

        const data = line.slice(5).trim();
        if (!data) continue;
        if (data === '[DONE]') break;

        try {
          const jsonData = JSON.parse(data);
          if (!jsonData.message) continue;

          const msg = jsonData.message;
          if (msg.type === 'answer') {
            if (msg.content) {
              fullResponse += msg.content;
            }
            if (msg.reasoning_content) {
              thinkingContent += msg.reasoning_content;
            }
            if (onUpdate) {
              onUpdate({
                messageType: 'answer',
                content: fullResponse,
                thinkingContent: thinkingContent,
                isProcessing: true
              });
            }
          } else if (msg.type === 'follow_up') {
            if (msg.content) {
              try {
                const questions = JSON.parse(msg.content);
                if (Array.isArray(questions)) {
                  followUpQuestions = questions;
                }
              } catch (e) {
                followUpQuestions.push(msg.content);
              }
            }
            if (onUpdate) {
              onUpdate({
                messageType: 'follow_up',
                followUpQuestions: followUpQuestions,
                isProcessing: false
              });
            }
          } else if (msg.type === 'verbose' || msg.type === 'thinking') {
            if (msg.reasoning_content) {
              thinkingContent += msg.reasoning_content;
            } else if (msg.content) {
              thinkingContent += msg.content;
            }
            if (onUpdate) {
              onUpdate({
                messageType: 'verbose',
                content: fullResponse,
                thinkingContent: thinkingContent,
                isProcessing: true
              });
            }
          }
        } catch (e) {
          // 保持和原逻辑一致，忽略单条分片解析异常
        }
      }
    }

    return {
      messageType: 'answer',
      success: true,
      content: fullResponse,
      thinkingContent: thinkingContent,
      followUpQuestions: followUpQuestions
    };
  } catch (error) {
    console.error('AI API调用失败:', error);
    throw error;
  }
};
