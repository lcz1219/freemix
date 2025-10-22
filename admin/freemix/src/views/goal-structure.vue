<template>
  <n-layout :native-scrollbar="false">
   
    <n-layout has-sider>
      <n-layout-sider bordered collapse-mode="transform"
 content-style="padding: 25px; " show-trigger="arrow-circle" :collapsed-width="0" :width="200" :collapsed-icon-size="22">
        <n-menu accordion :inverted="inverted"  :options="menuOptions" />
      </n-layout-sider>
      <n-layout-content class="main-content" >
        <div class="structure-container" ref="containerRef">
          <!-- 工具栏 -->
          <div class="toolbar" v-if="isShow">
            <n-button @click="addGoalNode" type="primary">
              <template #icon>
                <n-icon>
                  <AddIcon />
                </n-icon>
              </template>
              添加目标
            </n-button>
            <n-button @click="saveStructure" type="success">
              <template #icon>
                <n-icon>
                  <SaveIcon />
                </n-icon>
              </template>
              保存结构
            </n-button>
            <n-button @click="clearAll" type="error" v-if="isGoalOwner(currGoal)" >
              清空画布
            </n-button>
            <n-button id="download" @click="downloadImg" type="info">
              导出
            </n-button>
          </div>

          <!-- LogicFlow 画布容器 -->
          <div id="lf-container" ref="lfContainer" style="width: 100%; height: 100%;">


            <div id="mini-map-container" ref="minicontainer"></div>
          </div>

        </div>
      </n-layout-content>
    </n-layout>
  </n-layout>

  <!-- 编辑节点模态框 -->
  <n-modal v-model:show="showEditModal" preset="card" style="width: 600px;" :title="editingNode ? '编辑目标' : '添加目标'">
    <n-form :model="editForm" label-placement="left" label-width="80">
      <n-form-item label="标题" required>
        <n-input v-model:value="editForm.title" placeholder="请输入目标标题" />
      </n-form-item>
      <n-form-item label="描述" v-if="editForm.id == rootnode.id">
        <n-input v-model:value="editForm.description" type="textarea" placeholder="请输入目标描述" />
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button @click="showEditModal = false">取消</n-button>
        <n-button @click="saveNode" type="primary">保存</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup>
import { ref, onMounted, nextTick, watch, h } from 'vue';
import LogicFlow from '@logicflow/core';
import { RectNodeModel, RectNode } from '@logicflow/core';
import { Menu, Snapshot, DndPanel, Control, MiniMap } from '@logicflow/extension';

// 修复样式导入路径
import '@logicflow/core/dist/style/index.css';
import '@logicflow/extension/lib/style/index.css';
import { baseURL, isSuccess, postM, getMPaths, isGoalOwner } from '@/utils/request.js'

import {
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NButton,
  NModal,
  NForm,
  NFormItem,
  NLayoutSider,
  NMenu,
  NInput,
  NIcon,
  NSpace,
  useMessage
} from 'naive-ui';
import {
  Add as AddIcon,
  Save as SaveIcon,
  Document
} from '@vicons/ionicons5';
import NavBar from '../components/NavBar.vue';
import { useStore } from 'vuex';
import { download } from 'naive-ui/es/_utils';
import { style } from '@logicflow/extension/es/bpmn-elements/presets/icons';

const store = useStore();
const message = useMessage();
const lfContainer = ref(null);
const minicontainer = ref(null);
let lf = null;

// 菜单选项
const menuOptions = ref([]);

// 编辑相关
const showEditModal = ref(false);
const editingNode = ref(null);
const editForm = ref({
  title: '',
  description: ''
});
const downloadImg = () => {
  document.getElementById("download").addEventListener("click", () => {
    lf.getSnapshot();
    // 或者 1.1.13版本
    // lf.extension.snapshot.getSnapshot()
  });
}

