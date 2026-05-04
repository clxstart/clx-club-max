<template>
  <div class="comment-list">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="内容">
          <el-input v-model="searchForm.content" placeholder="搜索评论内容" clearable />
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

    <!-- 评论表格 -->
    <el-card class="table-card">
      <el-table :data="comments" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="postTitle" label="帖子" width="150" show-overflow-tooltip />
        <el-table-column prop="likeCount" label="点赞" width="80" />
        <el-table-column prop="createdAt" label="发布时间" width="180" />
        <el-table-column label="操作" fixed="right" width="100">
          <template #default="{ row }">
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
          @size-change="loadComments"
          @current-change="loadComments"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { commentAdminApi, type CommentAdminVO, type CommentPageRequest } from '@/api/comment'

const loading = ref(false)
const comments = ref<CommentAdminVO[]>([])
const total = ref(0)
const pageNo = ref(1)
const pageSize = ref(20)

const searchForm = reactive({
  content: ''
})

async function loadComments() {
  loading.value = true
  try {
    const params: CommentPageRequest = {
      content: searchForm.content || undefined,
      pageNo: pageNo.value,
      pageSize: pageSize.value
    }
    const res = await commentAdminApi.page(params)
    comments.value = res.list
    total.value = res.total
  } catch {
    ElMessage.error('加载评论列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNo.value = 1
  loadComments()
}

function handleReset() {
  searchForm.content = ''
  pageNo.value = 1
  loadComments()
}

async function handleDelete(row: CommentAdminVO) {
  await ElMessageBox.confirm('确认删除此评论？', '警告', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  })
  try {
    await commentAdminApi.remove(row.id)
    ElMessage.success('删除成功')
    loadComments()
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadComments()
})
</script>

<style scoped>
.comment-list {
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