<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="24" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon dau">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.dau }}</div>
            <div class="stat-label">今日活跃用户</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon mau">
            <el-icon><Users /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.mau }}</div>
            <div class="stat-label">月活跃用户</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon new-users">
            <el-icon><Plus /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.newUsers }}</div>
            <div class="stat-label">新增用户</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon new-posts">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.newPosts }}</div>
            <div class="stat-label">新增帖子</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图 -->
    <el-card class="trend-card">
      <template #header>
        <div class="card-header">
          <span>数据趋势（近 7 天）</span>
          <el-radio-group v-model="trendType" size="small">
            <el-radio-button label="dau">活跃用户</el-radio-button>
            <el-radio-button label="posts">新增帖子</el-radio-button>
            <el-radio-button label="comments">新增评论</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="chartRef" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
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
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: trend.value.dates
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: nameMap[trendType.value],
        type: 'line',
        smooth: true,
        data: dataMap[trendType.value],
        areaStyle: { opacity: 0.3 },
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

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-size: 24px;
  margin-right: 16px;
}

.stat-icon.dau { background: #e8f5e9; color: #4caf50; }
.stat-icon.mau { background: #e3f2fd; color: #2196f3; }
.stat-icon.new-users { background: #fff3e0; color: #ff9800; }
.stat-icon.new-posts { background: #f3e5f5; color: #9c27b0; }

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}

.trend-card {
  min-height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 320px;
}
</style>