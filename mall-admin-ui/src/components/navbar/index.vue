<template>
  <div class="navbar">
    <div class="left-side">
      <a-space>
        <router-link :to="{ path: '/dashboard/home' }">
          <img alt="logo" src="../../assets/images/logo.png" />
        </router-link>
        <a-typography-title
          :style="{ margin: 0, fontSize: '18px' }"
          :heading="5"
        >
          {{ appName }}
        </a-typography-title>
        <icon-menu-fold
          v-if="showMobileMenuButton"
          style="font-size: 22px; cursor: pointer"
          @click="toggleDrawerMenu"
        />
      </a-space>
    </div>
    <div class="center-side">
      <Suspense>
        <ModuleMenu v-if="moduleMenu" />
      </Suspense>
    </div>
    <ul class="right-side">
      <li>
        <a-tooltip :content="t('settings.language')">
          <a-button
            class="nav-btn"
            type="outline"
            :shape="'circle'"
            @click="setDropDownVisible"
          >
            <template #icon>
              <icon-language />
            </template>
          </a-button>
        </a-tooltip>
        <a-dropdown trigger="click" @select="changeLocale as any">
          <div ref="triggerBtn" class="trigger-btn"></div>
          <template #content>
            <a-doption
              v-for="item in locales"
              :key="item.value"
              :value="item.value"
            >
              <template #icon>
                <icon-check v-show="item.value === currentLocale" />
              </template>
              {{ item.label }}
            </a-doption>
          </template>
        </a-dropdown>
      </li>
      <li>
        <a-tooltip
          :content="
            theme === 'light'
              ? t('settings.navbar.theme.toDark')
              : t('settings.navbar.theme.toLight')
          "
        >
          <a-button
            class="nav-btn"
            type="outline"
            :shape="'circle'"
            @click="handleToggleTheme"
          >
            <template #icon>
              <icon-moon-fill v-if="theme === 'dark'" />
              <icon-sun-fill v-else />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-tooltip
          :content="
            isFullscreen
              ? t('settings.navbar.screen.toExit')
              : t('settings.navbar.screen.toFull')
          "
        >
          <a-button
            class="nav-btn"
            type="outline"
            :shape="'circle'"
            @click="toggleFullScreen"
          >
            <template #icon>
              <icon-fullscreen-exit v-if="isFullscreen" />
              <icon-fullscreen v-else />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-tooltip :content="t('settings.title')">
          <a-button
            class="nav-btn"
            type="outline"
            :shape="'circle'"
            @click="setVisible"
          >
            <template #icon>
              <icon-settings />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-dropdown trigger="click">
          <a-avatar
            :size="32"
            :style="{ marginRight: '8px', cursor: 'pointer' }"
          >
            <icon-user />
          </a-avatar>
          <template #content>
            <a-doption class="doption_style">
              <a-space>
                <icon-home />
                <router-link to="/dashboard/home" class="doption_style">
                  <span>{{ t('user.back-to-home') }}</span>
                </router-link>
              </a-space>
            </a-doption>
            <a-doption>
              <a-space @click="visible = true">
                <icon-user />
                <span>{{ t('user.userCenter') }}</span>
              </a-space>
            </a-doption>
            <a-doption>
              <a-space @click="passwordVisible = true">
                <icon-pen-fill />
                <span>{{ t('user.change-password') }}</span>
              </a-space>
            </a-doption>
            <a-doption>
              <a-space @click="handleLogout">
                <icon-export />
                <span>{{ t('user.logout') }}</span>
              </a-space>
            </a-doption>
          </template>
        </a-dropdown>
      </li>
    </ul>

    <!--用户信息-->
    <a-modal v-model:visible="visible" width="30%">
      <template #title> 个人信息 </template>
      <a-form :model="userInfo">
        <a-form-item :label="t('user-info.name')">
          {{ userInfo.username }}
        </a-form-item>
        <a-form-item :label="t('user-info.realName')">
          {{ userInfo.realName }}
        </a-form-item>
        <a-form-item :label="t('user-info.role')">
          {{ roleName }}
        </a-form-item>
        <a-form-item :label="t('user-info.email')">
          {{ userInfo.email }}
        </a-form-item>
      </a-form>
    </a-modal>

    <!--修改密码-->
    <a-modal v-model:visible="passwordVisible" title="修改密码" width="30%">
      <a-form
        ref="updatePasswordForm"
        :model="updatePasswordData"
        :rules="updatePasswordRules"
        auto-label-width
      >
        <a-form-item field="oldPassword" label="旧密码">
          <a-input-password v-model="updatePasswordData.oldPassword" />
        </a-form-item>
        <a-form-item field="newPassword" label="新密码">
          <a-input-password v-model="updatePasswordData.newPassword" />
        </a-form-item>
        <a-form-item field="newPassword2" label="密码确认">
          <a-input-password v-model="updatePasswordData.newPassword2" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-button type="primary" @click="submitUpdatePassword">确认</a-button>
        <a-button @click="passwordVisible = false">取消</a-button>
      </template>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref, inject, reactive, onMounted } from 'vue';
  import { useDark, useToggle, useFullscreen } from '@vueuse/core';
  import { useAppStore, useUserStore } from '@/store';
  import { LOCALE_OPTIONS } from '@/locale';
  import useLocale from '@/hooks/locale';
  import useUser from '@/hooks/user';
  import ModuleMenu from '@/components/menu/module-menu.vue';
  import { useI18n } from 'vue-i18n';
  import { getUserInfo, updatePassword, User } from '@/api/user';
  import { isItem } from '@/store/storage/storage';
  import { USER_TOKEN_KEY } from '@/store/user';
  import { Message } from '@arco-design/web-vue';
  import { appName } from '@/utils/env';

  const { t } = useI18n();
  const appStore = useAppStore();
  const userInfo = ref(new User({}));
  onMounted(() => {
    if (isItem(USER_TOKEN_KEY)) {
      getUserInfo().then((resp) => {
        userInfo.value.updateUserInfo(resp.data);
        useUserStore().setUserInfo(resp.data);
        getUserRole();
      });
    }
  });

  const { logout } = useUser();
  const { changeLocale, currentLocale } = useLocale();
  const { isFullscreen, toggle: toggleFullScreen } = useFullscreen();
  const locales = [...LOCALE_OPTIONS];
  const theme = computed(() => {
    return appStore.theme;
  });
  const moduleMenu = computed(() => appStore.menu);
  const showMobileMenuButton = computed(
    () => appStore.menu && appStore.device === 'mobile'
  );
  const isDark = useDark({
    selector: 'body',
    attribute: 'arco-theme',
    valueDark: 'dark',
    valueLight: 'light',
    storageKey: 'arco-theme',
    onChanged(dark: boolean) {
      // overridden default behavior
      appStore.toggleTheme(dark);
    },
  });
  const toggleTheme = useToggle(isDark);
  const visible = ref(false);
  const passwordVisible = ref(false);

  const handleToggleTheme = () => {
    toggleTheme();
  };
  const setVisible = () => {
    appStore.updateSettings({ globalSettings: true });
  };
  const triggerBtn = ref();
  const handleLogout = () => {
    logout();
  };
  const setDropDownVisible = () => {
    const event = new MouseEvent('click', {
      view: window,
      bubbles: true,
      cancelable: true,
    });
    triggerBtn.value.dispatchEvent(event);
  };
  const toggleDrawerMenu = inject('toggleDrawerMenu') as () => void;

  // 用户所属组别
  const roleName = ref('');
  function getUserRole() {
    if (!userInfo.value.roles || userInfo.value.roles === '[]') {
      roleName.value = '不属于任何组';
      return;
    }

    let s = '';
    JSON.parse(userInfo.value.roles).forEach((role: string) => {
      s += `${role}，`;
    });
    roleName.value = s.length > 0 ? s.substring(0, s.length - 1) : s;
  }

  // 用户密码
  const updatePasswordForm = ref();
  const updatePasswordData = reactive({
    oldPassword: '',
    newPassword: '',
    newPassword2: '',
  });
  const updatePasswordRules = {
    oldPassword: [{ required: true, trigger: 'change', message: '不能为空' }],
    newPassword: [
      {
        required: true,
        trigger: 'change',
        validator: (value: string, cb: any) => {
          if (!updatePasswordData.newPassword) {
            cb('不能为空');
          }
          if (
            updatePasswordData.newPassword === updatePasswordData.oldPassword
          ) {
            cb('新旧密码不能相同');
          }
          cb();
        },
      },
    ],
    newPassword2: [
      {
        required: true,
        trigger: 'change',
        validator: (value: string, cb: any) => {
          if (!value) {
            cb('不能为空');
          }
          if (
            updatePasswordData.newPassword !== updatePasswordData.newPassword2
          ) {
            cb('两次输入的新密码不相同');
          }

          cb();
        },
      },
    ],
  };

  function submitUpdatePassword() {
    updatePasswordForm.value?.validate((valid: any) => {
      if (valid !== undefined) {
        Message.warning('请完成填写');
        return;
      }

      updatePassword({
        username: userInfo.value.username,
        newPswd: updatePasswordData.newPassword,
        oldPswd: updatePasswordData.oldPassword,
      }).then((rep) => {
        if (rep.success) {
          Message.success('修改密码成功');
          passwordVisible.value = false;
          updatePasswordData.newPassword = '';
          updatePasswordData.newPassword2 = '';
          updatePasswordData.oldPassword = '';
        }
      });
    });
  }
