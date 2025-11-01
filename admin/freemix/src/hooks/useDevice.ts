import { ref, onMounted } from 'vue';

export function useDevice() {
  // 设备类型检测
  const isMobileDevice = ref(false);
  const isTabletDevice = ref(false);
  const isDesktopDevice = ref(false);
  
  // 移动端设备检测
  const checkMobile = () => {
    return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
  };
  
  // 平板设备检测
  const checkTablet = () => {
    return /(ipad|tablet|(android(?!.*mobile))|(windows(?!.*phone)(.*touch))|kindle|playbook|silk|(puffin(?!.*(IP|AP|WP))))/i.test(navigator.userAgent);
  };
  
  // 桌面端检测
  const checkDesktop = () => {
    // 这里需要引入is-electron库
    // 由于这是一个运行时检测，我们简单返回true/false
    return typeof process !== 'undefined' && typeof process.versions !== 'undefined' && typeof process.versions.electron !== 'undefined';
  };
  
  // 初始化设备检测
  const initDeviceDetection = () => {
    isMobileDevice.value = checkMobile();
    isTabletDevice.value = checkTablet();
    isDesktopDevice.value = checkDesktop();
  };
  
  // 在组件挂载时进行设备检测
  onMounted(() => {
    initDeviceDetection();
  });
  
  return {
    isMobileDevice,
    isTabletDevice,
    isDesktopDevice,
    initDeviceDetection
  };
}