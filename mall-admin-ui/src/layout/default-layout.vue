<template>
  <a-layout class="layout" :class="{ mobile: appStore.hideMenu }">
    <div v-if="navbar" class="layout-navbar">
      <NavBar />
    </div>
    <a-layout>
      <a-layout>
        <a-layout-sider
          v-if="renderMenu"
          v-show="!hideMenu"
          class="layout-sider"
          breakpoint="xl"
          :collapsed="collapsed"
          :collapsible="true"
          :width="menuWidth"
          :style="{ paddingTop: navbar ? '60px' : '' }"
          hide-trigger
          @collapse="setCollapsed"
        >
          <div class="menu-wrapper">
            <!--菜单栏的数据是异步加载-->
            <Suspense>
              <Menu />
            </Suspense>
          </div>
          <!-- 自定义折叠按钮 -->
          <div class="custom-collapse-btn" @click="toggleCollapse">
            <icon-menu-fold v-if="!collapsed" />
            <icon-menu-unfold v-else />
          </div>
        </a-layout-sider>
        <a-drawer
          v-if="hideMenu"
          :visible="drawerVisible"
          placement="left"
          mask-closable
          :closable="false"
          @cancel="drawerCancel"
        >
          <Suspense>
            <Menu />
          </Suspense>
          <template #footer>
            <div style="justify-self: center" @click="drawerCancel">
              <icon-menu-fold />
            </div>
          </template>
        </a-drawer>
        <a-layout class="layout-content" :style="paddingStyle">
          <TabBar v-if="appStore.tabBar" />
          <a-layout-content>
            <PageLayout />
          </a-layout-content>
          <Footer v-if="footer" />
        </a-layout>
      </a-layout>
    </a-layout>
  </a-layout>
</template>

<script lang="ts" setup>
  import { ref, computed, provide, onMounted } from 'vue';
  // eslint-disable-next-line import/no-cycle
  import { useAppStore } from '@/store';
  import NavBar from '@/components/navbar/index.vue';
  import Menu from '@/components/menu/index.vue';
  import Footer from '@/components/footer/index.vue';
  import TabBar from '@/components/tab-bar/index.vue';
  import useResponsive from '@/hooks/responsive';
  import PageLayout from './page-layout.vue';

  const isInit = ref(false);
  const appStore = useAppStore();
  useResponsive(true);
  const navbarHeight = `60px`;
  const navbar = computed(() => appStore.navbar);
  const renderMenu = computed(() => appStore.menu && !appStore.topMenu);
  const hideMenu = computed(() => appStore.hideMenu);
  const footer = computed(() => appStore.footer);
  const menuWidth = computed(() => {
    return collapsed.value ? 48 : appStore.menuWidth;
  });
  const collapsed = ref(false);
  const toggleCollapse = () => {
    collapsed.value = !collapsed.value;
  };
  const paddingStyle = computed(() => {
    const paddingLeft =
      renderMenu.value && !hideMenu.value
        ? { paddingLeft: `${menuWidth.value}px` }
        : {};
    const paddingTop = navbar.value ? { paddingTop: navbarHeight } : {};
    return { ...paddingLeft, ...paddingTop };
  });
  const setCollapsed = (val: boolean) => {
    if (!isInit.value) return; // for page initialization menu state problem
    appStore.updateSettings({ menuCollapse: val });
  };
  // watch(
  //   () => userStore.role,
  //   (roleValue) => {
  //     if (roleValue) router.push({ name: 'notFound' });
  //   }
  // );
  const drawerVisible = ref(false);
  const drawerCancel = () => {
    drawerVisible.value = false;
  };
  provide('toggleDrawerMenu', () => {
    drawerVisible.value = !drawerVisible.value;
  });
  onMounted(() => {
    isInit.value = true;
  });
</script>

<style scoped lang="less">
  @nav-size-height: 60px;
  @layout-max-width: 1100px;

  .layout {
    width: 100%;
    height: 100%;
  }

  .layout-navbar {
    position: fixed;
    top: 0;
    left: 0;
    z-index: 100;
    width: 100%;
    height: @nav-size-height;
  }

  .layout-sider {
    position: fixed;
    top: 0;
    left: 0;
    z-index: 99;
    height: 100%;
    transition: all 0.2s cubic-bezier(0.34, 0.69, 0.1, 1);
    &::after {
      position: absolute;
      top: 0;
      right: -1px;
      display: block;
      width: 1px;
      height: 100%;
      background-color: var(--color-border);
      content: '';
    }

    > :deep(.arco-layout-sider-children) {
      overflow-y: hidden;
    }
  }

  .menu-wrapper {
    height: 100%;
    overflow: auto;
    overflow-x: hidden;
    :deep(.arco-menu) {
      ::-webkit-scrollbar {
        width: 12px;
        height: 4px;
      }

      ::-webkit-scrollbar-thumb {
        border: 4px solid transparent;
        background-clip: padding-box;
        border-radius: 7px;
        background-color: var(--color-text-4);
      }

      ::-webkit-scrollbar-thumb:hover {
        background-color: var(--color-text-3);
      }
    }
  }

  .layout-content {
    min-height: 100vh;
    overflow-y: hidden;
    background-color: var(--color-fill-2);
    transition: padding 0.2s cubic-bezier(0.34, 0.69, 0.1, 1);
  }

  .custom-collapse-btn {
    position: absolute;
    bottom: 0;
    width: 100%;
    padding: 10px 0;
    text-align: center;
    cursor: pointer;
    background-color: var(--color-menu-light-bg);
    border-top: 1px solid var(--color-border);
  }

  :deep(.arco-menu-inner) {
    padding-bottom: 35px !important;
  }
</style>
