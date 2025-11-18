<template>
  <van-config-provider :theme="currentTheme">
    <div class="mobile-page">
      <!-- 顶部导航栏 -->
      <van-nav-bar
        title="移动端示例"
        left-text="返回"
        right-text="按钮"
        @click-left="onClickLeft"
        @click-right="onClickRight"
      />
      
      <!-- 页面内容 -->
      <div class="content">
        <van-cell-group inset>
          <van-cell title="单元格" value="内容" />
          <van-cell title="单元格" value="内容" label="描述信息" />
        </van-cell-group>
        
        <van-divider :style="{ color: '#00c9a7', borderColor: '#00c9a7', padding: '0 16px' }">
          分割线
        </van-divider>
        
        <van-button type="primary" block style="margin: 10px 0;">
          主要按钮
        </van-button>
        
        <van-button type="default" block style="margin: 10px 0;">
          默认按钮
        </van-button>
        
        <van-progress
          :percentage="50"
          stroke-width="8"
          style="margin: 20px 0;"
        />
        
        <van-card
          num="2"
          price="2.00"
          desc="描述信息"
          title="商品标题"
          thumb="https://fastly.jsdelivr.net/npm/@vant/assets/ipad.jpeg"
        />
      </div>
      
      <!-- 底部标签栏 -->
      <van-tabbar v-model="activeTab">
        <van-tabbar-item icon="home-o">首页</van-tabbar-item>
        <van-tabbar-item icon="search">搜索</van-tabbar-item>
        <van-tabbar-item icon="friends-o">我的</van-tabbar-item>
      </van-tabbar>
    </div>
  </van-config-provider>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { showToast } from 'vant';

// 获取全局主题
const isDark = inject('isDark', ref(false));

// 计算当前主题
const currentTheme = computed(() => {
  return isDark.value ? 'dark' : 'light';
});

// 标签栏激活项
const activeTab = ref(0);

// 导航栏事件
const onClickLeft = () => {
  showToast('返回');
};

const onClickRight = () => {
  showToast('按钮');
};
</script>

<style scoped>
.mobile-page {
  min-height: 100vh;
  background-color: var(--van-background-color);
  padding-bottom: 50px; /* 为底部标签栏留出空间 */
}

.content {
  padding: 10px;
}

/* 深色主题样式 */
.dark .mobile-page {
  background-color: #121212;
}

/* 浅色主题样式 */
.light .mobile-page {
  background-color: #f5f5f5;
}
</style>