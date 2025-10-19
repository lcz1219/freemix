import { isDesktop } from './device.js';
import { saveTokenToFile, getTokenFromFile, removeTokenFromFile } from './electronStorage.js';

/**
 * 桌面端 token 管理工具
 * 用于在 Electron 应用中持久化存储 token
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

// 保存普通 token（30天有效期）
export const saveToken = (token) => {
  try {
    if (isDesktop()) {
      // 桌面端使用 Electron 持久化存储
      const tokenData = {
        token: token,
        expiresAt: Date.now() + 30 * 24 * 60 * 60 * 1000 // 30天后过期
      };
      return saveTokenToFile(tokenData);
    }
    // 非桌面端或其他情况使用 localStorage
  
    return true;
  } catch (error) {
    console.error('保存 token 失败:', error);
    return false;
  }
};

// 获取普通 token
export const getToken = async () => {
  try {
    if (isDesktop()) {
      // 桌面端从文件获取 token
      const token = await  getTokenFromFile();
       console.log('token', getTokenFromFile());
        return token;
      
    }
   
  } catch (error) {
    console.error('获取 token 失败:', error);
    return null;
  }
};

// 删除普通 token
export const removeToken = () => {
  try {
    if (isDesktop()) {
      // 桌面端从文件删除 token
      return removeTokenFromFile();
    }
    
  } catch (error) {
    console.error('删除 token 失败:', error);
    return false;
  }
};

// 检查普通 token 是否有效
export const hasValidToken = () => {
  try {
    const token = getToken();
    return !!token;
  } catch (error) {
    console.error('检查 token 有效性失败:', error);
    return false;
  }
};

// 保存桌面端 token（30天有效期）
export const saveDesktopToken = (token) => {
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
export const getDesktopToken = () => {
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

// 删除桌面端 token
export const removeDesktopToken = () => {
  try {
    localStorage.removeItem('desktop_token');
    return true;
  } catch (error) {
    console.error('删除桌面端 token 失败:', error);
    return false;
  }
};

// 检查桌面端 token 是否有效
export const hasValidDesktopToken = () => {
  try {
    const token = getDesktopToken();
    return !!token;
  } catch (error) {
    console.error('检查桌面端 token 有效性失败:', error);
    return false;
  }
};