import { resolve } from 'path';
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';
import svgLoader from 'vite-svg-loader';
import configArcoStyleImportPlugin from './plugin/arcoStyleImport';

export default defineConfig({
  build: {
    chunkSizeWarningLimit: 1000, // 调整chunk大小警告阈值
    rollupOptions: {
      output: {
        // eslint-disable-next-line consistent-return
        manualChunks(id) {
          if (id.includes('views/')) {
            return `page-${id.split('views/')[1].split('/')[0]}`;
          }
          // 拆分node_modules依赖
          if (id.includes('node_modules')) {
            return 'vendor';
          }
        },
      },
    },
  },
  server: {
    host: '0.0.0.0', // 允许所有地址访问（包含内网IP）
    port: 5173, // 比如 3000，保持和之前一致
    // 1. 关闭冗余的文件监听（减少服务器资源占用）
    watch: {
      ignored: ['**/node_modules/**', '**/dist/**', '**/mock/**'],
    },
    hmr: {
      overlay: false, // 关闭热更新错误浮层（非必需）
    },
  },
  plugins: [
    vue(),
    vueJsx(),
    svgLoader({ svgoConfig: {} }),
    configArcoStyleImportPlugin(),
  ],
  resolve: {
    alias: [
      {
        find: '@',
        replacement: resolve(__dirname, '../src'),
      },
      {
        find: 'assets',
        replacement: resolve(__dirname, '../src/assets'),
      },
      {
        find: 'vue-i18n',
        replacement: 'vue-i18n/dist/vue-i18n.cjs.js', // Resolve the i18n warning issue
      },
      {
        find: 'vue',
        replacement: 'vue/dist/vue.esm-bundler.js', // compile template
      },
    ],
    extensions: ['.ts', '.js'],
  },
  define: {
    'process.env': {},
    '__VUE_PROD_HYDRATION_MISMATCH_DETAILS__': 'true',
  },
  css: {
    preprocessorOptions: {
      less: {
        modifyVars: {
          hack: `true; @import (reference) "${resolve(
            'src/assets/style/breakpoint.less'
          )}";`,
        },
        javascriptEnabled: true,
      },
    },
  },
});
