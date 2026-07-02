<template>
  <div class="auth-page">
    <div class="logo-area">
      <van-icon name="shop-o" size="48" color="#1a237e" />
      <h1>Mall Platform</h1>
    </div>

    <van-tabs v-model:active="activeTab" shrink>
      <van-tab title="Login" name="login">
        <van-form @submit="onLogin" class="auth-form">
          <van-field
            v-model="loginForm.account"
            label="Account"
            placeholder="Phone / Email / ID"
            :rules="[{ required: true }]"
          />
          <van-field
            v-model="loginForm.password"
            :type="showPwd ? 'text' : 'password'"
            label="Password"
            placeholder="Enter password"
            :rules="[{ required: true }]"
          >
            <template #button>
              <van-icon :name="showPwd ? 'eye-o' : 'closed-eye'" @click="showPwd = !showPwd" />
            </template>
          </van-field>
          <div class="form-actions">
            <van-checkbox v-model="loginForm.agreed" shape="square" icon-size="16px">
              I agree to <a href="/terms">Terms</a> & <a href="/privacy">Privacy</a>
            </van-checkbox>
            <router-link to="/auth/forgot-pwd" class="forgot-link">Forgot?</router-link>
          </div>
          <van-button type="primary" block round native-type="submit" :loading="submitting" :disabled="!loginForm.agreed"
            >Login</van-button
          >
        </van-form>
      </van-tab>

      <van-tab title="Register" name="register">
        <div class="form-tips">
          <van-icon name="info-o" size="14" />
          <span>Fields marked with <span class="required-star">*</span> are required</span>
        </div>

        <van-form @submit="onRegister" class="auth-form" validate-trigger="onBlur">
          <!-- 账号（必填） -->
          <van-field
            v-model="regForm.account"
            label="Account"
            placeholder="Username / Phone"
            :rules="[
              { required: true, message: 'Account is required' },
              { pattern: /^[a-zA-Z0-9_]{4,20}$/, message: '4-20 chars, letters/numbers/_' },
            ]"
          >
            <template #label><span class="required-star">*</span> Account</template>
          </van-field>

          <!-- 密码（必填） -->
          <van-field
            v-model="regForm.password"
            :type="showRegPwd ? 'text' : 'password'"
            label="Password"
            placeholder="8+ chars, letter & number"
            :rules="[{ required: true, validator: validatePwdStrength }]"
          >
            <template #label><span class="required-star">*</span> Password</template>
            <template #button>
              <van-icon
                :name="showRegPwd ? 'eye-o' : 'closed-eye'"
                @click="showRegPwd = !showRegPwd"
              />
            </template>
          </van-field>

          <!-- 确认密码（必填） -->
          <van-field
            v-model="regForm.confirmPwd"
            :type="showRegPwd ? 'text' : 'password'"
            label="Confirm"
            placeholder="Re-enter password"
            :rules="[
              { required: true, validator: checkPwdMatch, message: 'Passwords do not match' },
            ]"
          >
            <template #label><span class="required-star">*</span> Confirm</template>
          </van-field>

          <!-- 邮箱（必填 - 密码找回专用） -->
          <van-field
            v-model="regForm.email"
            label="Email"
            placeholder="For password recovery"
            type="email"
            :rules="[
              { required: true, message: 'Email is required for password reset' },
              { pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Invalid email format' },
            ]"
          >
            <template #label><span class="required-star">*</span> Email</template>
            <template #extra>
              <van-popover
                trigger="click"
                content="Used ONLY for password recovery. Will not be used for marketing."
              >
                <template #reference>
                  <van-icon name="question-o" size="16" color="#999" class="help-icon" />
                </template>
              </van-popover>
            </template>
          </van-field>

          <van-field
            v-model="regForm.nickname"
            label="Nickname"
            placeholder="Please enter a nickname you like"
            clearable
          >
          </van-field>

          <!-- 邀请码（选填 - 显式标注） -->
          <van-field
            v-model="regForm.inviteCode"
            label="Invite Code"
            placeholder="Optional - Enter if you have one"
            clearable
          >
            <template #label> Invite Code </template>
            <template #extra>
              <van-popover
                trigger="click"
                content="Enter an invite code to unlock exclusive welcome rewards. Leave blank if you don't have one."
              >
                <template #reference>
                  <van-icon name="question-o" size="16" color="#999" class="help-icon" />
                </template>
              </van-popover>
            </template>
          </van-field>

          <!-- 协议勾选 -->
          <van-checkbox
            v-model="regForm.agreed"
            shape="square"
            icon-size="16px"
            class="agree-check"
          >
            I agree to <a href="/terms">Terms</a> & <a href="/privacy">Privacy</a>
          </van-checkbox>

          <van-button type="primary" block round native-type="submit" :loading="submitting" :disabled="!regForm.agreed">
            Create Account
          </van-button>
        </van-form>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showSuccessToast } from 'vant'
