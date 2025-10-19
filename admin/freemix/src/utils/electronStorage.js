

/**
 * 在 Electron 环境中持久化存储 token 到本地文件
 */

// 保存 token 到本地文件
export const saveTokenToFile = (token) => {
  try {
    console.log('window.electronAPI):', window.electronAPI);
    if (window.electronAPI) {
      // const { ipcRenderer } = window.require('electron');
      // const tokenData = {
      //   token: token,
      //   expiresAt: Date.now() + 30 * 24 * 60 * 60 * 1000 // 30天后过期
      // };
     window.electronAPI.saveToken(token);
      return true;
    }
    return false;
  } catch (error) {
    console.error('保存 token 到文件失败:', error);
    return false;
  }
};

// 从本地文件获取 token
// 将函数改为异步函数，直接返回 Promise 的结果
export const getTokenFromFile = async () => { // 注意添加了 async
  try {
    if (window.electronAPI) {
      // 使用 await 等待 Promise 完成，并直接拿到结果
      const tokenData = await window.electronAPI.getToken();
      // console.log('从主进程获取的完整数据:', tokenData);

      if (tokenData && tokenData.token && tokenData.expiresAt > Date.now()) {
        return tokenData.token; // 直接返回有效的 token
      }
      return null; // 如果没有有效 token，返回 null
    }
    return null;
  } catch (error) {
    console.error('从文件获取 token 失败:', error);
    return null;
  }
};

// 从本地文件删除 token
export const removeTokenFromFile = () => {
  try {
    if (window.electronAPI) {
      window.electronAPI.removeToken();
      return true;
    }
    return false;
  } catch (error) {
    console.error('从文件删除 token 失败:', error);
    return false;
  }
};

// 检查本地文件中的 token 是否有效
export const hasValidTokenInFile = () => {
  try {
    const token = getTokenFromFile();
    return !!token;
  } catch (error) {
    console.error('检查文件中的 token 有效性失败:', error);
    return false;
  }
};