<template>
    <div class="recycle-page">
        <!-- 顶部导航栏 -->
        <NavBar active-tab="" />

        <!-- 主内容区域 -->
        <n-layout-content class="main-content">
            <div class="content-wrapper">
                <!-- 页面标题 -->
                <section class="page-header">
                    <n-page-header>
                        <template #title>
                            <h1 :class="isDark ? 'hero-title' : 'hero-title-light'">回收站</h1>
                        </template>
                        <template #subtitle>
                            <p :class="isDark ? 'hero-subtitle' : 'hero-subtitle-light'">
                                管理已删除的目标，可以恢复或永久删除
                            </p>
                        </template>
                        <template #extra>
                            <n-space>
                                <n-button v-if="selectedRowKeys.length > 0" type="error" @click="handleBatchDelete">
                                    <template #icon>
                                        <n-icon>
                                            <TrashIcon />
                                        </n-icon>
                                    </template>
                                    批量删除
                                </n-button>
                                <n-button v-if="selectedRowKeys.length > 0" type="primary"
                                    @click="handleBatchRestore()">
                                    <template #icon>
                                        <n-icon>
                                            <RestoreIcon />
                                        </n-icon>
                                    </template>
                                    批量恢复
                                </n-button>
                            </n-space>
                        </template>
                    </n-page-header>
                </section>

                <!-- 回收站内容 -->
                <section class="recycle-content">
                    <n-card :bordered="false" :class="isDark ? 'content-card' : 'content-card-light'">
                        <n-empty v-if="data.length === 0" description="回收站为空"
                            :class="isDark ? 'empty-state' : 'empty-state-light'">
                            <template #icon>
                                <n-icon>
                                    <TrashIcon />
                                </n-icon>
                            </template>
                            <template #extra>
                                <n-button type="primary" @click="$router.push('/home')">
                                    返回主页
                                </n-button>
                            </template>
                        </n-empty>

                        <n-data-table v-else ref="dataTableInst" :columns="columns" :data="data"
                            :pagination="pagination" :row-key="row => row._id"
                            v-model:checked-row-keys="selectedRowKeys" :scroll-x="800" />
                    </n-card>
                </section>
            </div>
        </n-layout-content>

        <!-- 页脚 -->
        <n-layout-footer class="footer" bordered>
            <p>© 2025 目标追踪者 - 您的目标完成度系统 | 让每一份努力都能被量化</p>
        </n-layout-footer>
    </div>
</template>

<script setup>
import NavBar from '../components/NavBar.vue';
import { ref, reactive, computed, watch, h, onMounted, onUnmounted, render, inject } from 'vue'
import {
    NDataTable,
    NButton,
    NInput,
    NLayoutContent,
    NLayoutFooter,
    NCard,
    NPageHeader,
    NSpace,
    NTooltip,
    NIcon,
    NEmpty,
    useMessage
} from 'naive-ui'
import { useStore } from 'vuex';
import { getM, postM, isSuccess, getMPaths, uploadFile } from '../utils/request'
import { TrashOutline as TrashIcon, Refresh as RestoreIcon } from '@vicons/ionicons5'
import { useRouter } from 'vue-router'

onMounted(() => {
    recycle()
});

const store = useStore();
const router = useRouter();
const message = useMessage();
const isDark = inject('isDark', ref(false));

// 选中的行
const selectedRowKeys = ref([]);

// 格式化日期
const formatDate = (dateString) => {
    if (!dateString) return '未设置';

    const date = new Date(dateString);
    if (isNaN(date.getTime())) return '日期无效';

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
};

const recycle = async () => {
    try {
        const res = await getMPaths("recycle", store.state.user.username);
        if (isSuccess(res)) {
            data.value = res.data.data || [];
            data.value.forEach(goal => {
                goal.deadlineString = formatDate(goal.deadline);
            });
        } else {
            message.error('获取回收站数据失败');
        }
    } catch (error) {
        console.error(error);
        message.error('获取回收站数据时发生错误');
    } finally {
    }
};

// 批量删除
const handleBatchDelete = () => {
    // 这里应该调用删除API
    message.success(`成功删除 ${selectedRowKeys.value.length} 个项目`);
    // 重新加载数据
    recycle();
    selectedRowKeys.value = [];
};

