<template>
  <div class="dashboard app-shell">
    <!-- 统计卡片 -->
    <el-row :gutter="24" class="stat-cards">
      <el-col :span="6" v-for="(stat, index) in statList" :key="index">
        <div class="card neu-stat-card">
          <div class="neu-stat-icon" :class="stat.class">
            <el-icon :size="28"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="neu-stat-content">
            <div class="neu-stat-value">{{ stats[stat.key] }}</div>
            <div class="neu-stat-label muted">{{ stat.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 趋势图 -->
    <div class="card trend-card">
      <div class="card-header">
        <h3>数据趋势（近 7 天）</h3>
        <el-radio-group v-model="trendType" size="small">
          <el-radio-button label="dau">活跃用户</el-radio-button>
          <el-radio-button label="posts">新增帖子</el-radio-button>
          <el-radio-button label="comments">新增评论</el-radio-button>
        </el-radio-group>
      </div>
      <div ref="chartRef" class="chart-container"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, markRaw } from 'vue'
import { User, Users, Plus, Document } from '@element-plus/icons-vue'
import { statsAdminApi, type StatsOverview, type StatsTrend } from '@/api/stats'
import * as echarts from 'echarts'

const stats = ref<StatsOverview>({
  dau: 0,
  mau: 0,
  newUsers: 0,
  newPosts: 0,
  newComments: 0
})

const trend = ref<StatsTrend>({
  dates: [],
  dau: [],
  posts: [],
  comments: []
})

const statList = reactive([
  { key: 'dau', label: '今日活跃用户', icon: markRaw(User), class: 'dau' },
  { key: 'mau', label: '月活跃用户', icon: markRaw(Users), class: 'mau' },
  { key: 'newUsers', label: '新增用户', icon: markRaw(Plus), class: 'new-users' },
  { key: 'newPosts', label: '新增帖子', icon: markRaw(Document), class: 'new-posts' }
])

const trendType = ref<'dau' | 'posts' | 'comments'>('dau')
const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

async function loadData() {
  try {
    stats.value = await statsAdminApi.overview()
    trend.value = await statsAdminApi.trend(7)
    updateChart()
  } catch (err) {
    console.error('加载数据失败', err)
  }
}

function updateChart() {
  if (!chartRef.value) return

  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const dataMap = {
    dau: trend.value.dau,
    posts: trend.value.posts,
    comments: trend.value.comments
  }

  const nameMap = {
    dau: '活跃用户',
    posts: '新增帖子',
    comments: '新增评论'
  }

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'var(--clx-surface)',
      borderColor: '#b8bcc2',
      borderWidth: 1,
      textStyle: { color: 'var(--clx-text)' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: trend.value.dates,
      axisLine: { lineStyle: { color: '#b8bcc2' } },
      axisLabel: { color: 'var(--clx-muted)' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(0,0,0,0.05)' } },
      axisLabel: { color: 'var(--clx-muted)' }
    },
    series: [
      {
        name: nameMap[trendType.value],
        type: 'line',
        smooth: true,
        data: dataMap[trendType.value],
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(108, 99, 255, 0.3)' },
            { offset: 1, color: 'rgba(108, 99, 255, 0.05)' }
          ])
        },
        lineStyle: { color: '#6c63ff', width: 3 },
        itemStyle: { color: '#6c63ff' }
      }
    ]
  })
}

watch(trendType, updateChart)

onMounted(() => {
  loadData()
  window.addEventListener('resize', () => chart?.resize())
})
</script>

<style scoped>
.dashboard {
  display: grid;
  gap: 24px;
}

.stat-cards {
  margin-bottom: 0;
}

.neu-stat-card {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s ease;
}

.neu-stat-card:hover {
  transform: translateY(-4px);
}

.neu-stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  background: var(--clx-surface);
  box-shadow: 4px 4px 8px #b8bcc2, -4px -4px 8px #ffffff;
}

.neu-stat-icon.dau { color: #4caf50; }
.neu-stat-icon.mau { color: #2196f3; }
.neu-stat-icon.new-users { color: #ff9800; }
.neu-stat-icon.new-posts { color: #9c27b0; }

.neu-stat-content {
  flex: 1;
}

.neu-stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--clx-text);
}

.neu-stat-label {
  font-size: 14px;
  margin-top: 4px;
}

.trend-card {
  min-height: 420px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
}

.card-header :deep(.el-radio-button__inner) {
  background: var(--clx-surface);
  border: none;
  box-shadow: 2px 2px 4px #b8bcc2, -2px -2px 4px #ffffff;
  color: var(--clx-muted);
  padding: 8px 16px;
  border-radius: 999px;
}

.card-header :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: var(--clx-accent);
  color: #fff;
  box-shadow: var(--clx-inset);
}

.chart-container {
  height: 320px;
}
</style>