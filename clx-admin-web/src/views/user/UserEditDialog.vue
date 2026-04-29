<template>
  <el-dialog
    :model-value="visible"
    title="编辑用户"
    width="500px"
    @close="handleClose"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
      <el-form-item label="用户名">
        <el-input :value="user?.username" disabled />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="form.nickname" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item label="签名">
        <el-input v-model="form.signature" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="角色">
        <el-checkbox-group v-model="form.roleIds" :loading="rolesLoading">
          <el-checkbox
            v-for="role in roles"
            :key="role.roleId"
            :label="role.roleId"
          >
            {{ role.roleName }}
          </el-checkbox>
        </el-checkbox-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi, roleApi } from '../../api'
import type { UserPageVO, RoleVO } from '../../api/types'

const props = defineProps<{
  visible: boolean
  user: UserPageVO | null
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  success: []
}>()

const formRef = ref()
const submitting = ref(false)
const rolesLoading = ref(false)
const roles = ref<RoleVO[]>([])

const form = reactive({
  nickname: '',
  email: '',
  phone: '',
  signature: '',
  roleIds: [] as number[]
})

const rules = {
  nickname: [{ max: 50, message: '昵称最多50个字符', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

// 监听用户变化，初始化表单
watch(
  () => props.user,
  async (newUser) => {
    if (newUser) {
      form.nickname = newUser.nickname || ''
      form.email = newUser.email || ''
      form.phone = newUser.phone || ''
      form.signature = newUser.signature || ''

      // 获取用户当前角色
      try {
        const res = await userApi.getUserRoles(newUser.userId)
        if (res.data.code === 200) {
          form.roleIds = res.data.data
        }
      } catch (e) {
        form.roleIds = []
      }
    }
  },
  { immediate: true }
)

// 加载角色列表
watch(
  () => props.visible,
  async (visible) => {
    if (visible && roles.value.length === 0) {
      rolesLoading.value = true
      try {
        const res = await roleApi.getList()
        if (res.data.code === 200) {
          roles.value = res.data.data
        }
      } catch (e) {
        ElMessage.error('获取角色列表失败')
      } finally {
        rolesLoading.value = false
      }
    }
  }
)

async function handleSubmit() {
  if (!props.user) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    // 更新用户资料
    await userApi.update(props.user.userId, {
      nickname: form.nickname,
      email: form.email,
      phone: form.phone,
      signature: form.signature
    })

    // 更新角色
    await userApi.updateUserRoles(props.user.userId, form.roleIds)

    ElMessage.success('保存成功')
    emit('success')
    handleClose()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.msg || '保存失败')
  } finally {
    submitting.value = false
  }
}

function handleClose() {
  emit('update:visible', false)
}
</script>
