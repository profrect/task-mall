<template>
  <div
    class="data_card_component"
    :class="spaceAround ? 'data_card_space_around' : 'data_card_space_between'"
  >
    <a-card
      v-for="(item, index) in cardDataList"
      :key="index"
      class="card_data_show_item"
    >
      <!-- 标题栏-->
      <div class="item_title">
        <div class="title_and_button">
          <div class="title">
            {{ item.name }}
          </div>
          <div
            v-if="refreshMethod"
            class="action_button"
            @click="refreshMethod"
          >
            <icon-refresh class="refresh" title="刷新数据" />
          </div>
        </div>
        <!-- 时间栏-->
        <div v-if="!noTime" class="item_time">
          {{ item.time }}
        </div>
      </div>
      <!-- 数据显示-->
      <div class="item_value">
        <a-skeleton-shape
          v-if="item.loading"
          animated
          style="width: 100%; height: 32px"
        />

        <div v-else>
          <div
            v-if="
              String(item.data).indexOf('.') > -1 ||
              String(item.data).indexOf('%') > -1
            "
          >
            {{ item.data }}
          </div>
          <digital-scroll v-else :value="item.data" :time="0.5" />
        </div>
      </div>
      <!-- 对比显示-->
      <div
        v-if="!noComparison && item.MOM"
        class="item_comparison"
        title="点击查看数据"
      >
        <div class="comparison">
          <div>较环比</div>
          <a-popover trigger="click">
            <div class="text-ellipsis">
              <div
                v-if="item.MOM.startsWith('-')"
                class="comparison_value down"
              >
                <icon-caret-down />
                <div>{{ item.MOM.substring(1) }}</div>
              </div>
              <div v-else class="comparison_value up">
                <icon-caret-up />
                <div>{{ item.MOM }}</div>
              </div>
            </div>

            <template #content>
              <div class="comparison_last_value">
                <div class="comparison_last_value_title">环比历史数据</div>
                <div>{{ item.lastData || 0 }}</div>
              </div>
            </template>
          </a-popover>
        </div>

        <div class="comparison">
          <div>较同比</div>

          <a-popover trigger="click">
            <div class="text-ellipsis">
              <div
                v-if="item.YOY?.startsWith('-')"
                class="comparison_value down"
              >
                <icon-caret-down />
                {{ item.YOY }}
              </div>
              <div v-else class="comparison_value up">
                <icon-caret-up />
                {{ item.YOY }}
              </div>
            </div>
            <template #content>
              <div class="comparison_last_value">
                <div class="comparison_last_value_title">同比历史数据</div>
                <div>{{ item.lastYearData || 0 }}</div>
              </div>
            </template>
          </a-popover>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
  import { CardData } from '@/model/common';
  import DigitalScroll from '@/components/digital-scroll/digitalScroll.vue';

  defineProps<{
    cardDataList: Array<CardData>;
    noTime?: boolean;
    noComparison?: boolean;
    refreshMethod?: any;
    spaceAround?: boolean;
  }>();
</script>

<style lang="scss">
  .data_card_component {
    display: flex;
    flex-wrap: wrap;

    .card_data_show_item {
      width: 16vw;
      margin: 0 10px 10px 0;

      .item_title {
        margin-bottom: 10px;

        .title_and_button {
          display: flex;
          justify-content: space-between;
          margin-bottom: 5px;
          font-weight: bold;

          .action_button {
            width: 18px;
            height: 18px;

            .refresh {
              cursor: pointer;
              color: var(--color_white);
              transition: all 0.3s;
            }
          }
        }

        .item_time {
          font-size: var(--font_size_small);
          color: var(--color_gray);
        }
      }

      .item_value {
        margin-top: 15px;
        font-size: 35px;
        font-weight: bold;
        transition: all 0.2s;
      }

      .item_comparison {
        font-size: 13px;
        display: flex;
        margin-top: 10px;

        .comparison {
          flex-grow: 1;
          display: flex;
          align-items: center;
          cursor: pointer;

          .comparison_value {
            margin-left: 10px;
            display: flex;
            align-items: center;

            i {
              margin-right: 3px;
            }
          }

          .down {
            color: red;
          }

          .up {
            color: var(--color_green);

            .svg_icon {
              transform: rotate(180deg);
            }
          }

          .svg_icon {
            width: 17px;
            height: 17px;
            margin-right: 2px;
          }
        }
      }
    }

    .card_data_show_item:hover {
      .item_title {
        .title_and_button {
          .action_button {
            .refresh {
              color: #97a8be;
            }

            .refresh:hover {
              animation: refresh_animation 0.5s linear;
            }
          }
        }

        .item_time {
          font-size: var(--font_size_small);
        }
      }

      .item_value {
        color: var(--color_blue);
        transition: all 0.3s;
      }
    }
  }

  .data_card_space_around {
    justify-content: space-around;
  }

  .data_card_space_between {
    justify-content: flex-start;
  }

  .comparison_last_value {
    font-size: var(--font_size_small);
  }
</style>
