import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import Vant from 'vant';
import 'vant/lib/index.css';
import './assets/styles/main.css';
import { Toast } from 'vant';
import i18n from './i18n'; // 路径根据实际调整

const app = createApp(App);
app.use(Vant);
app.use(router);
app.use(Toast);
app.use(i18n)
app.mount('#app');
