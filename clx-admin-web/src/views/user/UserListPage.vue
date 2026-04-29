<template>
  <div class="user-list-page">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="正常" value="0" />
            <el-option label="封禁" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'">
              {{ row.status === '0' ? '正常' : '封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="roles" label="角色" width="120">
          <template #default="{ row }">
            <el-tag v-for="role in row.roles" :key="role" size="small" class="role-tag">
              {{ role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === '0' ? 'danger' : 'success'"
              @click="handleToggleBan(row)"
            >
              {{ row.status === '0' ? '封禁' : '解封' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
        class="pagination"
      />
    </el-card>

    <!-- 编辑弹窗 -->
    <UserEditDialog
      v-model:visible="editDialogVisible"
      :user="currentUser"
      @success="fetchData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '../../api'
import type { UserPageVO } from '../../api/types'
import UserEditDialog from './UserEditDialog.vue'

const loading = ref(false)
const tableData = ref<UserPageVO[]>([])
const editDialogVisible = ref(false)
const currentUser = ref<UserPageVO | null>(null)

const searchForm = reactive({
  username: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await userApi.getPage({
      page: pagination.current,
      size: pagination.size,
      username: searchForm.username || undefined,
      status: searchForm.status || undefined
    })
    if (res.data.code === 200) {
      tableData.value = res.data.data.records
      pagination.total = res.data.data.total
    }
  } catch (e) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  fetchData()
}

function handleReset() {
  searchForm.username = ''
  searchForm.status = ''
  pagination.current = 1
  fetchData()
}

function handleEdit(user: UserPageVO) {
  currentUser.value = { ...user }
  editDialogVisible.value = true
}

async function handleToggleBan(user: UserPageVO) {
  const isBan = user.status === '0'
  const action = isBan ? '封禁' : '解封'

  try {
    await ElMessageBox.confirm(`确定要${action}用户 "${user.username}" 吗？`, '提示', {
      type: 'warning'
    })

    if (isBan) {
      await userApi.ban(user.userId)
    } else {
      await userApi.unban(user.userId)
    }

    ElMessage.success(`${action}成功`)
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.msg || `${action}失败`)
    }
  }
}
</script>

<style scoped lang="scss">
.user-list-page {
  .search-card {
    margin-bottom: 16px;
  }

  .role-tag {
    margin-right: 4px;
  }

  .pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }
}
</style>
