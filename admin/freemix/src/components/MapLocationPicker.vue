<template>
  <n-modal v-model:show="visible" preset="card" title="选择完成地点" size="large" style="width: 650px; max-width: 95vw;" :mask-closable="false">
    <div class="picker-body">
      <!-- 搜索框 -->
      <div class="search-row">
        <n-input
          v-model:value="searchQuery"
          placeholder="搜索地点名称..."
          @keydown.enter="searchLocation"
          clearable
          round
        >
          <template #prefix>
            <n-icon><SearchOutline /></n-icon>
          </template>
        </n-input>
        <n-button type="primary" @click="searchLocation" :loading="isSearching" style="flex-shrink:0;">
          搜索
        </n-button>
      </div>

      <!-- 搜索结果 -->
      <div v-if="searchResults.length > 0" class="search-results">
        <div
          v-for="(item, index) in searchResults"
          :key="index"
          class="search-result-item"
          @click="selectSearchResult(item)"
        >
          <div class="result-name">{{ item.name }}</div>
          <div class="result-addr">{{ item.address }}</div>
        </div>
      </div>

      <!-- 地图 -->
      <div id="picker-map" class="picker-map"></div>

      <!-- 加载中 -->
      <div v-if="isMapLoading" class="map-loading">
        地图加载中...
      </div>

      <!-- 已选位置信息 -->
      <div v-if="selectedLocation" class="selected-info">
        <n-icon size="18" color="#00c9a7"><LocationOutline /></n-icon>
        <span class="selected-name">{{ selectedLocation.name }}</span>
        <span class="selected-coord">
          {{ selectedLocation.lng.toFixed(4) }}，{{ selectedLocation.lat.toFixed(4) }}
        </span>
      </div>
    </div>

    <template #footer>
      <div class="picker-footer">
        <n-button @click="handleCancel">取消</n-button>
        <n-button type="primary" @click="handleConfirm" :disabled="!selectedLocation">
          确认并完成
        </n-button>
      </div>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { ref, watch, onUnmounted } from 'vue';
import { NModal, NInput, NButton, NIcon } from 'naive-ui';
import { SearchOutline, LocationOutline } from '@vicons/ionicons5';
import AMapLoader from '@amap/amap-jsapi-loader';
import { getM, postM, isSuccess } from '@/utils/request';

const props = defineProps<{
  show: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void;
  (e: 'confirm', location: { name: string; lng: number; lat: number }): void;
}>();

const visible = ref(props.show);
const searchQuery = ref('');
const isSearching = ref(false);
const isMapLoading = ref(false);
const searchResults = ref<any[]>([]);

const selectedLocation = ref<{ name: string; lng: number; lat: number } | null>(null);

// AMap 实例
let amapInstance: any = null;
let amapMarker: any = null;
let amapKey = '';

// 同步父组件的 show 属性
watch(() => props.show, (val) => {
  visible.value = val;
  if (val) {
    searchQuery.value = '';
    searchResults.value = [];
    selectedLocation.value = null;
    initMap();
  }
});

watch(visible, (val) => {
  emit('update:show', val);
});

// 初始化地图：先从后端获取 Key，再加载高德 SDK
const initMap = async () => {
  isMapLoading.value = true;
  try {
    // 1. 从后端获取 Key（不暴露在前端代码中）
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

    // 2. 加载高德 JS SDK
    const AMap = await AMapLoader.load({
      key: amapKey,
      version: '2.0',
    });

    // 3. 销毁旧实例
    if (amapInstance) {
      amapInstance.destroy();
      amapInstance = null;
    }

    // 4. 创建地图（暗色主题）
    amapInstance = new AMap.Map('picker-map', {
      zoom: 12,
      center: [116.397428, 39.90923], // 默认北京
      mapStyle: 'amap://styles/dark',
      resizeEnable: true,
    });

    // 5. 点击地图选点
    amapInstance.on('click', async (e: any) => {
      const lng = e.lnglat.getLng();
      const lat = e.lnglat.getLat();
      // 通过后端逆地理编码获取地址名
      const addressName = await reverseGeocode(lng, lat);
      setLocation({ name: addressName, lng, lat }, false);
    });

    isMapLoading.value = false;
  } catch (e) {
    console.error('加载高德地图失败:', e);
    isMapLoading.value = false;
  }
};

