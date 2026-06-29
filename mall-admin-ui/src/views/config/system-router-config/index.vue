<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.sys-permission', 'menu.system-router-config']" />
    <menu-table title="system-router-config" :menu-tag="0" />
  </div>
</template>

<script setup lang="ts">
  import MenuTable from '@/components/menu/menuTable.vue';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'system-router-config',
  });
</script>

<style scoped lang="less"></style>
