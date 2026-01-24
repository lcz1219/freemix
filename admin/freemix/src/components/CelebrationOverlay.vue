<template>
  <transition name="fade">
    <div v-if="show" class="celebration-overlay">
      <!-- 背景光晕 -->
      <div class="ambient-glow"></div>
      
      <canvas ref="canvas" class="confetti-canvas"></canvas>
      
      <div class="celebration-card" :class="{ 'enter': showContent }">
        <!-- 卡片顶部装饰光效 -->
        <div class="card-shine"></div>
        
        <div class="icon-wrapper">
          <div class="god-rays"></div>
          <div class="trophy-icon">🏆</div>
          <div class="sparkles">
            <span style="--d: 0s; --x: -20px; --y: -20px">✨</span>
            <span style="--d: 0.3s; --x: 20px; --y: -30px">✨</span>
            <span style="--d: 0.7s; --x: 0px; --y: 20px">✨</span>
          </div>
        </div>

        <div class="text-content">
          <h2 class="celebration-title">{{ heading }}</h2>
          <p class="celebration-subtitle">{{ subHeading }}</p>
          
          <div class="goal-container" v-if="title">
            <div class="goal-glow"></div>
            <div class="goal-title">{{ title }}</div>
          </div>
        </div>

        <button class="continue-btn" @click="handleClose">
          <span class="btn-text">继续征程</span>
          <div class="btn-shine"></div>
        </button>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue';

const props = defineProps({
  show: { type: Boolean, default: false },
  title: { type: String, default: '' },
  heading: { type: String, default: 'TARGET SMASHED!' },
  subHeading: { type: String, default: '恭喜！你已达成里程碑' }
});

const emit = defineEmits(['close']);

const canvas = ref<HTMLCanvasElement | null>(null);
const showContent = ref(false);
let animationId: number | null = null;
let particles: ConfettiParticle[] = [];
let ctx: CanvasRenderingContext2D | null = null;

// 高级配色方案：金、红、蓝绿、紫，增加高亮色
const colors = ['#FFD700', '#FF4D4D', '#00E0FF', '#BD00FF', '#FFFFFF'];

// 粒子类升级：支持形状和 3D 摆动
class ConfettiParticle {
  x: number;
  y: number;
  wobble: number;
  wobbleSpeed: number;
  velocity: { x: number; y: number };
  tiltAngle: number;
  color: string;
  shape: 'circle' | 'square' | 'ribbon';
  size: number;
  decay: number;
  life: number;

  constructor(canvasWidth: number, canvasHeight: number) {
    this.x = canvasWidth / 2; // 从中心爆发
    this.y = canvasHeight / 2 + 50;
    
    // 爆炸式初速度
    const angle = Math.random() * Math.PI * 2;
    const speed = Math.random() * 25 + 10; // 更快的初速度
    
    this.velocity = {
      x: Math.cos(angle) * speed,
      y: Math.sin(angle) * speed
    };

    this.wobble = Math.random() * 10;
    this.wobbleSpeed = Math.random() * 0.1 + 0.05;
    this.tiltAngle = Math.random() * Math.PI;
    this.color = colors[Math.floor(Math.random() * colors.length)];
    this.shape = Math.random() > 0.8 ? 'circle' : (Math.random() > 0.5 ? 'ribbon' : 'square');
    this.size = Math.random() * 8 + 5;
    this.decay = Math.random() * 0.02 + 0.95; // 空气阻力
    this.life = 1; // 透明度/生命周期
  }

  update() {
    this.life -= 0.005; // 缓慢消失
    
    // 物理模拟
    this.velocity.x *= 0.96; // x轴阻力
    this.velocity.y *= 0.96; // y轴阻力
    this.velocity.y += 0.6;  // 重力
    
    this.x += this.velocity.x;
    this.y += this.velocity.y;
    
    // 3D 摆动效果
    this.wobble += this.wobbleSpeed;
    
    // 飘动位移
    this.x += Math.sin(this.wobble) * 2; 
  }

