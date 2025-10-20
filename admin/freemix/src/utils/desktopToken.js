import { isDesktop } from './device.js';
import { saveTokenToFile, getTokenFromFile, removeTokenFromFile } from './electronStorage.js';

/**
 * 桌面端 token 管理工具
 * 用于在 Electron 应用中持久化存储 token
 * 注意：普通token的处理已移至tokenUtils.js工具函数中
 */

// 生成桌面端 token
export const generateDesktopToken = () => {
  try {
    // 生成随机 token
    const array = new Uint8Array(32);
    window.crypto.getRandomValues(array);
    return Array.from(array, byte => byte.toString(16).padStart(2, '0')).join('');
  } catch (error) {
    console.error('生成桌面端 token 失败:', error);
    return null;
  }
};

// 保存桌面端 token（30天有效期）
export const saveLocalStorageDesktopToken = (token) => {
  try {
    const tokenData = {
      token: token,
      expiresAt: Date.now() + 30 * 24 * 60 * 60 * 1000 // 30天后过期
    };
    localStorage.setItem('desktop_token', JSON.stringify(tokenData));
    return true;
  } catch (error) {
    console.error('保存桌面端 token 失败:', error);
    return false;
  }
};

// 获取桌面端 token
export const getLocalStorageDesktopToken = () => {
  try {
    const tokenStr = localStorage.getItem('desktop_token');
    if (!tokenStr) return null;
    
    const tokenData = JSON.parse(tokenStr);
    if (tokenData.expiresAt > Date.now()) {
      return tokenData.token;
    }
    return null;
  } catch (error) {
    console.error('获取桌面端 token 失败:', error);
    return null;
  }
};


