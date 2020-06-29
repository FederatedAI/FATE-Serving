import Vue from 'vue'

import ElementUI from 'element-ui'
import '@/styles/theme/index.css'
import '@/styles/index.scss'
import '@/styles/loading.scss'
import locale from 'element-ui/lib/locale/lang/en'
import ECharts from 'vue-echarts'
import 'echarts'

import App from './App'
import store from './store'
import router from './router'
import waterfall from 'vue-waterfall2'
import '@/icons' // icon

import '@/permission' // permission control

/**
 * This project originally used easy-mock to simulate data,
 * but its official service is very unstable,
 * and you can build your own service if you need it.
 * So here I use Mock.js for local emulation,
 * it will intercept your request, so you won't see the request in the network.
 * If you remove `../mock` it will automatically request easy-mock data.
 */

if (process.env.NODE_ENV === 'mock') {
    require('../mock') // simulation data
}
Vue.use(waterfall)
Vue.use(ElementUI, { locale })
// Vue.use(VueClipboard)
// Vue.use(mavonEditor)
// Vue.component('my-tooltip', Tooltip)
Vue.component('v-chart', ECharts)// 全局使用
Vue.config.productionTip = false
/* eslint-disable no-new */
new Vue({
    el: '#app',
    router,
    store,
    render: h => h(App)
})
