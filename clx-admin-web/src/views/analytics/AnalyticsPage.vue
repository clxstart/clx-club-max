<template>
  <div class="analytics-page">
    <div class="page-header">
      <h2>数据报表</h2>
      <el-date-picker
        v-model="selectedDate"
        type="date"
        placeholder="选择日期"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        @change="loadData"
      />
    </div>

    <!-- 核心指标卡片 -->
    <div class="metric-cards">
      <el-card class="metric-card">
        <div class="metric-value">{{ dailyReport?.dau || 0 }}</div>
        <div class="metric-label">日活用户</div>
      </el-card>
      <el-card class="metric-card">
        <div class="metric-value">{{ dailyReport?.wau || 0 }}</div>
        <div class="metric-label">周活用户</div>
      </el-card>
      <el-card class="metric-card">
        <div class="metric-value">{{ dailyReport?.mau || 0 }}</div>
        <div class="metric-label">月活用户</div>
      </el-card>
      <el-card class="metric-card">
        <div class="metric-value">{{ dailyReport?.newUsers || 0 }}</div>
        <div class="metric-label">新增用户</div>
      </el-card>
    </div>

    <!-- 用户活跃趋势 -->
    <el-card class="chart-card">
      <template #header>
        <span>用户活跃趋势</span>
      </template>
      <div ref="trendChartRef" class="chart-container"></div>
    </el-card>

    <!-- 热门帖子 -->
    <el-card class="hot-posts-card">
      <template #header>
        <span>热门帖子 Top10</span>
      </template>
      <el-table :data="hotPosts" stripe>
        <el-table-column prop="postId" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="viewCount" label="浏览" width="100" />
        <el-table-column prop="likeCount" label="点赞" width="100" />
        <el-table-column prop="commentCount" label="评论" width="100" />
      </el-table>
    </el-card>

    <!-- 用户留存率 -->
    <el-card class="retention-card">
      <template #header>
        <span>用户留存率</span>
      </template>
      <div class="retention-bars">
        <div class="retention-item">
          <span class="label">次日留存</span>
          <el-progress
            :percentage="(dailyReport?.retention1d || 0) * 100"
            :format="() => ((dailyReport?.retention1d || 0) * 100).toFixed(1) + '%'"
          />
        </div>
        <div class="retention-item">
          <span class="label">7日留存</span>
          <el-progress
            :percentage="(dailyReport?.retention7d || 0) * 100"
            :format="() => ((dailyReport?.retention7d || 0) * 100).toFixed(1) + '%'"
          />
        </div>
        <div class="retention-item">
          <span class="label">30日留存</span>
          <el-progress
            :percentage="(dailyReport?.retention30d || 0) * 100"
            :format="() => ((dailyReport?.retention30d || 0) * 100).toFixed(1) + '%'"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { analyticsApi, type DailyReport, type HotPost, type TrendData } from '../../api/analytics'

const selectedDate = ref(formatDate(new Date()))
const dailyReport = ref<DailyReport | null>(null)
const hotPosts = ref<HotPost[]>([])
const trendData = ref<TrendData | null>(null)
const trendChartRef = ref<HTMLElement | null>(null)

/** 格式化日期 */
function formatDate(date: Date): string {
  return date.toISOString().split('T')[0]
}

/** 加载数据 */
async function loadData() {
  try {
    // 并行加载报表数据
    const [dailyRes, hotRes, trendRes] = await Promise.all([
      analyticsApi.getDaily(selectedDate.value),
      analyticsApi.getHotPosts(selectedDate.value),
      analyticsApi.getTrend(
        formatDate(new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)),
        selectedDate.value
      )
    ])

    dailyReport.value = dailyRes.data.data
    hotPosts.value = hotRes.data.data
    trendData.value = trendRes.data.data

    // 渲染趋势图
    await nextTick()
    renderTrendChart()
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

/** 渲染趋势图 */
function renderTrendChart() {
  if (!trendChartRef.value || !trendData.value) return

  const chart = echarts.init(trendChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: trendData.value.dates,
      axisLabel: { rotate: 45 }
    },
    yAxis: { type: 'value' },
    series: [{
      name: '日活用户',
      type: 'line',
      smooth: true,
      data: trendData.value.values,
      areaStyle: { opacity: 0.3 }
    }]
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.analytics-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}

.metric-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.metric-card {
  text-align: center;
}

.metric-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}

.metric-label {
  color: #909399;
  margin-top: 8px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
}

.hot-posts-card {
  margin-bottom: 20px;
}

.retention-card {
  margin-bottom: 20px;
}

.retention-bars {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.retention-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.retention-item .label {
  width: 80px;
  color: #606266;
}

.retention-item .el-progress {
  flex: 1;
}
</style>
