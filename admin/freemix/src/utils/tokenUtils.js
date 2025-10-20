/**
 * Token工具函数，封装桌面端和移动端的token处理逻辑
 */

import { isDesktop } from './device.js';
import { saveTokenToFile, getTokenFromFile, removeTokenFromFile } from './electronStorage.js';
import { getLocalStorageDesktopToken } from './desktopToken.js';

/**
 * 保存token到合适的存储位置
 * @param {string} token - 要保存的token
 * @returns {boolean} 是否保存成功
 */
export const saveToken = (token) => {
  if (isDesktop()) {
     // 桌面端使用 Electron 持久化存储
      const tokenData = {
        token: token,
        expiresAt: Date.now() + 30 * 24 * 60 * 60 * 1000 // 30天后过期
      };
       saveTokenToFile(tokenData);
      return true
  } else {
    // 移动端保存到localStorage
    localStorage.setItem('token', token);
    return true;
  }
};

/**
 * 从合适的存储位置获取token
 * @returns {Promise<string|null>} token值或null
 */
export const getToken = async () => {
  if (isDesktop()) {
       const getTokenFromFileData=  await getTokenFromFile()
        console.log("getLocalStorageDesktopToken",getLocalStorageDesktopToken());
      console.log("getTokenFromFileData",getTokenFromFileData);
      return getLocalStorageDesktopToken()?getLocalStorageDesktopToken():getTokenFromFileData;
     
      
      
     
    // 桌面端从文件获取
  } else {
    // 移动端从localStorage获取
    return localStorage.getItem('token');
  }
};

/**
 * 从合适的存储位置移除token
 * @returns {boolean} 是否移除成功
 */
export const removeToken = () => {
  if (isDesktop()) {
    console.log("removeTokenFromFile");
    localStorage.removeItem('desktop_token');
    // 桌面端从文件移除
    return removeTokenFromFile();
  } else {
    // 移动端从localStorage移除
    localStorage.removeItem('token');
    return true;
  }
};

/**
 * 检查是否存在有效的token
 * @returns {Promise<boolean>} 是否存在有效token
 */
export const hasValidToken = async () => {
  if (isDesktop()) {
    // 桌面端检查文件中的token
    try {
      // 这里简化处理，实际应该检查token是否过期
      const token = await getTokenFromFile();
      return !!token;
    } catch (error) {
      console.error('检查桌面端token有效性失败:', error);
      return false;
    }
  } else {
    // 移动端检查localStorage中的token
    return !!localStorage.getItem('token');
  }
};