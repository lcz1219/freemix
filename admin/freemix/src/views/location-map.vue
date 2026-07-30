<template>
  <div class="location-map-page">
    <n-layout has-sider style="height:100%">
      <!-- 左侧：可折叠菜单侧栏 -->
      <n-layout-sider
        bordered
        :collapsed-width="64"
        :width="320"
        collapse-mode="width"
        v-model:collapsed="sidebarCollapsed"
        :native-scrollbar="false"
        
        show-trigger="bar"
        style="background:rgb(18,18,18);height:100%;"
      >
        <!-- 无数据占位 -->
        <div v-if="locationRecords.length === 0" style="padding:20px;text-align:center;color:#8b949e;font-size:13px;">
          暂无打卡记录
        </div>

        <!-- 折叠态：每个目标显示一个定位图标 -->
        <div v-if="locationRecords.length > 0 && sidebarCollapsed" class="collapsed-icons">
          <div
            v-for="group in goalGroups"
            :key="group.goalId"
            class="collapsed-icon-wrapper"
            :title="group.goalTitle"
            :class="{ 'icon-active': group.records.some(r => r.childGoalId === activeRecordId) }"
            @click="onCardClick(group.records[0])"
          >
            <svg viewBox="0 0 16 16" width="20" height="20">
              <circle cx="8" cy="6" r="2.5" fill="none" stroke="#00c9a7" stroke-width="1.5"/>
              <path d="M8 15C8 15 13 10 13 6A5 5 0 1 0 3 6C3 10 8 15 8 15Z" fill="none" stroke="#00c9a7" stroke-width="1.2"/>
            </svg>
          </div>
        </div>

        <!-- 展开态：完整分组卡片列表 -->
        <template v-if="locationRecords.length > 0 && !sidebarCollapsed">
          <!-- 统计头部 -->
          <div class="sidebar-stats">
            <div class="sidebar-stats-row">
              <span class="sidebar-stat-num">{{ locationRecords.length }}</span>
              <span class="sidebar-stat-label">个地点</span>
              <span class="sidebar-stat-badge">已打卡</span>
            </div>
            <span class="sidebar-stat-sub">{{ uniqueGoals.length }} 个目标</span>
            <div class="sidebar-stats-line"></div>
          </div>

          <!-- 按目标分组卡片列表 -->
          <div class="group-list">
            <div v-for="group in goalGroups" :key="group.goalId" class="group-item">
              <!-- 父级：目标名 + 数量 + 折叠箭头 -->
              <div class="group-header" @click="toggleGroup(group.goalId)">
                <span class="group-title">{{ group.goalTitle }}</span>
                <span class="group-badge">{{ group.records.length }}</span>
                <svg
                  :class="['group-arrow', { expanded: expandedGoalIds.has(group.goalId) }]"
                  viewBox="0 0 16 16" width="12" height="12"
                >
                  <path d="M6 4l4 4-4 4" fill="none" stroke="#8b949e" stroke-width="1.5"/>
                </svg>
              </div>
              <!-- 子目标卡片 -->
              <div v-if="expandedGoalIds.has(group.goalId)" class="group-cards">
                <div
                  v-for="record in group.records"
                  :key="record.childGoalId"
                  class="location-card"
                  :class="{ 'card-active': activeRecordId === record.childGoalId }"
                  @click="onCardClick(record)"
                >
                  <div class="card-top">
                    <span class="card-title">{{ record.goalTitle }}</span>
                    <span class="card-time">{{ formatDate(record.finishDate) }}</span>
                  </div>
                  <div class="card-mid">
                    <span class="card-sub">{{ record.childGoalMessage }}</span>
                  </div>
                  <div class="card-addr">
                    <svg viewBox="0 0 16 16" width="12" height="12">
                      <circle cx="8" cy="6" r="2.5" fill="none" stroke="#00c9a7" stroke-width="1.5"/>
                      <path d="M8 15C8 15 13 10 13 6A5 5 0 1 0 3 6C3 10 8 15 8 15Z" fill="none" stroke="#00c9a7" stroke-width="1.2"/>
                    </svg>
                    <span>{{ record.locationName }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </n-layout-sider>

      <!-- 右侧：地图区域 -->
      <n-layout>
        <div class="map-area">
          <div id="location-map" class="map-container"></div>
          <div v-if="isMapLoading" class="map-loading-overlay">地图加载中...</div>
          <div v-if="locationRecords.length === 0 && !isMapLoading" class="empty-overlay">
            暂无打卡记录，完成子目标时选择地点即可在这里看到
          </div>
        </div>
      </n-layout>
    </n-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue';
import { useStore } from 'vuex';
import AMapLoader from '@amap/amap-jsapi-loader';
import { getM, isSuccess } from '@/utils/request';
import { NLayout, NLayoutSider } from 'naive-ui';

const store = useStore();

// 地点记录数据
interface LocationRecord {
  goalId: string;
  goalTitle: string;
  childGoalId: string;
  childGoalMessage: string;
  locationName: string;
  locationCoord: [number, number];
  finishDate: string;
}

const locationRecords = ref<LocationRecord[]>([]);
const isMapLoading = ref(false);
const activeRecordId = ref('');

// 侧栏折叠状态（n-layout-sider 折叠）
const sidebarCollapsed = ref(false);

// 按目标 ID 分组
const goalGroups = computed(() => {
  const groups = new Map<string, { goalId: string; goalTitle: string; records: LocationRecord[] }>();
  locationRecords.value.forEach((record) => {
    const g = groups.get(record.goalId);
    if (g) {
      g.records.push(record);
    } else {
      groups.set(record.goalId, {
        goalId: record.goalId,
        goalTitle: record.goalTitle,
        records: [record],
      });
    }
  });
  return Array.from(groups.values());
});

// 展开的分组 ID 集合（控制子目标卡片显示/隐藏）
const expandedGoalIds = ref(new Set<string>());

// 切换分组折叠
const toggleGroup = (goalId: string) => {
  const set = new Set(expandedGoalIds.value);
  if (set.has(goalId)) {
    set.delete(goalId);
  } else {
    set.add(goalId);
  }
  expandedGoalIds.value = set;
};

// 数据加载后自动展开所有分组；点击标记时自动展开对应分组
watch(activeRecordId, (id) => {
  if (!id) return;
  const record = locationRecords.value.find(r => r.childGoalId === id);
  if (record) {
    const set = new Set(expandedGoalIds.value);
    set.add(record.goalId);
    expandedGoalIds.value = set;
  }
});

// 加载完数据后默认全部展开
watch(locationRecords, (records) => {
  if (records.length > 0) {
    expandedGoalIds.value = new Set(records.map(r => r.goalId));
  }
}, { immediate: true });

// 卡片 DOM 引用映射（用于标记点点击后自动滚动卡片到可见区域）
const cardRefMap = new Map<string, HTMLElement>();
const cardStripRef = ref<HTMLElement | null>(null);

const setCardRef = (id: string, el: any) => {
  if (el) cardRefMap.set(id, el);
};

// 唯一关联目标数
const uniqueGoals = computed(() => {
  const ids = new Set(locationRecords.value.map(r => r.goalId));
  return ids.size;
});

// AMap 实例
let amapInstance: any = null;
let amapMarkers: any[] = [];
let amapKey = '';
let infoWindow: any = null;
// 标记 ↔ 记录映射（用于更新选中态）
const markerRecordMap = new Map<any, LocationRecord>();

// 构建信息窗 HTML（供多处使用）
const buildInfoContent = (record: LocationRecord) => {
  return `<div style="min-width:200px;padding:6px 0;font-family:system-ui,sans-serif;">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;">
      <div style="font-weight:700;font-size:14px;color:#e6edf3;">${escapeHtml(record.goalTitle)}</div>
      <div style="font-size:11px;color:#6e7681;padding:2px 8px;background:rgb(18,18,18);border-radius:4px;">${formatDate(record.finishDate)}</div>
    </div>
    <div style="font-size:13px;color:#8b949e;margin-bottom:8px;padding-left:2px;">${escapeHtml(record.childGoalMessage)}</div>
    <div style="display:flex;align-items:center;gap:4px;font-size:13px;color:#00c9a7;padding-top:8px;border-top:1px solid rgba(255,255,255,0.06);">
      <svg viewBox="0 0 16 16" width="12" height="12" style="flexShrink:0">
        <circle cx="8" cy="6" r="2.5" fill="none" stroke="#00c9a7" stroke-width="1.5"/>
        <path d="M8 15C8 15 13 10 13 6A5 5 0 1 0 3 6C3 10 8 15 8 15Z" fill="none" stroke="#00c9a7" stroke-width="1.2"/>
      </svg>
      <span>${escapeHtml(record.locationName)}</span>
    </div>
  </div>`;
};

// 更新所有标记的选中态样式
const updateMarkerActiveStates = () => {
  amapMarkers.forEach((marker) => {
    const record = markerRecordMap.get(marker);
    if (!record) return;
    const isActive = activeRecordId.value === record.childGoalId;
    const size = isActive ? 14 * 1.8 : 14;
    const glow = isActive ? 0.9 : 0.4;
    const dotSize = Math.max(10, size * 1.2);
    const outerSize = dotSize + 6;
    const content = `<div style="position:relative;width:${outerSize}px;height:${outerSize}px;transition:all 0.3s;">
      <div style="position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);
        width:${dotSize}px;height:${dotSize}px;background:#00c9a7;border-radius:50%;
        border:${Math.max(2, size * 0.12)}px solid #fff;
        box-shadow:0 0 ${size * 0.8}px rgba(0,201,167,${glow});opacity:${isActive ? 1 : 0.6};"></div>
    </div>`;
    marker.setContent(content);
    marker.setzIndex(isActive ? 200 : 100);
  });
};

// 加载地点记录
const loadLocationRecords = async () => {
  try {
    const user = store.state.user;
    if (!user?.username) return;
    const res = await getM('getLocationRecords/' + user.username);
    if (isSuccess(res)) {
      locationRecords.value = res.data.data || [];
    }
  } catch (e) {
    console.error('加载地点记录失败:', e);
  }
};

// 初始化地图
const initMap = async () => {
  isMapLoading.value = true;
  try {
    // 1. 从后端获取 Key
    if (!amapKey) {
      const configRes = await getM('amap/config');
      if (isSuccess(configRes)) {
        const data = configRes.data.data || configRes.data || configRes;
        amapKey = data.key || '';
      }
      if (!amapKey) {
        console.error('获取高德地图 Key 失败');
        isMapLoading.value = false;
        return;
      }
    }

    // 2. 加载高德 SDK
    const AMap = await AMapLoader.load({
      key: amapKey,
      version: '2.0',
      plugins: ['AMap.InfoWindow'],
    });

    // 3. 销毁旧实例
    if (amapInstance) {
      amapInstance.destroy();
    }

    // 4. 创建地图
    amapInstance = new AMap.Map('location-map', {
      zoom: 5,
      center: [105, 35],
      mapStyle: 'amap://styles/dark',
      resizeEnable: true,
    });

    // 5. 创建信息窗口
    infoWindow = new AMap.InfoWindow({
      offset: new AMap.Pixel(0, -30),
      closeWhenClickMap: true,
    });

    // 6. 渲染标记
    renderMarkers(AMap);

    isMapLoading.value = false;
  } catch (e) {
    console.error('加载高德地图失败:', e);
    isMapLoading.value = false;
  }
};

// 渲染标记点
const renderMarkers = (AMap: any) => {
  if (!amapInstance) return;
  // 清除旧标记
  amapMarkers.forEach(m => amapInstance.remove(m));
  amapMarkers = [];

  if (locationRecords.value.length === 0) return;

  // 标记内容辅助：选中态放大 + 强发光，非选中缩小 + 弱发光
  const markerContent = (size: number, isActive: boolean = false) => {
    const activeSize = isActive ? size * 1.8 : size;
    const glow = isActive ? 0.9 : 0.4;
    const dotSize = Math.max(10, activeSize * 1.2);
    const outerSize = dotSize + 6;
    return `<div style="position:relative;width:${outerSize}px;height:${outerSize}px;transition:all 0.3s;">
      <div style="position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);
        width:${dotSize}px;height:${dotSize}px;background:#00c9a7;border-radius:50%;
        border:${Math.max(2, activeSize * 0.12)}px solid #fff;
        box-shadow:0 0 ${activeSize * 0.8}px rgba(0,201,167,${glow});opacity:${isActive ? 1 : 0.6};"></div>
    </div>`;
  };

  locationRecords.value.forEach((record) => {
    const [lng, lat] = record.locationCoord;
    const marker = new AMap.Marker({
      position: [lng, lat],
      content: markerContent(14, false),
      offset: new AMap.Pixel(-14, -14),
      zIndex: 100,
    });
    markerRecordMap.set(marker, record);

    // 点击标记 → 高亮菜单项 + 弹窗
    marker.on('click', () => {
      activeRecordId.value = record.childGoalId;
      updateMarkerActiveStates();
      infoWindow.setContent(buildInfoContent(record));
      infoWindow.open(amapInstance, [lng, lat]);
    });

    // 悬停时高亮菜单项
    marker.on('mouseover', () => {
      activeRecordId.value = record.childGoalId;
    });
    // 移出时取消高亮（如果没点击过）
    marker.on('mouseout', () => {
      // updateMarkerActiveStates() 会恢复正确的选中态
    });

    amapInstance.add(marker);
    amapMarkers.push(marker);
  });

  // 自适应缩放
  if (locationRecords.value.length > 1) {
    amapInstance.setFitView(amapMarkers, false, [60, 60, 60, 60]);
  } else if (locationRecords.value.length === 1) {
    const [lng, lat] = locationRecords.value[0].locationCoord;
    amapInstance.setZoomAndCenter(14, [lng, lat]);
  }
};

// 将卡片滚动到可见区域（纵向滚动）
const scrollToCard = (id: string) => {
  const el = cardRefMap.get(id);
  if (el && cardStripRef.value) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }
};

