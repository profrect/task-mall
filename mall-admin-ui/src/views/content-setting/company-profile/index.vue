<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.content-setting', 'menu.content-setting.company-profile']" />
    <ContentEditor
      type="COMPANY_PROFILE"
      title="公司简介"
      default-title="公司简介"
    />
  </div>
</template>

<script setup lang="ts">
  import ContentEditor from '../ContentEditor.vue';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'company-profile',
  });
</script>

<style scoped></style>