export const genMsg = (msg) => {
  try {
    if (window.electronAPI) {
      // const { ipcRenderer } = window.require('electron');
      // const tokenData = {
      //   token: token,
      //   expiresAt: Date.now() + 30 * 24 * 60 * 60 * 1000 // 30天后过期
      // };
      console.log("msg:",msg);
     window.electronAPI.genMsg(msg);
      return true;
    }
    return false;
  } catch (error) {
    console.error('保存 token 到文件失败:', error);
    return false;
  }
};