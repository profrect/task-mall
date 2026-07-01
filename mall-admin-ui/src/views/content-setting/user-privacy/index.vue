<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.content-setting', 'menu.content-setting.user-privacy']" />
    <ContentEditor
      type="USER_PRIVACY"
      title="隐私协议"
      default-title="隐私协议"
    />
  </div>
</template>

<script setup lang="ts">
  import ContentEditor from '../ContentEditor.vue';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'user-privacy',
  });
</script>

<style scoped></style>