// 逆地理编码（坐标 -> 地址，走后端代理保护 Key）
const reverseGeocode = async (lng: number, lat: number): Promise<string> => {
  try {
    const res = await getM('amap/regeo', { location: `${lng},${lat}` });
    if (isSuccess(res)) {
      const data = res.data.data || res.data || res;
      return data?.formatted_address || '坐标点';
    }
  } catch (e) {
    console.error('逆地理编码失败:', e);
  }
  return '坐标点';
};

// 设置地图标记点
const setLocation = (loc: { name: string; lng: number; lat: number }, flyTo: boolean = true) => {
  if (!amapInstance) return;
  // 移除旧标记
  if (amapMarker) {
    amapInstance.remove(amapMarker);
  }
  // 添加新标记
  amapMarker = new (window as any).AMap.Marker({
    position: [loc.lng, loc.lat],
    content: '<div class="picker-marker-dot"></div>',
    offset: new (window as any).AMap.Pixel(-10, -10),
  });
  amapInstance.add(amapMarker);

  selectedLocation.value = loc;
  if (flyTo) {
    amapInstance.setZoomAndCenter(16, [loc.lng, loc.lat]);
  }
};

// 搜索地点（走后端代理，保护 Key）
const searchLocation = async () => {
  if (!searchQuery.value.trim()) return;
  isSearching.value = true;
  try {
    const res = await getM('amap/geocode', { keywords: searchQuery.value });
    if (isSuccess(res)) {
      const pois = res.data.data || res.data || [];
      searchResults.value = (Array.isArray(pois) ? pois : []).map((item: any) => ({
        name: item.name,
        address: item.address,
        lng: parseFloat(item.location?.split(',')[0] || 0),
        lat: parseFloat(item.location?.split(',')[1] || 0),
      }));
    }
  } catch (e) {
    console.error('搜索地点失败:', e);
    searchResults.value = [];
  } finally {
    isSearching.value = false;
  }
};

// 选择搜索结果
const selectSearchResult = (item: any) => {
  searchResults.value = [];
  setLocation({ name: item.name + (item.address ? ' ' + item.address : ''), lng: item.lng, lat: item.lat });
};

// 确认
const handleConfirm = () => {
  if (!selectedLocation.value) return;
  emit('confirm', { ...selectedLocation.value });
  visible.value = false;
};

// 取消
const handleCancel = () => {
  visible.value = false;
};

onUnmounted(() => {
  if (amapInstance) {
    amapInstance.destroy();
    amapInstance = null;
  }
});
</script>

<style scoped>
.picker-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search-row {
  display: flex;
  gap: 8px;
}

.search-results {
  max-height: 160px;
  overflow-y: auto;
  border-radius: 8px;
  border: 1px solid var(--fm-border-color, #30363d);
}

.search-result-item {
  padding: 10px 14px;
  cursor: pointer;
  transition: background 0.15s;
  border-bottom: 1px solid rgba(48, 54, 61, 0.5);
}

.search-result-item:last-child {
  border-bottom: none;
}

.search-result-item:hover {
  background: rgba(0, 201, 167, 0.1);
}

.result-name {
  font-size: 13px;
  color: var(--fm-base-text, #e6edf3);
  font-weight: 500;
}

.result-addr {
  font-size: 11px;
  color: var(--fm-secondary-text, #8b949e);
  margin-top: 2px;
}

.picker-map {
  width: 100%;
  height: 340px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--fm-border-color, #30363d);
  position: relative;
}

.map-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: var(--fm-secondary-text, #8b949e);
  font-size: 14px;
}

.selected-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 8px;
  background: rgba(0, 201, 167, 0.08);
  border: 1px solid rgba(0, 201, 167, 0.2);
}

.selected-name {
  flex: 1;
  font-size: 13px;
  color: var(--fm-base-text, #e6edf3);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.selected-coord {
  font-size: 11px;
  color: var(--fm-secondary-text, #8b949e);
  flex-shrink: 0;
}

.picker-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>

<style>
/* 高德地图自定义标记点 */
.picker-marker-dot {
  width: 20px;
  height: 20px;
  background: #00c9a7;
  border-radius: 50%;
  border: 3px solid #fff;
  box-shadow: 0 0 12px rgba(0, 201, 167, 0.6);
}
</style>
