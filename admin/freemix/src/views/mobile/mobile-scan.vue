<template>
  <div class="mobile-scan-page">
    <!-- 顶部导航栏 (透明背景，白色图标) -->
    <div class="scan-header">
      <div class="back-btn" @click="goBack">
        <van-icon name="arrow-left" size="24" color="#fff" />
      </div>
      <div class="title">扫码登录</div>
      <div class="right-placeholder"></div>
    </div>

    <!-- 扫码区域 -->
    <div class="scan-container">
      <div class="scan-video-wrapper" :style="isNative ? { background: 'transparent' } : {}">
         <!-- 非 Native 模式下显示 video 元素 -->
        <video v-if="!isNative" ref="qrVideoRef" class="qr-video" autoplay playsinline></video>
        
        <!-- 扫码框覆盖层 -->
        <div class="scan-overlay">
          <div class="scan-box">
            <div class="scan-line"></div>
            <div class="corner top-left"></div>
            <div class="corner top-right"></div>
            <div class="corner bottom-left"></div>
            <div class="corner bottom-right"></div>
          </div>
          <div class="scan-tip">
            <span v-if="qrLoading && !qrScanError">正在启动相机...</span>
            <span v-else>将二维码放入框内，自动扫描</span>
          </div>
          <div v-if="qrScanError" class="error-tip">{{ qrScanError }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { Capacitor } from '@capacitor/core';
import { BarcodeScanner, BarcodeFormat, LensFacing } from '@capacitor-mlkit/barcode-scanning';

const router = useRouter();
const isNative = ref(Capacitor.isNativePlatform());
const qrVideoRef = ref<HTMLVideoElement | null>(null);
const qrLoading = ref(true);
const qrScanError = ref('');
let qrMediaStream: MediaStream | null = null;
let qrScanTimer: any = null;

const goBack = () => {
  stopQrScan();
  router.back();
};

const playScanFeedback = () => {
  try {
    if (navigator.vibrate) {
      navigator.vibrate(100);
    }
  } catch (e) {}
};

const handleQrPayload = async (text: string) => {
  try {
    let data;
    try {
      data = JSON.parse(text);
    } catch (e) {
      // 兼容 URL 格式
      if (text.includes('data=')) {
         try {
            const urlObj = new URL(text);
            const dataStr = urlObj.searchParams.get('data') || (urlObj.hash.split('data=')[1]);
            if (dataStr) {
               data = JSON.parse(decodeURIComponent(dataStr));
            }
         } catch(err) {
            const match = text.match(/data=([^&]+)/);
             if (match) {
               data = JSON.parse(decodeURIComponent(match[1]));
             }
         }
      }
    }

    if (!data || data.type !== 'freemix-qr-login' || !data.sessionId || !data.sessionToken) {
      showToast('无效的登录二维码');
      // 继续扫描，不退出
      return;
    }

    playScanFeedback();
    stopQrScan();
    // 跳转到确认页面
    router.replace({
      path: '/mobile/qr-confirm',
      query: { data: encodeURIComponent(JSON.stringify(data)) }
    });

  } catch (e) {
    console.error('扫码解析错误', e);
    showToast('二维码内容无法识别');
  }
};

const stopQrScan = () => {
  qrLoading.value = false;
  if (Capacitor.isNativePlatform()) {
    document.body.classList.remove('camera-active');
    document.documentElement.classList.remove('camera-active');
    // 恢复背景色
    document.body.style.backgroundColor = '';
    document.documentElement.style.backgroundColor = '';
    
    BarcodeScanner.removeAllListeners();
    BarcodeScanner.stopScan();
  } else {
    if (qrScanTimer) {
      clearInterval(qrScanTimer);
      qrScanTimer = null;
    }
    if (qrMediaStream) {
      qrMediaStream.getTracks().forEach(t => t.stop());
      qrMediaStream = null;
    }
  }
};

const startQrScanLoop = (video: HTMLVideoElement, detector: any) => {
  if (qrScanTimer) clearInterval(qrScanTimer);
  qrScanTimer = setInterval(async () => {
    if (!video || video.readyState !== 4) return;
    try {
      const barcodes = await detector.detect(video);
      if (barcodes && barcodes.length > 0) {
        const value = barcodes[0].rawValue;
        if (value) {
          await handleQrPayload(value);
        }
      }
    } catch (e) {
      console.error('Detection error', e);
    }
  }, 500);
};

const startQrScanner = async () => {
  qrScanError.value = '';
  qrLoading.value = true;

  if (Capacitor.isNativePlatform()) {
    try {
      const { camera } = await BarcodeScanner.requestPermissions();
      if (camera !== 'granted' && camera !== 'limited') {
        qrLoading.value = false;
        qrScanError.value = '请在系统设置中允许访问相机';
        return;
      }

      // 设置透明背景
      document.body.classList.add('camera-active');
      document.documentElement.classList.add('camera-active');
      document.body.style.backgroundColor = 'transparent';
      document.documentElement.style.backgroundColor = 'transparent';

      await BarcodeScanner.addListener('barcodeScanned', async result => {
        if (result.barcode.rawValue) {
          await handleQrPayload(result.barcode.rawValue);
        }
      });

      await BarcodeScanner.startScan({ 
        formats: [BarcodeFormat.QrCode],
        lensFacing: LensFacing.Back
      });
      
      qrLoading.value = false;
    } catch (e: any) {
      console.error('Native scan error', e);
      qrLoading.value = false;
      qrScanError.value = '无法启动相机: ' + e.message;
      stopQrScan(); // 清理样式
    }
  } else {
    // Web 模式
    try {
      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        throw new Error('浏览器不支持摄像头访问');
      }
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment' },
        audio: false
      });
      qrMediaStream = stream;
      
      await nextTick();
      const video = qrVideoRef.value;
      if (video) {
        video.srcObject = stream;
        await video.play();
        
        if ((window as any).BarcodeDetector) {
          const detector = new (window as any).BarcodeDetector({ formats: ['qr_code'] });
          startQrScanLoop(video, detector);
        } else {
          qrScanError.value = '当前浏览器不支持原生扫码检测';
        }
      }
      qrLoading.value = false;
    } catch (e: any) {
      console.error('Web scan error', e);
      qrLoading.value = false;
      qrScanError.value = '无法访问相机，请检查权限';
    }
  }
};

