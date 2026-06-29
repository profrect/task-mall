<template>
  <a-upload
    v-if="props.ifImport"
    action="/"
    accept=".xls,.xlsx"
    :multiple="false"
    :show-file-list="false"
    :custom-request="uploadFile"
    style="margin-right: 20px"
  >
    <template #upload-button>
      <div>
        <a-button status="success" :loading="imLoading">
          <template #icon><icon-import /></template>
          导入
        </a-button>
      </div>
    </template>
  </a-upload>

  <a-button v-if="props.ifExport" :loading="exLoading" @click="downloadFile">
    <template #icon>
      <icon-export />
    </template>
    导出
  </a-button>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';
  import { fileDown } from '@/utils/fileUtils';
  import { ref } from 'vue';

  const props = withDefaults(
    defineProps<{
      ifImport?: boolean;
      ifExport: boolean;
      imMethod?: any;
      exMethod?: any;
      fileName: string;
    }>(),
    {
      ifImport: () => true,
      ifExport: () => true,
    }
  );

  const imLoading = ref(false);
  const exLoading = ref(false);

  const emit = defineEmits(['refreshTable']);

  const uploadFile = async (info: any) => {
    const { fileItem } = info;
    const { file } = fileItem;

    // 构建 FormData 发送文件
    const formData = new FormData();
    formData.append('file', file);
    imLoading.value = true;
    await props
      .imMethod(formData)
      .then((resp: { success: boolean }) => {
        if (resp.success) {
          // 设置返回结果
          Message.success('导入成功');
          emit('refreshTable');
        }
      })
      .finally(() => {
        imLoading.value = false;
      });
  };

  // 导出
  function downloadFile() {
    exLoading.value = true;
    props
      .exMethod()
      .then((blob: Blob) => {
        if (blob) {
          fileDown(blob, `${props.fileName}.xlsx`);
        }
      })
      .finally(() => {
        exLoading.value = false;
      });
  }
</script>

<style scoped lang="scss"></style>
