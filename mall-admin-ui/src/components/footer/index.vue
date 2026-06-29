<template>
  <a-layout-footer class="footer">
    {{ t('title.footer-name') }}
  </a-layout-footer>
</template>

<script lang="ts" setup>
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();

  window.document.title = t('title.app-name');
</script>

<style lang="less" scoped>
  .footer {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 40px;
    color: var(--color-text-2);
    text-align: center;
  }
</style>
