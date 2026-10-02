<template>
  <!-- 自定义箭头指针：整层铺满视口，pointer-events:none 保证完全不吃鼠标事件 -->
  <div v-if="enabled" class="fm-cursor" :class="{ 'is-hidden': hidden, 'is-active': hovering, 'is-down': pressing }">
    <!--
      位置层：只负责跟随鼠标（每帧由 JS 写 translate3d），不能挂 transition。
      缩放层：内层 SVG，只负责悬停放大 / 按下收缩（由 CSS 类驱动，带过渡）。
      两层拆开是必须的——同一个 transform 属性被 JS 和 CSS 同时写会互相覆盖，
      把它们放到不同元素上才能各管各的。
    -->
    <div class="fm-cursor-arrow" ref="arrowRef">
      <!--
        箭头形状用内联 SVG 画（CSS 画不出这种带凹口的指针轮廓）。
        path 起点 (0,0) 就是箭尖，所以 SVG 左上角天然对齐鼠标坐标，热点无需偏移。
        配色取自 App 图标：深青描边 + 青绿填充，扁平化处理（无高光、无立体投影）。
      -->
      <svg class="fm-cursor-arrow-face" viewBox="0 0 24 24" width="21" height="21" aria-hidden="true">
        <path d="M0,0 L0,18 L4.5,13.5 L7.9,21.4 L11.3,19.7 L7.9,11.8 L13.5,11.8 Z" />
      </svg>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';

// 是否启用自定义光标。只有鼠标类设备 + 未开启"减弱动态效果"时才为 true
const enabled = ref(false);

const arrowRef = ref<HTMLDivElement | null>(null);

// 三个状态：鼠标移出窗口隐藏 / 悬停在可点元素上 / 正在按下
const hidden = ref(true);
const hovering = ref(false);
const pressing = ref(false);

// 最新鼠标坐标。起始给 -100 让光标在第一次移动前藏在屏幕外，避免从左上角滑入
let posX = -100;
let posY = -100;
// rAF 句柄同时兼作节流标记：为 0 表示当前帧还没排过更新
let rafId = 0;

// 悬停到这些元素上时箭头放大，提示"这里可点"
const HOVER_SELECTOR = 'a, button, [role="button"], .clickable, input, textarea, select, .n-button, .n-switch';

const handleMouseMove = (e: MouseEvent) => {
  posX = e.clientX;
  posY = e.clientY;
  if (hidden.value) hidden.value = false;

  // 高刷鼠标一秒能报上千次位置，没必要每次都写样式；
  // 每帧只落一次，用 translate3d 走 GPU 合成阶段，不触发重排，跟手且不掉帧
  if (rafId) return;
  rafId = requestAnimationFrame(() => {
    rafId = 0;
    if (arrowRef.value) {
      arrowRef.value.style.transform = `translate3d(${posX}px, ${posY}px, 0)`;
    }
  });
};

// 事件委托：不管鼠标移到哪个元素，都从它往上找最近的"可点元素"
const handleMouseOver = (e: MouseEvent) => {
  const el = e.target as HTMLElement | null;
  hovering.value = !!(el && el.closest(HOVER_SELECTOR));
};

const handleMouseDown = () => {
  pressing.value = true;
};

const handleMouseUp = () => {
  pressing.value = false;
};

// 鼠标离开浏览器窗口时隐藏光标，否则它会停在边缘不动，让人以为卡死
const handleMouseLeave = () => {
  hidden.value = true;
};

const handleMouseEnter = () => {
  hidden.value = false;
};

