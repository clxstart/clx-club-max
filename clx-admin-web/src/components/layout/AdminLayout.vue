<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="sidebar">
      <div class="sidebar-header">
        <div class="logo">CLX</div>
        <span class="title">后台管理</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/post">
          <el-icon><Document /></el-icon>
          <span>帖子管理</span>
        </el-menu-item>
        <el-menu-item index="/comment">
          <el-icon><ChatDotRound /></el-icon>
          <span>评论管理</span>
        </el-menu-item>
        <el-menu-item index="/role">
          <el-icon><Key /></el-icon>
          <span>角色权限</span>
        </el-menu-item>
        <el-menu-item index="/log">
          <el-icon><List /></el-icon>
          <span>操作日志</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-left">
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="32" :src="user?.avatar">
                {{ user?.nickname?.charAt(0) || user?.username?.charAt(0) || 'A' }}
              </el-avatar>
              <span class="username">{{ user?.nickname || user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const user = computed(() => authStore.user)
const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta.title || '')

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100%;
}

.sidebar {
  background: #001529;
  overflow: hidden;
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  background: #002140;
}

.logo {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #6c63ff, #4d46d6);
  color: #fff;
  display: grid;
  place-items: center;
  font-weight: 800;
  font-size: 14px;
}

.title {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  margin-left: 12px;
}

.sidebar-menu {
  background: transparent;
  border: none;
}

.sidebar-menu .el-menu-item {
  color: #a6adb4;
}

.sidebar-menu .el-menu-item:hover {
  background: #000c17;
  color: #fff;
}

.sidebar-menu .el-menu-item.is-active {
  background: #6c63ff;
  color: #fff;
}

.header {
  height: 64px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
}

.main {
  background: #f5f7fa;
  padding: 24px;
}
</style>