// 自定义目标节点
class GoalNodeModel extends RectNodeModel {
  // 自定义节点属性
  getNodeStyle() {
    const style = super.getNodeStyle();

    // 获取节点完成状态
    let strokeColor = '#e88080'; // 默认颜色

    // 从节点属性中获取完成状态
    if (this.properties && this.properties.finish !== undefined) {
      strokeColor = this.properties.finish ? 'green' : '#e88080';
    }

    return {
      ...style,
      fill: '#f0f5ff',
      stroke: strokeColor,
      strokeWidth: 2,
      radius: 8,
    };
  }

  // 自定义文本样式
  getTextStyle() {
    const style = super.getTextStyle();
    return {
      ...style,
      fontWeight: 'bold',
      fontSize: 16,
    };
  }
}

// 暗黑主题状态
const isDark = ref(false);
// 初始化LogicFlow
const initLogicFlow = () => {
  lf = new LogicFlow({
    container: lfContainer.value,
    grid: {
      visible: true,
      config: {
        // color: "black",
        thickness: 1,
      },
    },
    keyboard: { enabled: true },
    background: {
      // backgroundColor:'black'
    },


    // 修复plugins配置，正确使用插件类而不是实例
    plugins: [Menu, Snapshot, DndPanel, Control, MiniMap],
    miniMap: {
      container: minicontainer.value,
      width: 200,// 小地
      height: 200// 图容器（必须存在）

    },
    // 确保样式正确应用
    style: {
      rect: {
        fill: '#f0f5ff',
        stroke: '#81c683',
        strokeWidth: 2,
        radius: 8,
      },
      circle: {
        fill: '#f0f5ff',
        // stroke: '#81c683',
        strokeWidth: 2,
      },
      polyline: {
        // stroke: '#81c683',
        strokeWidth: 2,
        // 添加 lineCap 和 lineJoin 以确保线条端点和连接处可见
        lineCap: 'round',
        lineJoin: 'round',
      },
      anchor: {
        // stroke: '#81c683',
        fill: '#ffffff',
        r: 4,
      },
      nodeText: {
        fontSize: 14,
        color: '#333',
      },
    },

    // 添加 theme 配置，使用 LogicFlow 推荐的主题切换方式
    // theme: isDark.value ? 'dark' : 'light'
  });

  lf.extension.menu.addMenuConfig({

    nodeMenu: [
      {
        text: "分享",
        callback() {
          alert("分享成功！");
        },
      },
      {
        text: "属性",
        callback(node) {
          alert(`
          节点id：${node.id}
          节点类型：${node.type}
          节点坐标：(x: ${node.x}, y: ${node.y})`);
        },
      },
    ],
    edgeMenu: [
      {
        text: "属性",
        className: "lf-menu-item",
        callback(edge) {
          alert(`
          边id：${edge.id}
          边类型：${edge.type}
          边坐标：(x: ${edge.x}, y: ${edge.y})
          源节点id：${edge.sourceNodeId}
          目标节点id：${edge.targetNodeId}`);
        },
      },
    ],
    graphMenu: [
      {
        text: "分享",
        callback() {
          alert("分享成功！");
        },
      },
    ],
  });



  // 注册自定义节点
  lf.register({
    type: 'goalNode',
    model: GoalNodeModel,
    view: RectNode // 传入正确的视图类
  });

  // 绑定事件
  lf.on('node:click', (event) => {
    editNode(event.data);
  });
  // 修改事件监听器，确保正确获取 edge 对象
  lf.on('edge:add', (event) => {
    // 检查 event 对象的结构
    console.log('edge:add 事件参数:', event);

    // 根据实际结构获取 edge 对象
    const edge = event.data || event.edge || event;

    if (!edge) {
      console.error('无法获取 edge 对象', event);
      return;
    }
    if (edge.sourceNodeId != rootnode.value.id) {
      message.warning("只支持根结点开始连接😁")
      lf.deleteEdge(edge.id);
      return
    }

    console.log('连接创建:', edge);

    // 添加到连接数组
    connections.value.push({
      id: edge.id,
      source: edge.sourceNodeId,
      target: edge.targetNodeId
    });
  });

  lf.on('node:delete', ({ data }) => {
    deleteNode(data.id);
  });
  // lf.extension.miniMap.show(100, 100)
  // 渲染初始数据
  renderData();
};
const isShow=ref(true);

