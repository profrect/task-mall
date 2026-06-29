<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.sys-log', 'menu.sys-log.operation']" />
    <h2>操作日志</h2>
  </div>
</template>

<script setup lang="ts">
  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'member-list',
  });
</script>

<style scoped>

</style>