  draw(ctx: CanvasRenderingContext2D) {
    ctx.save();
    ctx.globalAlpha = this.life;
    ctx.fillStyle = this.color;
    ctx.translate(this.x, this.y);
    
    // 模拟旋转
    ctx.rotate(this.tiltAngle);
    // 使用 scale 模拟 3D 翻转
    const scaleX = Math.cos(this.wobble);
    ctx.scale(scaleX, 1);

    if (this.shape === 'circle') {
      ctx.beginPath();
      ctx.arc(0, 0, this.size / 2, 0, Math.PI * 2);
      ctx.fill();
    } else if (this.shape === 'ribbon') {
      ctx.fillRect(-this.size, -this.size / 4, this.size * 2, this.size / 2);
    } else {
      ctx.fillRect(-this.size / 2, -this.size / 2, this.size, this.size);
    }
    
    ctx.restore();
  }
}

const initParticles = () => {
  if (!canvas.value) return;
  const { width, height } = canvas.value;
  particles = [];
  // 初始大爆炸
  for (let i = 0; i < 250; i++) {
    particles.push(new ConfettiParticle(width, height));
  }
};

// 持续的小爆发
const cannon = () => {
  if (!canvas.value || !props.show) return;
  const { width, height } = canvas.value;
  // 左右角落发射
  for (let i = 0; i < 5; i++) {
    const pLeft = new ConfettiParticle(width, height);
    pLeft.x = 0;
    pLeft.y = height;
    pLeft.velocity = { x: Math.random() * 20 + 10, y: -(Math.random() * 20 + 15) };
    particles.push(pLeft);

    const pRight = new ConfettiParticle(width, height);
    pRight.x = width;
    pRight.y = height;
    pRight.velocity = { x: -(Math.random() * 20 + 10), y: -(Math.random() * 20 + 15) };
    particles.push(pRight);
  }
};

const animate = () => {
  if (!canvas.value || !ctx) return;
  ctx.clearRect(0, 0, canvas.value.width, canvas.value.height);

  for (let i = particles.length - 1; i >= 0; i--) {
    const p = particles[i];
    p.update();
    p.draw(ctx);
    
    // 移除离开屏幕或消失的粒子
    if (p.life <= 0 || p.y > canvas.value.height + 50) {
      particles.splice(i, 1);
    }
  }

  // 随机补充粒子，保持画面活跃
  if (Math.random() > 0.9) cannon();

  if (props.show) {
    animationId = requestAnimationFrame(animate);
  }
};

const startAnimation = async () => {
  await nextTick();
  if (!canvas.value) return;
  
  canvas.value.width = window.innerWidth;
  canvas.value.height = window.innerHeight;
  ctx = canvas.value.getContext('2d');
  
  initParticles();
  animate();
  
  setTimeout(() => { showContent.value = true; }, 50);
};

const handleClose = () => {
  showContent.value = false;
  // 给一点时间让关闭动画播放
  setTimeout(() => emit('close'), 300);
};

watch(() => props.show, (newVal) => {
  if (newVal) {
    startAnimation();
  } else {
    showContent.value = false;
    if (animationId) cancelAnimationFrame(animationId);
    particles = [];
  }
});

const handleResize = () => {
  if (canvas.value) {
    canvas.value.width = window.innerWidth;
    canvas.value.height = window.innerHeight;
  }
};

onMounted(() => window.addEventListener('resize', handleResize));
onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  if (animationId) cancelAnimationFrame(animationId);
});
</script>

<style scoped>
/* 字体引入 (建议在全局引入 Inter 或 Roboto) */
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@400;700;900&display=swap');

.celebration-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: radial-gradient(circle at center, rgba(20, 20, 30, 0.85) 0%, rgba(0, 0, 0, 0.95) 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  font-family: 'Outfit', sans-serif;
  perspective: 1000px; /* 增加3D透视 */
}

/* 背景氛围光 */
.ambient-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60vw;
  height: 60vh;
  background: radial-gradient(circle, rgba(255, 215, 0, 0.15) 0%, transparent 70%);
  pointer-events: none;
  z-index: 0;
}

.confetti-canvas {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}

/* === 主卡片 === */
.celebration-card {
  position: relative;
  z-index: 2;
  width: 90%;
  max-width: 480px;
  padding: 50px 40px;
  border-radius: 32px;
  text-align: center;
  
  /* 高级毛玻璃 + 渐变边框模拟 */
  background: rgba(30, 30, 40, 0.7);
  backdrop-filter: blur(20px) saturate(180%);
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.5),
    inset 0 0 0 1px rgba(255, 255, 255, 0.1),
    inset 0 1px 0 0 rgba(255, 255, 255, 0.2); /* 顶部高光 */
  
  transform: scale(0.9) translateY(40px) rotateX(10deg);
  opacity: 0;
  transition: all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.celebration-card.enter {
  transform: scale(1) translateY(0) rotateX(0deg);
  opacity: 1;
}