// 渲染数据
const renderData = () => {
  const data = {
    nodes: nodes.value.map(node => ({
      id: String(node.id),
      type: 'goalNode',
      x: node.x,
      y: node.y,
      text: node.title,
      properties: {
        description: node.description,
        finish: node.finish
      }
    })),
    edges: connections.value.map(conn => ({
      id: String(conn.id),
      type: 'polyline',
      sourceNodeId: String(conn.source),
      targetNodeId: String(conn.target),
      // text: '父子关系'

    }))
  };
  console.log("data", data);


  if (lf) {
    lf.render(data);
  }
};

// 节点数据
const nodes = ref([]);
const connections = ref([]);

onMounted(async () => {
  await getGoals();
  nextTick(initLogicFlow);
  loadGoalStructure(goals.value[0])
});
const goals = ref([])
const renderLabel = (goal) => {
  console.log("goal",goal);
  
  let count = 0;
  let total = goal.childGoals.length;
  if (!goal.finish) {
    goal.childGoals.forEach(childGoal => {
      if (childGoal.finish) {
        count++;
      }
    });

  }
  
  return h('div', { style: 'display: flex; flex-direction: column; width: 100%;' }, [
    h('div', { style: 'font-weight: bold; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 200px;' }, goal.title),
    h('div', { style: 'font-size: 12px;' }, goal.finish ?
      h('span', { style: 'color:#5acea7' }, '(已完成)') : goal.status=='expired'?
      h('span', { style: 'color:#e98b8b' }, `(未完成 ${count}/${total})`):
      h('span', { style: 'color:#e6c260' }, `(未完成 ${count}/${total})`))
  ])

}

const currGoal = ref({})
// 获取目标列表
const getGoals = async () => {
  console.log("getGoals");

  try {
    const res = await getMPaths("getGoals", store.state.user.username, "正在获取目标数据...");
    console.log("res", res);
    if (isSuccess(res)) {
      console.log("res", res);
      goals.value = res.data.data;
      menuOptions.value = res.data.data.map(goal => ({
        key: goal._id,
        label: () => renderLabel(goal),
        onClick: () => loadGoalStructure(goal)
      }));
      console.log("menuOptions", menuOptions.value);

    }
  } catch (error) {
    console.log("error", error);

    message.error('获取目标列表失败');
  }
};
const rootnode = ref()
// 加载目标结构
const loadGoalStructure = (goal) => {
  console.log("loadGoalStructure", goal);
    currGoal.value = goal
  nodes.value = [{
    id: goal._id,
    title: goal.title,
    description: goal.description,
    finish: goal.finish,
    x: 300,
    y: 100
  }];
  rootnode.value = nodes.value[0]

  connections.value = [];
  circleloadGoalStructure(goal)

  renderData();
  if(goal.status=='expired'){
    isShow.value=false
  }else{
      isShow.value=true
  }
};
const circleloadGoalStructure = (goal) => {
  goal.childGoals.forEach((child, index) => {
    nodes.value.push({
      id: child._id,
      title: child.message,
      description: child.message,
      finish: child.finish,
      x: 150 +index*70,
      y: 250+ index *90
    });

    connections.value.push({
      id: `conn-${goal._id}-${child._id}`,
      source: goal._id,
      target: child._id
    });
    if (child.childGoals) {
      circleloadGoalStructure(child);
    }
  });
};

// 添加目标节点
const addGoalNode = () => {
  editingNode.value = null;
  editForm.value = {
    title: '',
    description: ''
  };
  // showEditModal.value = true;
  saveNode()
};

// 编辑节点
const editNode = (node) => {

  editingNode.value = node;
  editForm.value = {
    id: node.id,
    title: node.text ? node.text.value : '',
    description: node.properties.description
  };
  showEditModal.value = true;
};

