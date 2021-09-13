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
import axios from 'axios'
import Vue from 'vue'

// import { Message } from 'element-ui'

import store from '@/store'
import { getToken, removeToken } from '@/utils/auth'
const service = axios.create({
    baseURL: process.env.NODE_ENV === 'mock' ? process.env.VUE_APP_BASE_API : process.env.BASE_API,
    withCredentials: true,
    timeout: 15000
})
let url = window.location.href
let arr = url.split('/')
let pram
if (arr[3] === '#' || arr[3] === '') {
    pram = ''
} else {
    pram = '/' + arr[3]
}
service.interceptors.request.use(
    config => {
        if (config.url !== `${pram}/api/component/list` && config.url !== `${pram}/api/monitor/query` &&
         config.url !== `${pram}/api/monitor/queryJvm` && config.url !== `${pram}/api/monitor/queryModel` &&
         config.url !== `${pram}/api/monitor/selfCheck` && config.url !== `${pram}/api/monitor/checkHealth`) {
            let loading = document.getElementById('ajaxLoading')
            loading.style.display = 'block'
        }
        if (store.getters.token) {
            config.headers['sessionToken'] = getToken()
        }
        config.params = {
            _t: +new Date(),
            ...config.params
        }
        return config
    },
    error => {
        Promise.reject(error)
    }
)

service.interceptors.response.use(
    response => {
        if (+response.data.retcode === 127) {
            removeToken()
            location.reload()
        }
        let loading = document.getElementById('ajaxLoading')
        loading.style.display = 'none'
        const res = response.data

        if (+res.retcode === 0) {
            return res
        } else if (response.config.url === '/api/admin/login' && +res.retcode === 120) {
            return Promise.reject(res)
        } else {
            Vue.prototype.$message.error({
                message: `${res.retmsg ? res.retmsg : 'http reqest failed!'}`,
                duration: 5 * 1000
            })
            return Promise.reject(res)
        }
    },
    error => {
        let loading = document.getElementById('ajaxLoading')
        loading.style.display = 'none'
        Vue.prototype.$message.error({
            message: 'http reqest failed!',
            duration: 5 * 1000
        })
        return Promise.reject(error)
    }
)

export default service
