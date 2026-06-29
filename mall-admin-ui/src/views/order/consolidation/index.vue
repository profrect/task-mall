<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.order', 'menu.order.consolidation']" />
    <h2>归集订单</h2>
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