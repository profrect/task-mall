<template>
  <div class="pwd-page">
    <div class="page-header">{{ isFundPwd ? 'Fund Password' : 'Login Password' }}</div>

    <van-form @submit="onSubmit" class="pwd-form">
      <van-field
        v-model="form.oldPwd"
        :type="showOld ? 'text' : 'password'"
        label="Old Password"
        :rules="[{ required: true }]"
      >
        <template #button
          ><van-icon :name="showOld ? 'eye-o' : 'closed-eye'" @click="showOld = !showOld"
        /></template>
      </van-field>

      <van-field
        v-model="form.newPwd"
        :type="showNew ? 'text' : 'password'"
        label="New Password"
        :rules="[{ required: true, validator: validateStrength }]"
      >
        <template #button
          ><van-icon :name="showNew ? 'eye-o' : 'closed-eye'" @click="showNew = !showNew"
        /></template>
      </van-field>

      <!-- 密码强度指示器 -->
      <div class="strength-bar" v-if="form.newPwd">
        <div class="bar" :class="strengthClass"></div>
        <span class="label">{{ strengthLabel }}</span>
      </div>

      <van-field
        v-model="form.confirmPwd"
        :type="showNew ? 'text' : 'password'"
        label="Confirm"
        :rules="[{ validator: checkMatch, message: 'Mismatch' }]"
      />

      <!-- 资金密码专属：二次验证 -->
      <van-field
        v-if="isFundPwd"
        v-model="form.verifyCode"
        label="Verify Code"
        placeholder="SMS / Email code"
        :rules="[{ required: true }]"
      >
        <template #button>
          <van-button size="small" type="primary" plain :disabled="cd > 0" @click="sendCode">
            {{ cd > 0 ? `${cd}s` : 'Send' }}
          </van-button>
        </template>
      </van-field>

      <van-button type="primary" block round native-type="submit" style="margin-top: 24px"
        >Confirm Change</van-button
      >
    </van-form>

    <!-- 资金密码修改成功后的风控提示 -->
    <van-notice-bar
      v-if="fundPwdChanged"
      left-icon="info-o"
      color="#fff3e0"
      background="#ff9800"
      text="Fund operations suspended for 15 min after password change"
      wrapable
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isFundPwd = computed(() => route.params.type === 'fund-pwd')
const fundPwdChanged = ref(false)

const showOld = ref(false)
const showNew = ref(false)
const cd = ref(0)
const form = reactive({ oldPwd: '', newPwd: '', confirmPwd: '', verifyCode: '' })

const getStrength = (pwd: string) => {
  let score = 0
  if (pwd.length >= 8) score++
  if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) score++
  if (/\d/.test(pwd)) score++
  if (/[^a-zA-Z0-9]/.test(pwd)) score++
  return score
}

const strengthLevel = computed(() => getStrength(form.newPwd))
const strengthClass = computed(
  () => ['weak', 'medium', 'strong', 'very-strong'][strengthLevel.value - 1] || '',
)
const strengthLabel = computed(
  () => ['Weak', 'Medium', 'Strong', 'Very Strong'][strengthLevel.value - 1] || '',
)

const validateStrength = () => strengthLevel.value >= 2 || 'Password too weak'
const checkMatch = () => form.newPwd === form.confirmPwd
const sendCode = () => {
  cd.value = 60
  const t = setInterval(() => {
    cd.value--
    if (cd.value <= 0) clearInterval(t)
  }, 1000)
}
const onSubmit = () => {
  if (isFundPwd.value) fundPwdChanged.value = true
}
</script>

<style scoped>
.pwd-page {
  min-height: 100vh;
  background: #f5f6fa;
}
.page-header {
  padding: 24px 24px 0;
  font-size: 20px;
  font-weight: 700;
  color: #333;
}
.pwd-form {
  padding: 24px;
  background: #fff;
  margin: 16px 12px;
  border-radius: 12px;
}
.strength-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 16px 12px;
}
.bar {
  height: 4px;
  border-radius: 2px;
  transition: width 0.3s;
}
.bar.weak {
  width: 25%;
  background: #f44336;
}
.bar.medium {
  width: 50%;
  background: #ff9800;
}
.bar.strong {
  width: 75%;
  background: #4caf50;
}
.bar.very-strong {
  width: 100%;
  background: #1a237e;
}
.label {
  font-size: 11px;
  color: #999;
}
</style>
