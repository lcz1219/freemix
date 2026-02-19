
const API_ENDPOINT = 'https://api.coze.cn/open_api/v1/chat';
const PERSONAL_ACCESS_TOKEN = 'sat_alIbwyaIhODXfXtTHCuj74C3swKTZd08L82jZDfMsfzplbENrkX5bu3ddTU5VHdn';
const BOT_ID = '7569182284998524934';

export const callCozeAPI = async (message, onUpdate) => {
  try {
    const response = await fetch(API_ENDPOINT, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${PERSONAL_ACCESS_TOKEN}`,
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify({
        bot_id: BOT_ID,
        user: 'user_' + Date.now(),
        query: message,
        stream: true
      })
    });

    if (!response.ok) {
      throw new Error(`API call failed: ${response.statusText}`);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let fullResponse = '';
    let thinkingContent = '';
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      const chunk = decoder.decode(value, { stream: true });
      buffer += chunk;
      
      const lines = buffer.split('\n');
      buffer = lines.pop() || '';

      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.slice(5).trim();
          if (!data) continue;
          
          if (data === '[DONE]') break;

          try {
            const jsonData = JSON.parse(data);
            
            // Check if it's a message type response
            if (jsonData.message) {
              const msg = jsonData.message;
              
              if (msg.type === 'answer') {
                if (msg.content) {
                  fullResponse += msg.content;
                }
                
                // Also check reasoning_content in answer type just in case
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
              } else if (msg.type === 'verbose' || msg.type === 'thinking') {
                if (msg.reasoning_content) {
                  thinkingContent += msg.reasoning_content;
                } else if (msg.content && msg.type === 'thinking') {
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
            } else if (jsonData.event === 'message') {
              // Fallback to event based check if message object structure is different
              if (jsonData.message && jsonData.message.type === 'answer') {
                 const content = jsonData.message.content;
                 fullResponse += content;
                 if (onUpdate) {
                   onUpdate({
                     messageType: 'answer',
                     content: fullResponse,
                     thinkingContent: thinkingContent,
                     isProcessing: true
                   });
                 }
              }
            }
          } catch (e) {
            // ignore parse error
          }
        }
      }
    }

    return {
      messageType: 'answer',
      content: fullResponse,
      thinkingContent: thinkingContent
    };
  } catch (error) {
    console.error('AI API调用失败:', error);
    throw error;
  }
};
