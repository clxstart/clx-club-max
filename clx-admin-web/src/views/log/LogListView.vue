<template>
  <div class="log-list">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="操作人">
          <el-input v-model="searchForm.operator" placeholder="搜索操作人" clearable />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-input v-model="searchForm.action" placeholder="搜索操作类型" clearable />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.timeRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志表格 -->
    <el-card class="table-card">
      <el-table :data="logs" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="action" label="操作类型" width="150" />
        <el-table-column prop="target" label="操作对象" min-width="150" show-overflow-tooltip />
        <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column prop="createdAt" label="操作时间" width="180" />
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNo"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadLogs"
          @current-change="loadLogs"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { logAdminApi, type OperLogVO, type LogPageRequest } from '@/api/log'

const loading = ref(false)
const logs = ref<OperLogVO[]>([])
const total = ref(0)
const pageNo = ref(1)
const pageSize = ref(20)

const searchForm = reactive({
  operator: '',
  action: '',
  timeRange: [] as string[]
})

async function loadLogs() {
  loading.value = true
  try {
    const params: LogPageRequest = {
      operator: searchForm.operator || undefined,
      action: searchForm.action || undefined,
      startTime: searchForm.timeRange[0] || undefined,
      endTime: searchForm.timeRange[1] || undefined,
      pageNo: pageNo.value,
      pageSize: pageSize.value
    }
    const res = await logAdminApi.page(params)
    logs.value = res.list
    total.value = res.total
  } catch {
    ElMessage.error('加载日志列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNo.value = 1
  loadLogs()
}

function handleReset() {
  searchForm.operator = ''
  searchForm.action = ''
  searchForm.timeRange = []
  pageNo.value = 1
  loadLogs()
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.log-list {
  display: grid;
  gap: 16px;
}

.search-card {
  padding: 16px;
}

.pagination {
  padding: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>