<template>
  <div class="user-list">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="搜索用户名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="正常" :value="0" />
            <el-option label="封禁" :value="1" />
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

    <!-- 用户表格 -->
    <el-card class="table-card">
      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="roleNames" label="角色" width="120">
          <template #default="{ row }">
            <el-tag v-for="role in row.roleNames" :key="role" size="small" type="success">
              {{ role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small" class="status-tag">
              {{ row.status === 0 ? '正常' : '封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="danger"
              size="small"
              link
              @click="handleBan(row)"
            >
              封禁
            </el-button>
            <el-button
              v-else
              type="success"
              size="small"
              link
              @click="handleUnban(row)"
            >
              解封
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
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑用户" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.roleIds" multiple placeholder="选择角色">
            <el-option
              v-for="role in roles"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userAdminApi, type UserAdminVO, type UserPageRequest } from '@/api/user'
import { roleAdminApi, type RoleVO } from '@/api/role'

const loading = ref(false)
const users = ref<UserAdminVO[]>([])
const total = ref(0)
const pageNo = ref(1)
const pageSize = ref(20)

const roles = ref<RoleVO[]>([])

const searchForm = reactive({
  username: '',
  status: undefined as number | undefined
})

const editVisible = ref(false)
const editForm = reactive({
  id: 0,
  nickname: '',
  roleIds: [] as number[]
})

async function loadUsers() {
  loading.value = true
  try {
    const params: UserPageRequest = {
      username: searchForm.username || undefined,
      status: searchForm.status,
      pageNo: pageNo.value,
      pageSize: pageSize.value
    }
    const res = await userAdminApi.page(params)
    users.value = res.list
    total.value = res.total
  } catch {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

async function loadRoles() {
  try {
    roles.value = await roleAdminApi.list()
  } catch {
    console.error('加载角色列表失败')
  }
}

function handleSearch() {
  pageNo.value = 1
  loadUsers()
}

function handleReset() {
  searchForm.username = ''
  searchForm.status = undefined
  pageNo.value = 1
  loadUsers()
}

function handleEdit(row: UserAdminVO) {
  editForm.id = row.id
  editForm.nickname = row.nickname || ''
  editForm.roleIds = row.roleIds || []
  editVisible.value = true
}

async function handleSaveEdit() {
  try {
    await userAdminApi.update(editForm.id, { nickname: editForm.nickname })
    await userAdminApi.updateRoles(editForm.id, editForm.roleIds)
    ElMessage.success('保存成功')
    editVisible.value = false
    loadUsers()
  } catch {
    ElMessage.error('保存失败')
  }
}

async function handleBan(row: UserAdminVO) {
  await ElMessageBox.confirm(`确认封禁用户 ${row.username}？`, '警告', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  })
  try {
    await userAdminApi.ban(row.id)
    ElMessage.success('封禁成功')
    loadUsers()
  } catch {
    ElMessage.error('封禁失败')
  }
}

async function handleUnban(row: UserAdminVO) {
  try {
    await userAdminApi.unban(row.id)
    ElMessage.success('解封成功')
    loadUsers()
  } catch {
    ElMessage.error('解封失败')
  }
}

onMounted(() => {
  loadUsers()
  loadRoles()
})
</script>

<style scoped>
.user-list {
  display: grid;
  gap: 16px;
}

.search-card {
  padding: 16px;
}

.table-card {
  padding: 0;
}

.pagination {
  padding: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>