// 移动端设备检测工具
import isElectron from 'is-electron';

export function isMobile() {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
}

export function isTablet() {
  return /(ipad|tablet|(android(?!.*mobile))|(windows(?!.*phone)(.*touch))|kindle|playbook|silk|(puffin(?!.*(IP|AP|WP))))/i.test(navigator.userAgent);
}

export function isDesktop() {
  // 优先检查 window.electronAPI (项目自定义的preload注入)
  if (typeof window !== 'undefined' && window.electronAPI) {
    return true;
  }
  return isElectron();
}