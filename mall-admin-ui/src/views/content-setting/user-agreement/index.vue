<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.content-setting', 'menu.content-setting.user-agreement']" />
    <ContentEditor
      type="USER_AGREEMENT"
      title="用户协议"
      default-title="用户协议"
    />
  </div>
</template>

<script setup lang="ts">
  import ContentEditor from '../ContentEditor.vue';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'user-agreement',
  });
</script>

<style scoped></style>