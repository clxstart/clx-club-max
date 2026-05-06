<template>
  <div class="shell-with-nav">
    <!-- 左侧导航 -->
    <nav class="left-nav">
      <div class="nav-brand">
        <div class="nav-logo">CLX</div>
        <span class="nav-title">后台管理</span>
      </div>
      <div class="nav-items">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: activeMenu === item.path }"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span class="nav-label">{{ item.label }}</span>
        </router-link>
        <button class="nav-item logout" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span class="nav-label">退出</span>
        </button>
      </div>
    </nav>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, markRaw } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { DataAnalysis, User, Document, ChatDotRound, Key, List, SwitchButton } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeMenu = computed(() => route.path)

const menuItems = [
  { path: '/dashboard', label: '数据概览', icon: markRaw(DataAnalysis) },
  { path: '/user', label: '用户管理', icon: markRaw(User) },
  { path: '/post', label: '帖子管理', icon: markRaw(Document) },
  { path: '/comment', label: '评论管理', icon: markRaw(ChatDotRound) },
  { path: '/role', label: '角色权限', icon: markRaw(Key) },
  { path: '/log', label: '操作日志', icon: markRaw(List) }
]

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
/* 复用前台左侧导航样式 */
.shell-with-nav {
  margin-left: 208px;
  padding: 28px;
  min-height: 100vh;
  background: var(--clx-bg);
}

.left-nav {
  position: fixed;
  left: 28px;
  top: 28px;
  width: 180px;
  height: calc(100vh - 56px);
  display: flex;
  flex-direction: column;
  padding: 20px 0;
  border-radius: 24px;
  background: var(--clx-surface);
  box-shadow: var(--clx-shadow);
  z-index: 100;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 16px 20px;
  border-bottom: 1px solid rgba(0,0,0,0.06);
  margin-bottom: 16px;
}

.nav-logo {
  width: 40px;
  height: 40px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: var(--clx-surface);
  box-shadow: var(--clx-shadow-soft);
  color: var(--clx-accent);
  font-weight: 800;
  font-size: 14px;
}

.nav-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--clx-text);
}

.nav-items {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  padding: 0 12px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border: 0;
  border-radius: 14px;
  background: transparent;
  color: var(--clx-muted);
  cursor: pointer;
  transition: all .18s ease;
  text-align: left;
  width: 100%;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
}

.nav-item:hover {
  color: var(--clx-text);
  background: rgba(108, 99, 255, 0.06);
}

.nav-item.active {
  color: var(--clx-accent);
  background: rgba(108, 99, 255, 0.1);
  box-shadow: var(--clx-inset);
}

.nav-item.logout {
  margin-top: auto;
  border-top: 1px solid rgba(0,0,0,0.06);
  margin-left: 12px;
  margin-right: 12px;
  padding-top: 16px;
  margin-bottom: 8px;
}

.nav-item.logout:hover {
  color: var(--clx-danger);
}

.nav-label {
  font-size: 14px;
  font-weight: 500;
}

.main-content {
  min-height: calc(100vh - 56px);
}

/* 移动端适配 */
@media (max-width: 960px) {
  .left-nav {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    top: auto;
    width: 100%;
    height: auto;
    flex-direction: row;
    padding: 8px 16px;
    border-radius: 24px 24px 0 0;
    justify-content: space-around;
  }

  .nav-brand,
  .nav-title,
  .nav-label {
    display: none;
  }

  .nav-items {
    flex-direction: row;
    justify-content: space-around;
    padding: 0;
    gap: 0;
  }

  .nav-item {
    padding: 10px 14px;
    border-radius: 12px;
  }

  .nav-item.logout {
    display: none;
  }

  .shell-with-nav {
    margin-left: 0;
    padding: 16px;
    padding-bottom: 80px;
  }
}
</style>