</script>

<style scoped lang="less">
  .navbar {
    display: flex;
    justify-content: space-between;
    height: 100%;
    overflow-x: auto;
    overflow-y: hidden;
    background-color: var(--color-bg-2);
    border-bottom: 1px solid var(--color-border);
    flex-wrap: nowrap;
  }

  .left-side {
    display: flex;
    align-items: center;
    padding-left: 20px;
    flex-shrink: 0; /* 关键：禁止左侧收缩 */
    white-space: nowrap; /* 禁止左侧文本换行 */
  }

  .center-side {
    display: flex;
    flex: 1 1 auto;
    align-items: center;
    justify-content: center;
    min-width: 0;
    overflow: hidden;
    white-space: nowrap;
  }

  .right-side {
    display: flex;
    padding-right: 20px;
    list-style: none;
    flex-shrink: 0; /* 关键：禁止右侧收缩 */
    white-space: nowrap; /* 禁止右侧整体文本换行 */

    :deep(.locale-select) {
      border-radius: 20px;
      white-space: nowrap; /* 下拉框文本不换行 */
    }

    li {
      display: flex;
      align-items: center;
      padding: 0 10px;
      white-space: nowrap; /* 禁止每个li内文本换行 */
    }

    a {
      color: var(--color-text-1);
      text-decoration: none;
      white-space: nowrap; /* 禁止链接文本换行 */
    }

    .nav-btn {
      border-color: rgb(var(--gray-2));
      color: rgb(var(--gray-8));
      font-size: 16px;
      white-space: nowrap; /* 按钮文本不换行 */
    }

    .trigger-btn,
    .ref-btn {
      position: absolute;
      bottom: 14px;
      white-space: nowrap; /* 按钮文本不换行 */
    }
    .trigger-btn {
      margin-left: 14px;
    }
  }
</style>

<style lang="less">
  .message-popover {
    .arco-popover-content {
      margin-top: 0;
    }
  }

  .layout_head_left_item {
    .arco-typography {
      margin: auto;
    }
    margin-top: 2px;
    margin-left: 20px;
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
  }

  .doption_style {
    text-decoration-line: none;
    span {
      color: black;
    }
  }

  .demo-basic {
    padding: 2rem;
    @media (max-width: @screen-lg) {
      width: 55rem;
    }
    @media screen and (min-device-width: 0) and (max-device-width: @screen-md) {
      width: 30rem;
      margin-left: 1rem;
    }
    background-color: var(--color-bg-popup);
    border-radius: 4px;
    box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.15);
    overflow: scroll;
  }
</style>
