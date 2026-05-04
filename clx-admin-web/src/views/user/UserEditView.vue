<template>
  <div class="user-edit">
    <el-card>
      <template #header>
        <span>用户详情</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户ID">{{ user?.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ user?.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ user?.nickname }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ user?.email }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ user?.gender || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="user?.status === 0 ? 'success' : 'danger'">
            {{ user?.status === 0 ? '正常' : '封禁' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="关注数">{{ user?.followCount }}</el-descriptions-item>
        <el-descriptions-item label="粉丝数">{{ user?.fansCount }}</el-descriptions-item>
        <el-descriptions-item label="签名" :span="2">{{ user?.signature || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userAdminApi, type UserDetailVO } from '@/api/user'

const route = useRoute()
const user = ref<UserDetailVO>()

async function loadUser() {
  const userId = Number(route.params.id)
  try {
    user.value = await userAdminApi.get(userId)
  } catch {
    ElMessage.error('加载用户详情失败')
  }
}

onMounted(() => {
  loadUser()
})
</script>

<style scoped>
.user-edit {
  display: grid;
  gap: 16px;
}
</style>