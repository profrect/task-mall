<template>
  <div class="banner">
    <div class="banner-inner">
      <a-carousel class="carousel" animation-name="fade">
        <a-carousel-item>
          <div class="carousel-item">
            <img class="carousel-image" :src="bannerImage" />
          </div>
        </a-carousel-item>
      </a-carousel>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import bannerImage from '@/assets/images/banner.png';
</script>

<style lang="less" scoped>
  .banner {
    display: flex;
    align-items: center;
    justify-content: center;

    &-inner {
      flex: 1;
      height: 100%;
    }
  }

  .carousel {
    height: 100%;

    &-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
    }

    &-image {
      width: 320px;
      @media (max-width: @screen-lg) {
        width: 20rem;
      }
      @media screen and (min-device-width: 0) and (max-device-width: @screen-xs) {
        width: 10rem;
      }
      margin-top: 30px;
    }
  }
</style>
