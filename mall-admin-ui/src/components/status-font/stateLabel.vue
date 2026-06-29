<template>
  <div class="state_label_component">
    <div class="state_label" :class="getClass()">
      <div class="state_label_text">
        {{ text }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  const props = defineProps({
    text: {
      type: String,
      default: '',
    },
    size: {
      type: String,
      default: 'normal',
    },
    color: {
      type: String,
      default: 'blue',
    },
  });

  function getClass() {
    return `${props.color} ${props.size}`;
  }
</script>

<style lang="scss">
  .state_label_component {
    .state_label {
      border-radius: 5px;
      display: inline-block;
    }

    .blue {
      background-color: #fff7ec;

      .state_label_text {
        color: var(--color_blue);
      }
    }

    .red {
      background-color: #fef0f0;

      .state_label_text {
        color: #f56c6c;
      }
    }

    .green {
      background-color: #f0f9eb;

      .state_label_text {
        color: #67c23a;
      }
    }

    .yellow {
      background-color: #fdf6ec;

      .state_label_text {
        color: #e6a23c;
      }
    }

    .gray {
      background-color: #f4f4f5;

      .state_label_text {
        color: #909399;
      }
    }

    .normal {
      .state_label_text {
        margin: 3px 15px;
      }
    }
  }
</style>
