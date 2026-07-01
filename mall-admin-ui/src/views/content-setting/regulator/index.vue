<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.content-setting', 'menu.content-setting.regulator']" />
    <ContentEditor
      type="REGULATOR"
      title="监管机构"
      default-title="监管机构"
    />
  </div>
</template>

<script setup lang="ts">
  import ContentEditor from '../ContentEditor.vue';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'regulator',
  });
</script>

<style scoped></style>