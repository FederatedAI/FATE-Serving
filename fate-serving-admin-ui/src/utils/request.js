import axios from 'axios'
import { Message } from 'element-ui'

import store from '@/store'
import { getToken, removeToken } from '@/utils/auth'
// axios.defaults.headers.common['Authorization'] = getToken()
// create an axios instance
const service = axios.create({
    baseURL: process.env.NODE_ENV === 'mock' ? process.env.VUE_APP_BASE_API : process.env.BASE_API,
    withCredentials: true, // 跨域请求时发送 cookies
    timeout: 15000 // request timeout
})

// request interceptor
// 请求拦截
service.interceptors.request.use(
    config => {
        // 开启全局loading
        if (config.url !== '/api/component/list' && config.url !== '/api/monitor/query' && config.url !== '/api/monitor/queryJvm' && config.url !== '/api/monitor/queryModel') {
            let loading = document.getElementById('ajaxLoading')
            loading.style.display = 'block'
        }
        // Do something before request is sent
        if (store.getters.token) {
            // 让每个请求携带token-- ['X-Token']为自定义key 请根据实际情况自行修改
            // config.headers['Authorization'] = getToken()
            config.headers['sessionToken'] = getToken()
        }
        return config
    },
    error => {
        // Do something with request error
        Promise.reject(error)
    }
)

// response interceptor
// 响应拦截
service.interceptors.response.use(
    /**
   * If you want to get information such as headers or status
   * Please return  response => response
  */
    /**
   * 下面的注释为通过在response里，自定义code来标示请求状态
   * 当code返回如下情况则说明权限有问题，登出并返回到登录页
   * 如想通过 XMLHttpRequest 来状态码标识 逻辑可写在下面error中
   * 以下代码均为样例，请结合自生需求加以修改，若不需要，则可删除
   */
    response => {
        // 关闭全局loading
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
        // 关闭全局loading
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