// 保存节点
const saveNode = () => {
  console.log("saveNode");

  // if (!editForm.value.title) {
  //   message.warning('请输入目标标题');
  //   return;
  // }

  if (editingNode.value) {
    // 更新现有节点
    editingNode.value.title = editForm.value.title;
    editingNode.value.description = editForm.value.description;

    // 更新LogicFlow中的节点
    lf.updateText({
      id: editingNode.value.id,
      properties: {
        text: editForm.value.title,
        description: editForm.value.description
      },
      value: editForm.value.title
    });
    nodes.value.forEach(e => {
      if (e.id == editingNode.value.id) {
        console.log("改变e", e.title, editForm.value);

        e.title = editingNode.value.title
        e.description = editingNode.value.description
      }
    })
    renderData()
  } else {
    // 添加新节点
    const newNode = {
      id: `node-${Date.now()}`,
      title: editForm.value.title,
      description: editForm.value.description,
      finish: false, // 新节点默认未完成
      x: 350,
      y: 100
    };

    nodes.value.push(newNode);

    lf.addNode({
      id: newNode.id,
      type: 'goalNode',
      x: newNode.x,
      y: newNode.y,
      text: newNode.title,
      properties: {
        description: newNode.description,
        finish: newNode.finish
      }
    });
  }


  showEditModal.value = false;
  message.success('保存成功');
};

// 删除节点
const deleteNode = (id) => {
  // 删除节点
  nodes.value = nodes.value.filter(node => node.id !== id);

  // 删除相关连接
  connections.value = connections.value.filter(
    conn => conn.source !== id && conn.target !== id
  );

  // 从画布中删除
  lf.deleteNode(id);
};

// 清空画布
const clearAll = () => {
  nodes.value = [];
  connections.value = [];
  lf.clearData();
};

// 保存结构
const saveStructure = async () => {
  const structure = {
    nodes: nodes.value,
    connections: connections.value
  };
  const res = await postM("saveGoalStructure", structure);


  message.success('结构保存成功');
  getGoals()
};

// 切换主题
const toggleTheme = () => {
  isDark.value = !isDark.value;

  // 更新 LogicFlow 实例的主题
  if (lf) {
    // lf.setTheme(isDark.value ? 'dark' : 'light');
  }

  // 同时更新页面的 class，保持与 LogicFlow 主题一致
  // document.body.className = isDark.value ? 'dark-theme' : '';
};

</script>

<style scoped>
.structure-container {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.toolbar {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 10;
  display: flex;
  gap: 10px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.339);
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

#lf-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.edge-menu-item {
  background-color: #2d2d2d;
}

/* 暗黑主题样式 */
/* .dark-theme {
  background-color: #1a1a1a;
  color: #e0e0e0;
}

.dark-theme .toolbar {
  background: #2d2d2d;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.dark-theme .n-button {
  background: #333;
  border-color: #444;
  color: #e0e0e0;
} */
/* 设置整个菜单背景为黑色 */
:deep .lf-menu {
  background-color: #000 !important;
}

:deep .lf-control {
  color: #000;
}

/* 设置菜单项文字颜色为白色 */
:deep .lf-menu li {
  color: #ffffff !important;
  background-color: #000 !important;
}

/* 优化菜单项宽度，确保长标题有足够空间显示 */
:deep .n-menu-item-content {
  min-width: 220px !important;
  max-width: 300px !important;
  padding: 8px 12px !important;
}

/* 优化菜单项内容布局 */
:deep .n-menu-item-content__label {
  width: 100% !important;
  overflow: hidden !important;
}

/* 设置菜单项悬停效果 */
:deep .lf-menu li:hover {
  background-color: #333 !important;
}

/* .dark-theme .n-button:hover {
  background: #444;
}

.dark-theme .n-input {
  background: #2d2d2d;
  border-color: #444;
  color: #e0e0e0;
}

.dark-theme .n-input:focus {
  border-color: #81c683;
}

.dark-theme .n-form-item-label {
  color: #e0e0e0;
} */
</style>