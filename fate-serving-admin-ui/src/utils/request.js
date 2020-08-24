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
import { Message } from 'element-ui'

import store from '@/store'
import { getToken, removeToken } from '@/utils/auth'
const service = axios.create({
    baseURL: process.env.NODE_ENV === 'mock' ? process.env.VUE_APP_BASE_API : process.env.BASE_API,
    withCredentials: true,
    timeout: 15000
})

service.interceptors.request.use(
    config => {
        if (config.url !== '/api/component/list' && config.url !== '/api/monitor/query' && config.url !== '/api/monitor/queryJvm' && config.url !== '/api/monitor/queryModel') {
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
            Message({
                message: `${res.retmsg ? res.retmsg : 'http reqest failed!'}`,
                type: 'error',
                duration: 5 * 1000
            })
            return Promise.reject(res)
        }
    },
    error => {
        let loading = document.getElementById('ajaxLoading')
        loading.style.display = 'none'
        Message({
            // message: error.message,
            message: 'http reqest failed!',
            type: 'error',
            duration: 5 * 1000
        })
        return Promise.reject(error)
    }
)

export default service
