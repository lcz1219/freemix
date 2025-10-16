// src/utils/desktopToken.js
// 桌面端token管理工具

/**
 * 生成桌面端token（30天有效期）
 * @returns {string} token字符串
 */
export function generateDesktopToken() {
  // 生成一个随机token
  const token = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
  return token;
}

/**
 * 保存桌面端token到localStorage（30天有效期）
 * @param {string} token - token字符串
 */
export function saveDesktopToken(token) {
  const expiration = new Date();
  expiration.setDate(expiration.getDate() + 30); // 30天后过期
  
  const tokenData = {
    token: token,
    expiresAt: expiration.getTime()
  };
  
  localStorage.setItem('desktop_token', JSON.stringify(tokenData));
}

/**
 * 获取桌面端token
 * @returns {string|null} token字符串或null（如果token不存在或已过期）
 */
export function getDesktopToken() {
  const tokenDataStr = localStorage.getItem('desktop_token');
  if (!tokenDataStr) {
    return null;
  }
  
  try {
    const tokenData = JSON.parse(tokenDataStr);
    const now = new Date().getTime();
    
    // 检查token是否过期
    if (now > tokenData.expiresAt) {
      // token已过期，删除它
      localStorage.removeItem('desktop_token');
      return null;
    }
    
    return tokenData.token;
  } catch (e) {
    // 解析失败，删除无效数据
    localStorage.removeItem('desktop_token');
    return null;
  }
}

/**
 * 删除桌面端token
 */
export function removeDesktopToken() {
  localStorage.removeItem('desktop_token');
}

/**
 * 检查是否存在有效的桌面端token
 * @returns {boolean} 是否存在有效的token
 */
export function hasValidDesktopToken() {
  return getDesktopToken() !== null;
}