onMounted(() => {
  // (pointer: fine) 命中说明主指针是鼠标 / 触控板；
  // 触摸屏没有 hover 概念，强行启用会让光标僵在屏幕上，故直接不启用
  const finePointer = window.matchMedia('(pointer: fine)').matches;
  // 用户系统开了"减弱动态效果"时也退回系统光标，尊重无障碍设置
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  if (!finePointer || reduceMotion) return;

  enabled.value = true;
  // 给 html 打标记类，样式表据此把全站系统光标隐藏
  document.documentElement.classList.add('fm-custom-cursor');

  window.addEventListener('mousemove', handleMouseMove, { passive: true });
  window.addEventListener('mouseover', handleMouseOver, { passive: true });
  window.addEventListener('mousedown', handleMouseDown);
  window.addEventListener('mouseup', handleMouseUp);
  document.addEventListener('mouseleave', handleMouseLeave);
  document.addEventListener('mouseenter', handleMouseEnter);
});

onUnmounted(() => {
  // 卸载时清理：还原系统光标、摘掉事件、停掉动画帧，避免内存泄漏
  document.documentElement.classList.remove('fm-custom-cursor');
  window.removeEventListener('mousemove', handleMouseMove);
  window.removeEventListener('mouseover', handleMouseOver);
  window.removeEventListener('mousedown', handleMouseDown);
  window.removeEventListener('mouseup', handleMouseUp);
  document.removeEventListener('mouseleave', handleMouseLeave);
  document.removeEventListener('mouseenter', handleMouseEnter);
  if (rafId) cancelAnimationFrame(rafId);
});
</script>

<style scoped>
/* 启用自定义光标时隐藏全站系统光标。
   用 :global 是因为要作用于整个文档，scoped 只会命中本组件元素 */
:global(html.fm-custom-cursor),
:global(html.fm-custom-cursor *) {
  cursor: none !important;
}

.fm-cursor {
  position: fixed;
  inset: 0;
  /* 高于 Naive UI 弹窗层级，保证任何弹层上方都能看到 */
  z-index: 99999;
  pointer-events: none;
  transition: opacity 0.2s ease;
}

.fm-cursor.is-hidden {
  opacity: 0;
}

/* 位置层：位移每帧都在变，绝不能挂 transition，
   否则过渡会被不断重启，箭头反而滞后并抖动 */
.fm-cursor-arrow {
  position: absolute;
  top: 0;
  left: 0;
  will-change: transform;
}

/* 缩放层：负责悬停放大 / 按下收缩，锚点锁在箭尖。
   若用默认的中心锚点，放大后箭头会整体漂移，热点就不在鼠标上了 */
.fm-cursor-arrow-face {
  display: block;
  /* viewBox 贴着图形边缘，描边会往外溢出半个宽度，放开裁剪才不会被切掉 */
  overflow: visible;
  transform-origin: 0 0;
  /* 常态一层浅投影，保证箭头落在浅色内容上也看得清轮廓 */
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.28));
  transition: transform 0.18s cubic-bezier(0.16, 1, 0.3, 1), filter 0.25s ease;
}

/* 箭头本体：深青描边 + 青绿填充，取自 App 图标的配色，扁平无立体感 */
.fm-cursor-arrow-face path {
  fill: white;
  stroke: #00c9a7;
  stroke-width: 5;
  /* 圆角连接，让尖角不生硬 */
  stroke-linejoin: round;
  /* 先描边后填充：描边只露出外侧，箭头内部不被描边吃掉，视觉更饱满 */
  paint-order: stroke fill;
}

/* 暗色主题下深青描边会糊进背景，换成浅描边保证轮廓清晰 */
:global(html.dark-theme) .fm-cursor-arrow-face path {
  fill: black;
}

/* 悬停在可点元素上：箭头放大一圈并透出品牌色光晕 */
.fm-cursor.is-active .fm-cursor-arrow-face {
  transform: scale(1.18);
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.28)) drop-shadow(0 0 6px rgba(0, 201, 167, 0.8));
}

/* 按下时收缩一下，给出"按压"反馈。
   写在 is-active 之后，两者同时存在时以按下为准 */
.fm-cursor.is-down .fm-cursor-arrow-face {
  transform: scale(0.85);
}
</style>