// 批量恢复
const handleBatchRestore = () => {
    selectedRowKeys.value.forEach(e => {

        data.value.forEach(e1 => {
            if (e1._id == e) {
                recoverGoal(e1)
            }
        })

    })

    // 这里应该调用恢复API
    message.success(`成功恢复 ${selectedRowKeys.value.length} 个项目`);
    // 重新加载数据
    recycle();
    selectedRowKeys.value = [];
};
const recoverGoal = async (row) => {
    row.del = 0;
    const res = await postM('editGoal', row);
    if (isSuccess(res)) {
        // message.success('目标创建成功')
        // router.push('/home');
        recycle();
    }
    // 提交后跳转到主页


}

const columns = [
    {
        type: 'selection',
        fixed: 'left',
        key: 'id',
        disabled: (row) => row.disRecover ,

    },
    {
        title: '目标名称',
        key: 'title',
        // ellipsis: true,
        sorter: (row1, row2) => row1.title.localeCompare(row2.title),
        resizable: true
    },
    {
        title: '负责人',
        key: 'owner',
        sorter: (row1, row2) => row1.owner.localeCompare(row2.owner),
        width: 120
    },
    {
        title: '截止日期',
        key: 'deadlineString',
        defaultSortOrder: false,
        sorter: {
            compare: (a, b) => new Date(a.deadline) - new Date(b.deadline),
            multiple: 3
        },
        width: 120
    },
    {
    title: '操作',
    key: 'option',
    fixed: 'right',
    width: 150,
    render(row) {
        return h('div', { style: 'display: flex; gap: 8px;' }, [
            // 恢复按钮（带 Tooltip）
            h(NTooltip, { trigger: 'hover' }, {
                // ✅ 关键修复 1：用函数返回插槽内容
                default: () => h('span', row.disRecover?'超过30天无法恢复':'恢复此目标'),
                // ✅ 关键修复 2：正确挂载触发元素
                trigger: () => h(NButton, {
                    type: 'primary',
                    size: 'small',
                    disabled: row.disRecover,
                    secondary: true,
                    onClick: () => recoverGoal(row)
                }, {
                    // ✅ 关键修复 3：图标渲染标准化
                    icon: () => h(NIcon, null, () => h(RestoreIcon)),
                    default: () => '恢复'
                })
            }),
            // 删除按钮（带 Tooltip）
            h(NTooltip, { trigger: 'hover' }, {
                default: () => h('span', row.disRecover?'超过30天无法删除':'永久删除此目标'),
                trigger: () => h(NButton, {
                    type: 'error',
                    size: 'small',
                    secondary: true,
                    disabled: row.disRecover,
                    onClick: () => {
                        message.success(`已删除目标: ${row.title}`);
                        recycle();
                    }
                }, {
                    icon: () => h(NIcon, null, () => h(TrashIcon)),
                    default: () => '删除'
                })
            })
        ]);
    }
}
]



const data = ref([]);

const pagination = reactive({
    page: 1,
    pageSize: 10,
    showSizePicker: true,
    pageSizes: [10, 20, 30],
    onChange: (page) => {
        pagination.page = page;
    },
    onPageSizeChange: (pageSize) => {
        pagination.pageSize = pageSize;
        pagination.page = 1;
    }
});
</script>

<style scoped>
.recycle-page {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
}

.main-content {
    flex: 1;
    padding: 20px 0;
}

.content-wrapper {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
}

.page-header {
    margin-bottom: 24px;
}

.hero-title,
.hero-title-light {
    font-size: 28px;
    font-weight: 600;
    margin: 0;
    background: linear-gradient(to right, #8a2be2, #4b0082);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.hero-subtitle,
.hero-subtitle-light {
    font-size: 16px;
    margin: 8px 0 0 0;
    opacity: 0.8;
}

.content-card,
.content-card-light {
    border-radius: 12px;
    overflow: hidden;
}

.empty-state,
.empty-state-light {
    padding: 40px 0;
}

.footer {
    text-align: center;
    padding: 20px;
    border-top: 1px solid rgba(128, 128, 128, 0.2);
}

.footer p {
    margin: 0;
    color: #666;
    font-size: 14px;
}

/* 深色主题适配 */
.content-card {
    background-color: rgba(30, 30, 40, 0.6);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.1);
}

.content-card-light {
    background-color: rgba(255, 255, 255, 0.8);
    border: 1px solid rgba(0, 0, 0, 0.1);
}

.recycle-content {
    margin-bottom: 24px;
}
</style>