// 菜单项点击 → 飞到地点 + 弹信息窗 + 同步标记选中态
const onCardClick = (record: LocationRecord) => {
  if (!amapInstance) return;
  activeRecordId.value = record.childGoalId;
  const [lng, lat] = record.locationCoord;
  amapInstance.setZoomAndCenter(15, [lng, lat]);
  updateMarkerActiveStates();
  infoWindow.setContent(buildInfoContent(record));
  infoWindow.open(amapInstance, [lng, lat]);
};

// HTML 转义
const escapeHtml = (str: string) => {
  if (!str) return '';
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
};

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
};

onMounted(async () => {
  await loadLocationRecords();
  await nextTick();
  initMap();
});

onUnmounted(() => {
  if (amapInstance) {
    amapInstance.destroy();
    amapInstance = null;
    amapMarkers = [];
    infoWindow = null;
  }
});
</script>

<style scoped>
/* 全屏布局 */
.location-map-page {
  height: 88vh;
  width: 100%;
  overflow: hidden;
}

/* ===== 侧栏统计头部 ===== */
.sidebar-stats {
  position: relative;
  padding: 20px 16px 16px;
  overflow: hidden;
}

.sidebar-stats-row {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 2px;
}

.sidebar-stat-num {
  font-size: 32px;
  font-weight: 800;
  color: #00c9a7;
  line-height: 1;
  letter-spacing: -1px;
}

