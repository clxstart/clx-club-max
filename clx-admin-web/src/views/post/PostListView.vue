<template>
  <div class="post-list">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="标题">
          <el-input v-model="searchForm.title" placeholder="搜索标题" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="正常" :value="0" />
            <el-option label="隐藏" :value="1" />
            <el-option label="删除" :value="2" />
          </el-select>
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

    <!-- 帖子表格 -->
    <el-card class="table-card">
      <el-table :data="posts" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status].type" size="small">
              {{ statusMap[row.status].label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80" />
        <el-table-column prop="likeCount" label="点赞" width="80" />
        <el-table-column prop="commentCount" label="评论" width="80" />
        <el-table-column prop="createdAt" label="发布时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="warning"
              size="small"
              link
              @click="handleHide(row)"
            >
              隐藏
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="success"
              size="small"
              link
              @click="handleShow(row)"
            >
              显示
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNo"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadPosts"
          @current-change="loadPosts"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { postAdminApi, type PostAdminVO, type PostPageRequest } from '@/api/post'

const loading = ref(false)
const posts = ref<PostAdminVO[]>([])
const total = ref(0)
const pageNo = ref(1)
const pageSize = ref(20)

const statusMap: Record<number, { label: string; type: string }> = {
  0: { label: '正常', type: 'success' },
  1: { label: '隐藏', type: 'warning' },
  2: { label: '删除', type: 'danger' }
}

const searchForm = reactive({
  title: '',
  status: undefined as number | undefined
})

async function loadPosts() {
  loading.value = true
  try {
    const params: PostPageRequest = {
      title: searchForm.title || undefined,
      status: searchForm.status,
      pageNo: pageNo.value,
      pageSize: pageSize.value
    }
    const res = await postAdminApi.page(params)
    posts.value = res.list
    total.value = res.total
  } catch {
    ElMessage.error('加载帖子列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNo.value = 1
  loadPosts()
}

function handleReset() {
  searchForm.title = ''
  searchForm.status = undefined
  pageNo.value = 1
  loadPosts()
}

async function handleHide(row: PostAdminVO) {
  try {
    await postAdminApi.updateStatus(row.id, 1)
    ElMessage.success('隐藏成功')
    loadPosts()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleShow(row: PostAdminVO) {
  try {
    await postAdminApi.updateStatus(row.id, 0)
    ElMessage.success('显示成功')
    loadPosts()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(row: PostAdminVO) {
  await ElMessageBox.confirm(`确认删除帖子「${row.title}」？`, '警告', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  })
  try {
    await postAdminApi.remove(row.id)
    ElMessage.success('删除成功')
    loadPosts()
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.post-list {
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