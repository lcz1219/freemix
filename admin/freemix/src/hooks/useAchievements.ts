import { ref } from 'vue';
import request from '@/utils/request';

const achievements = ref([]);
const loading = ref(false);

export function useAchievements() {
  
  const getMyAchievements = async () => {
    loading.value = true;
    try {
      const res = await request.get('/freemix/api/achievements/my');
      if (res.data.code === 200) {
        achievements.value = res.data.data;
      }
    } catch (error) {
      console.error('Failed to fetch achievements', error);
    } finally {
      loading.value = false;
    }
  };

  return {
    achievements,
    loading,
    getMyAchievements
  };
}