import { login, register } from '@/api/auth'
import { tokenStore } from '@/api/http'
import { store } from '@/store'

const router = useRouter()
const route = useRoute()

const activeTab = ref('login')
const showPwd = ref(false)
const showRegPwd = ref(false)
const submitting = ref(false)

const loginForm = reactive({ account: '', password: '', agreed: false })
const regForm = reactive({
  account: '',
  email: '',
  password: '',
  confirmPwd: '',
  inviteCode: '',
  nickname: '',
  agreed: false,
})

const onLogin = async () => {
  if (submitting.value) return
  submitting.value = true
  try {
    const { accessToken } = await login({
      account: loginForm.account.trim(),
      password: loginForm.password,
    })
    tokenStore.set(accessToken)
    store.setLoggedIn(true)
    store.setImpersonated(false)
    showSuccessToast('登录成功')
    const redirect = (route.query.redirect as string) || '/wallet'
    router.replace(redirect)
  } finally {
    submitting.value = false
  }
}

const validatePwdStrength = (val: string) => {
  if (!val) return 'Password is required'
  if (val.length < 8) return 'At least 8 characters'
  if (!/[a-zA-Z]/.test(val) || !/\d/.test(val)) return 'Must contain letters and numbers'
  return true
}

// 密码一致性校验器
const checkPwdMatch = (val: string) => {
  if (!val) return 'Please confirm your password'
  if (val !== regForm.password) return 'Passwords do not match'
  return true
}

const onRegister = async () => {
  if (submitting.value) return
  submitting.value = true
  try {
    await register({
      account: regForm.account.trim(),
      password: regForm.password,
      email: regForm.email.trim(),
      nickname: regForm.nickname?.trim() || undefined,
      inviteCode: regForm.inviteCode?.trim() || undefined,
    })
    showSuccessToast('注册成功，请登录')
    loginForm.account = regForm.account.trim()
    activeTab.value = 'login'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  background: #fff;
  padding: 40px 24px 24px;
}
.logo-area {
  text-align: center;
  margin-bottom: 32px;
}
.logo-area h1 {
  font-size: 24px;
  color: #1a237e;
  margin-top: 12px;
}
.auth-form {
  margin-top: 16px;
}
.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0 20px;
  font-size: 12px;
}
.forgot-link {
  color: #1a237e;
  text-decoration: none;
}

/* 必填提示条 */
.form-tips {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #666;
  background: #fafafa;
  border-radius: 8px;
}
.required-star {
  color: #e53935;
  font-weight: 700;
  margin-right: 2px;
}

/* 选填标签 */
.optional-tag {
  display: inline-block;
  font-size: 10px;
  color: #fff;
  background: #bdbdbd;
  padding: 1px 6px;
  border-radius: 4px;
  margin-right: 6px;
  vertical-align: middle;
}

/* 帮助图标 */
.help-icon {
  cursor: pointer;
  padding: 4px;
}

/* 协议勾选 */
.agree-check {
  margin: 16px 0 20px;
  font-size: 12px;
}
a {
  color: #1a237e;
  text-decoration: none;
}
</style>