/* 顶部流光 */
.card-shine {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.8), transparent);
  opacity: 0.5;
}

/* === 图标区域 === */
.icon-wrapper {
  position: relative;
  height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
}

/* 圣光特效 */
.god-rays {
  position: absolute;
  width: 200px;
  height: 200px;
  background: conic-gradient(from 0deg, transparent 0deg, rgba(255, 215, 0, 0.3) 20deg, transparent 40deg);
  border-radius: 50%;
  animation: rotate 10s linear infinite;
  mask-image: radial-gradient(black 0%, transparent 70%);
}

.trophy-icon {
  font-size: 90px;
  filter: drop-shadow(0 10px 20px rgba(0,0,0,0.3));
  transform-origin: bottom center;
  animation: bounce-rotate 3s ease-in-out infinite;
  position: relative;
  z-index: 2;
}

/* 闪烁星星 */
.sparkles span {
  position: absolute;
  top: 50%;
  left: 50%;
  font-size: 24px;
  color: #FFD700;
  opacity: 0;
  animation: sparkle-anim 2s infinite;
  animation-delay: var(--d);
  transform: translate(var(--x), var(--y));
}

/* === 文本区域 === */
.celebration-title {
  font-size: 32px;
  font-weight: 900;
  margin: 0 0 8px;
  text-transform: uppercase;
  letter-spacing: 2px;
  /* 黄金渐变字 */
  background: linear-gradient(to bottom, #FFF 0%, #FFD700 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  filter: drop-shadow(0 2px 0 rgba(0,0,0,0.2));
}

.celebration-subtitle {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 30px;
  font-weight: 400;
}

/* === 目标展示框 === */
.goal-container {
  position: relative;
  margin-bottom: 40px;
}

.goal-title {
  position: relative;
  background: linear-gradient(135deg, rgba(255,255,255,0.1), rgba(255,255,255,0.05));
  padding: 16px 24px;
  border-radius: 16px;
  font-size: 20px;
  font-weight: 700;
  color: white;
  border: 1px solid rgba(255,255,255,0.1);
  box-shadow: 0 10px 20px rgba(0,0,0,0.2);
  z-index: 2;
}

.goal-glow {
  position: absolute;
  inset: 0;
  border-radius: 16px;
  background: linear-gradient(135deg, #FFD700, #FF5500);
  opacity: 0.2;
  filter: blur(15px);
  transform: translateY(5px);
  z-index: 1;
}

/* === 按钮 === */
.continue-btn {
  position: relative;
  background: linear-gradient(135deg, #10b981 0%, #047857 100%);
  border: none;
  padding: 16px 48px;
  color: white;
  font-size: 16px;
  font-weight: 700;
  border-radius: 100px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-shadow: 0 10px 20px -5px rgba(16, 185, 129, 0.5), inset 0 2px 0 rgba(255,255,255,0.2);
  width: 100%;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.continue-btn:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 15px 30px -5px rgba(16, 185, 129, 0.6), inset 0 2px 0 rgba(255,255,255,0.2);
}

.continue-btn:active {
  transform: translateY(1px) scale(0.98);
}

/* 按钮扫光动画 */
.btn-shine {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    to right,
    transparent,
    rgba(255, 255, 255, 0.4),
    transparent
  );
  transform: skewX(-20deg);
  animation: shine 3s infinite;
}

/* === 动画定义 === */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@keyframes rotate {
  from { transform: translate(-50%, -50%) rotate(0deg); }
  to { transform: translate(-50%, -50%) rotate(360deg); }
}

@keyframes bounce-rotate {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-15px) rotate(3deg); }
}

@keyframes sparkle-anim {
  0%, 100% { transform: translate(var(--x), var(--y)) scale(0); opacity: 0; }
  50% { transform: translate(var(--x), var(--y)) scale(1.2); opacity: 1; }
}

@keyframes shine {
  0% { left: -100%; opacity: 0; }
  20% { left: 200%; opacity: 1; }
  100% { left: 200%; opacity: 0; }
}
</style>