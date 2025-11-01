import { useRouter } from 'vue-router';
import { useLoadingBar } from 'naive-ui';

export function useNavigation() {
  const router = useRouter();
  const loadingBar = useLoadingBar();
  
  // 开始加载
  const loading = () => {
    loadingBar.start();
  };
  
  // 结束加载
  const loadingFinish = () => {
    loadingBar.finish();
  };
  
  // 导航到指定路径
  const goTo = (path: string) => {
    loading();
    router.push(path);
    setTimeout(() => {
      loadingFinish();
    }, 1000);
  };
  
  // 延迟导航到指定路径
  const goToWithDelay = (path: string, delay: number = 1000) => {
    loading();
    setTimeout(() => {
      router.push(path);
      loadingFinish();
    }, delay);
  };
  
  return {
    loading,
    loadingFinish,
    goTo,
    goToWithDelay
  };
}