<template>
  <div class="role-list">
    <el-card>
      <el-table :data="roles" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" width="150" />
        <el-table-column prop="code" label="角色编码" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="权限" width="100">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEditPermissions(row)">
              配置权限
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 权限配置弹窗 -->
    <el-dialog v-model="permVisible" title="配置权限" width="500px">
      <el-tree
        ref="treeRef"
        :data="permissionTree"
        :props="{ label: 'name', children: 'children' }"
        show-checkbox
        node-key="id"
        default-expand-all
      />
      <template #footer>
        <el-button @click="permVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePermissions">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { ElTree } from 'element-plus'
import { roleAdminApi, type RoleVO, type PermissionVO } from '@/api/role'

const loading = ref(false)
const roles = ref<RoleVO[]>([])
const permissions = ref<PermissionVO[]>([])
const permVisible = ref(false)
const currentRole = ref<RoleVO | null>(null)
const treeRef = ref<InstanceType<typeof ElTree>>()

// 将权限列表转换为树结构
const permissionTree = computed(() => {
  const map = new Map<number, PermissionVO & { children?: PermissionVO[] }>()
  const roots: (PermissionVO & { children?: PermissionVO[] })[] = []

  // 先创建所有节点
  permissions.value.forEach(p => {
    map.set(p.id, { ...p, children: [] })
  })

  // 构建树
  permissions.value.forEach(p => {
    const node = map.get(p.id)!
    if (p.parentId && map.has(p.parentId)) {
      map.get(p.parentId)!.children!.push(node)
    } else {
      roots.push(node)
    }
  })

  return roots
})

async function loadRoles() {
  loading.value = true
  try {
    roles.value = await roleAdminApi.list()
  } catch {
    ElMessage.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

async function loadPermissions() {
  try {
    permissions.value = await roleAdminApi.permissions()
  } catch {
    console.error('加载权限列表失败')
  }
}

function handleEditPermissions(role: RoleVO) {
  currentRole.value = role
  permVisible.value = true

  // 设置当前选中的权限
  setTimeout(() => {
    treeRef.value?.setCheckedKeys(role.permissionIds || [])
  }, 100)
}

async function handleSavePermissions() {
  if (!currentRole.value) return

  const checkedKeys = treeRef.value?.getCheckedKeys() as number[]
  try {
    await roleAdminApi.updatePermissions(currentRole.value.id, checkedKeys)
    ElMessage.success('保存成功')
    permVisible.value = false
    loadRoles()
  } catch {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  loadRoles()
  loadPermissions()
})
</script>

<style scoped>
.role-list {
  display: grid;
  gap: 16px;
}
</style>