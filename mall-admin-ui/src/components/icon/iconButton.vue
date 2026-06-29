<template>
  <div class="custom_icon_button">
    <div :class="props.colorClass" class="custom_icon_button_main">
      <dynamic-icon :icon="props.iconName" :size="size" />
    </div>
  </div>
</template>

<script setup lang="ts">
  import DynamicIcon from '@/components/icon/dynamicIcon.vue';

  const props = defineProps({
    iconName: {
      type: String,
      required: true,
    },
    colorClass: {
      type: String,
      default: 'white',
      validator(value: string) {
        return ['white', 'red'].includes(value);
      },
    },
    size: Number,
  });
</script>

<style lang="scss">
  .custom_icon_button {
    width: 100%;
    height: 100%;
    display: inline-flex;
    align-items: center;

    .custom_icon_button_main {
      width: 100%;
      height: 100%;
      display: inline-flex;
      align-items: center;
      border-radius: 5px;
      padding: 0 2px;
      cursor: pointer;
      border: 1px solid var(--color_white);
      transition: color 0.2s;
      justify-content: center;
    }

    .white {
      border-color: var(--color_gray2);
      color: var(--color_gray2);
    }

    .white:hover {
      border-color: var(--color_blue);
      color: var(--color_blue);
    }

    .red {
      border-color: var(--color_red2);
      color: var(--color_red2);
    }

    .red:hover {
      border-color: var(--color_red);
      color: var(--color_red);
    }
  }
</style>