.sidebar-stat-label {
  font-size: 14px;
  color: var(--fm-base-text, #e6edf3);
}

.sidebar-stat-badge {
  margin-left: auto;
  font-size: 10px;
  color: #00c9a7;
  background: rgba(0,201,167,0.12);
  padding: 0 8px;
  border-radius: 4px;
  line-height: 20px;
  letter-spacing: 0.5px;
}

.sidebar-stat-sub {
  font-size: 12px;
  color: var(--fm-secondary-text, #8b949e);
  padding-left: 2px;
}

/* 统计头部底部绿色渐变线 */
.sidebar-stats-line {
  position: absolute;
  bottom: 0;
  left: 16px;
  right: 16px;
  height: 1px;
  background: linear-gradient(90deg, #00c9a7, transparent);
}

/* ===== 折叠态：图标列表 ===== */
.collapsed-icons {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 0;
}

.collapsed-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
}
.collapsed-icon-wrapper:hover {
  background: rgba(255,255,255,0.06);
}
.collapsed-icon-wrapper.icon-active svg circle {
  fill: #00c9a7;
}
.collapsed-icon-wrapper.icon-active svg path {
  stroke-width: 2;
}

/* ===== 分组列表（替代 n-menu） ===== */
.group-list {
  height: calc(100% - 100px);
  overflow-y: auto;
  padding: 0 8px 20px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

/* 单个分组 */
.group-item {
  display: flex;
  flex-direction: column;
}

/* 父级标题行 */
.group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 8px;
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.15s;
  user-select: none;
}
.group-header:hover {
  background: rgba(255,255,255,0.04);
}

.group-title {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  color: #e6edf3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.group-badge {
  font-size: 11px;
  color: #00c9a7;
  background: rgba(0,201,167,0.12);
  border-radius: 10px;
  padding: 0 8px;
  line-height: 18px;
  flex-shrink: 0;
}

.group-arrow {
  flex-shrink: 0;
  transition: transform 0.2s;
}
.group-arrow.expanded {
  transform: rotate(90deg);
}

/* 子目标卡片容器 */
.group-cards {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 2px 0 8px 0;
}

/* ===== 单张打卡卡片 ===== */
.location-card {
  background: rgb(26,26,26);
  border-radius: 10px;
  padding: 12px;
  border: 1px solid rgba(255,255,255,0.04);
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.location-card:hover {
  border-color: rgba(0,201,167,0.25);
  box-shadow: 0 0 12px rgba(0,201,167,0.08);
}

.card-active {
  border-color: rgba(0,201,167,0.35) !important;
  background: rgba(0,201,167,0.06) !important;
  box-shadow: 0 0 16px rgba(0,201,167,0.1) !important;
  position: relative;
}
.card-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 2px;
  background: #00c9a7;
  border-radius: 0 2px 2px 0;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-size: 13px;
  font-weight: 600;
  color: #e6edf3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 160px;
}
.card-time {
  font-size: 11px;
  color: #8b949e;
  flex-shrink: 0;
}

.card-mid {
  display: flex;
  align-items: center;
}
.card-sub {
  font-size: 12px;
  color: #8b949e;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-addr {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #00c9a7;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.card-addr svg {
  flex-shrink: 0;
}

/* ===== 右侧地图区域 ===== */
.map-area {
  flex: 1;
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
}

.map-container {
  width: 100%;
  height: 100%;
}

.map-loading-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(13, 17, 23, 0.8);
  color: var(--fm-secondary-text, #8b949e);
  font-size: 14px;
  z-index: 10;
}

.empty-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--fm-secondary-text, #8b949e);
  font-size: 14px;
  pointer-events: none;
}
</style>

<!-- 覆盖高德地图信息窗默认白色背景为暗色 -->
<style>
.amap-info-content {
  background: rgb(18,18,18) !important;
  padding: 12px 18px 12px 12px;
  line-height: 1.5;
  overflow: auto;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.6) !important;
  border: 1px solid rgba(255,255,255,0.06);
}

/* 信息窗箭头/尖角 */
.amap-info-sharp {
  border-top-color: rgb(18,18,18) !important;
  border-top-width: 10px !important;
}

/* 信息窗关闭按钮 */
.amap-info-close {
  color: #8b949e !important;
  font-size: 14px !important;
  top: 6px !important;
  right: 4px !important;
}

/* 信息窗容器间距 */
.amap-info-outer {
  margin-bottom: 8px !important;
}
</style>
