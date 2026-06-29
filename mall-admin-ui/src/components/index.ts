import { App } from 'vue';
import Chart from './chart/index.vue';
import BaseTable from './base-table/index.vue';
import Breadcrumb from './breadcrumb/index.vue';

export default {
  install(Vue: App) {
    Vue.component('Chart', Chart);
    Vue.component('BaseTable', BaseTable);
    Vue.component('Breadcrumb', Breadcrumb);
  },
};