onMounted(() => {
  // 页面加载后延迟一点启动相机，确保页面转场完成
  setTimeout(() => {
    startQrScanner();
  }, 300);
});

onUnmounted(() => {
  stopQrScan();
});
</script>

<style scoped>
.mobile-scan-page {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: #000;
  z-index: 9999;
  display: flex;
  flex-direction: column;
}

.scan-header {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  z-index: 100;
  background: rgba(0, 0, 0, 0.3);
  padding-top: env(safe-area-inset-top);
  box-sizing: content-box;
}

.title {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
}

.back-btn, .right-placeholder {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.scan-container {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
}

.scan-video-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
  background: #000;
  overflow: hidden;
}

.qr-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 覆盖层：用于在非 Native 模式下显示扫描框，或在 Native 模式下作为 UI 装饰 */
.scan-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  /* 半透明遮罩，中间镂空 */
  background: rgba(0, 0, 0, 0.5); 
  /* 注意：在 Native 模式下，如果 background 有颜色，会遮挡住下面的相机预览。
     所以如果是 Native 模式，这个 overlay 的 background 必须是 transparent。
     但是我们需要黑色遮罩效果，怎么办？
     通常 Native 模式下，我们会做一个 SVG 或者 div 拼凑出“中间镂空”的效果。
     为了简单起见，这里用 clip-path 或者 border 模拟。
  */
}

/* 针对 Native 的特殊处理 */
:global(body.camera-active) .scan-overlay {
  background: transparent; /* 相机预览在 HTML 之下，所以必须透明 */
  /* 使用超大 border 来模拟遮罩 */
}

.scan-box {
  width: 260px;
  height: 260px;
  position: relative;
  /* 镂空效果：利用 box-shadow */
  box-shadow: 0 0 0 2000px rgba(0, 0, 0, 0.6); 
  border-radius: 2px;
}

.scan-line {
  position: absolute;
  width: 100%;
  height: 2px;
  background: #00c9a7;
  top: 0;
  left: 0;
  animation: scanMove 2.5s infinite linear;
  box-shadow: 0 0 4px #00c9a7;
}

@keyframes scanMove {
  0% { top: 0; opacity: 1; }
  90% { top: 100%; opacity: 1; }
  100% { top: 100%; opacity: 0; }
}

.corner {
  position: absolute;
  width: 20px;
  height: 20px;
  border-color: #00c9a7;
  border-style: solid;
}

.top-left { top: -2px; left: -2px; border-width: 3px 0 0 3px; }
.top-right { top: -2px; right: -2px; border-width: 3px 3px 0 0; }
.bottom-left { bottom: -2px; left: -2px; border-width: 0 0 3px 3px; }
.bottom-right { bottom: -2px; right: -2px; border-width: 0 3px 3px 0; }

.scan-tip {
  margin-top: 24px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  text-align: center;
  text-shadow: 0 1px 2px rgba(0,0,0,0.5);
}

.error-tip {
  position: absolute;
  bottom: 40px;
  color: #ff4d4f;
  background: rgba(0,0,0,0.7);
  padding: 8px 16px;
  border-radius: 4px;
}
</style>

<style>
/* Global transparency for Capacitor Camera */
body.camera-active,
body.camera-active html,
body.camera-active #app {
  background: transparent !important;
}
</style>