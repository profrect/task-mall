<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.content-setting', 'menu.content-setting.platform-profile']" />
    <ContentEditor
      type="PLATFORM_PROFILE"
      title="平台简介"
      default-title="平台简介"
    />
  </div>
</template>

<script setup lang="ts">
  import ContentEditor from '../ContentEditor.vue';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'platform-profile',
  });
</script>

<style scoped></style>