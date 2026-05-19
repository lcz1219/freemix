<template>
  <transition name="fade">
    <div v-if="show" class="celebration-overlay">
      <!-- 背景光晕 -->
      <div class="ambient-glow"></div>
      
      <canvas ref="canvas" class="confetti-canvas"></canvas>
      
      <div class="celebration-card" :class="{ 'enter': showContent }">
        <div class="icon-wrapper">
          <div class="trophy-icon">🏆</div>
          <div class="sparkles">
            <span style="--d: 0s; --x: -20px; --y: -20px">✦</span>
            <span style="--d: 0.5s; --x: 20px; --y: -30px">✦</span>
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
          继续征程
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

const colors = ['#00c9a7', '#34d399', '#6ee7b7', '#a7f3d0', '#FFFFFF'];

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
.celebration-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(10, 10, 14, 0.92);
  backdrop-filter: blur(8px);
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* 背景氛围光 */
.ambient-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60vw;
  height: 60vh;
  background: radial-gradient(circle, rgba(0, 201, 167, 0.12) 0%, transparent 70%);
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
  max-width: 420px;
  padding: 40px 36px;
  border-radius: 16px;
  text-align: center;
  
  background: rgba(30, 30, 40, 0.6);
  backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 
    0 8px 30px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 0 rgba(255, 255, 255, 0.05);
  
  transform: scale(0.92) translateY(30px);
  opacity: 0;
  transition: all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.celebration-card.enter {
  transform: scale(1) translateY(0);
  opacity: 1;
}

/* === 图标区域 === */
.icon-wrapper {
  position: relative;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 16px;
}

.trophy-icon {
  font-size: 72px;
  filter: drop-shadow(0 8px 16px rgba(0, 0, 0, 0.3));
  position: relative;
  z-index: 2;
  animation: bounce-rotate 3s ease-in-out infinite;
}

.sparkles span {
  position: absolute;
  top: 50%;
  left: 50%;
  font-size: 14px;
  color: #00c9a7;
  opacity: 0;
  animation: sparkle-anim 2.5s infinite;
  animation-delay: var(--d);
  transform: translate(var(--x), var(--y));
}

/* === 文本区域 === */
.celebration-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
  letter-spacing: 1px;
  color: #00c9a7;
  text-shadow: 0 2px 8px rgba(0, 201, 167, 0.3);
}

.celebration-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 24px;
  font-weight: 400;
}

/* === 目标展示框 === */
.goal-container {
  position: relative;
  margin-bottom: 32px;
}

.goal-title {
  position: relative;
  background: rgba(0, 201, 167, 0.08);
  padding: 14px 20px;
  border-radius: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #e0e0e0;
  border: 1px solid rgba(0, 201, 167, 0.2);
  z-index: 2;
}

.goal-glow {
  position: absolute;
  inset: 0;
  border-radius: 12px;
  background: linear-gradient(135deg, #00c9a7, #00a98f);
  opacity: 0.15;
  filter: blur(12px);
  transform: translateY(4px);
  z-index: 1;
}

/* === 按钮 === */
.continue-btn {
  position: relative;
  background: linear-gradient(92deg, #00c9a7 0%, #00a98f 100%);
  border: none;
  padding: 14px 0;
  color: white;
  font-size: 15px;
  font-weight: 600;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 201, 167, 0.3);
  width: 100%;
  letter-spacing: 0.5px;
}

.continue-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 201, 167, 0.4);
}

.continue-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(0, 201, 167, 0.3);
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

@keyframes bounce-rotate {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@keyframes sparkle-anim {
  0%, 100% { transform: translate(var(--x), var(--y)) scale(0); opacity: 0; }
  50% { transform: translate(var(--x), var(--y)) scale(1); opacity: 1; }
}
</style>