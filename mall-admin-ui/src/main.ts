import { createApp } from 'vue';
import ArcoVue from '@arco-design/web-vue';
import ArcoVueIcon from '@arco-design/web-vue/es/icon';
import globalComponents from '@/components';
import setupPlugins from '@/plugins';
import store from './store';
import i18n from './locale';
import App from './App.vue';
import '@/assets/style/global.less';
import '@/api/request';
import '@/styles/index.scss';
import '@/styles/arco-pro-style.scss';

const app = createApp(App);

app.use(ArcoVue, {});
app.use(ArcoVueIcon);

app.use(setupPlugins);
app.use(store);
app.use(i18n);
app.use(globalComponents);

app.mount('#app');
