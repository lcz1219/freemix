<template>
  <svg viewBox="0 0 512 512" version="1.1" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <!-- 定义渐变让图标更有质感 -->
      <linearGradient id="iconGradient" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" style="stop-color:#2AF598;stop-opacity:1" />
        <stop offset="100%" style="stop-color:#009EFD;stop-opacity:1" />
      </linearGradient>
    </defs>

    <!-- 1. 背景容器：墨绿色 -->
    <!-- 模仿原图的粗边框风格，stroke为深青色，fill为墨绿色 -->
    <rect x="30" y="30" width="452" height="452" rx="100" ry="100" fill="#0f2e26" stroke="#065f56"
      stroke-width="40" />

    <!-- 2. AI 核心图标：星光 (Sparkles) -->
    <g transform="translate(0, -20)" fill="#1de9b6">
      <!-- 主星 -->
      <path
        d="M256,120 C270,200 280,220 360,235 C280,250 270,270 256,350 C242,270 232,250 152,235 C232,220 242,200 256,120 Z" />
      <!-- 右上角小星 -->
      <path
        d="M380,100 C385,130 390,140 420,145 C390,150 385,160 380,190 C375,160 370,150 340,145 C370,140 375,130 380,100 Z" />
    </g>

    <!-- 3. 底部进度条容器 (灰色背景) -->
    <rect x="136" y="350" width="240" height="40" rx="20" ry="20" fill="#1f4a40" />

    <!-- 4. 底部进度条 (高亮部分) -->
    <!-- 模仿原图的“进行中”状态 -->
    <rect x="136" y="350" width="160" height="40" rx="20" ry="20" fill="#1de9b6" />
  </svg>
</template>
