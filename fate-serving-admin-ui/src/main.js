/**
*  Copyright 2019 The FATE Authors. All Rights Reserved.
*
*  Licensed under the Apache License, Version 2.0 (the 'License');
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an 'AS IS' BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
**/
import Vue from 'vue'

import ElementUI from 'element-ui'
import '@/styles/theme/index.css'
import '@/styles/index.scss'
import '@/styles/loading.scss'
import locale from 'element-ui/lib/locale/lang/en'

import App from './App'
import store from './store'
import router from './router'
import '@/icons' // icon

import '@/permission' // permission control

import * as filters from './filters'

if (process.env.NODE_ENV === 'mock') {
    require('../mock') // simulation data
}
Object.keys(filters).forEach(key => Vue.filter(key, filters[key]))
Vue.use(ElementUI, { locale })
Vue.config.productionTip = false
/* eslint-disable no-new */
new Vue({
    el: '#app',
    router,
    store,
    render: h => h(App)
})
