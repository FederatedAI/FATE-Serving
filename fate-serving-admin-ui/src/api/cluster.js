import request from '@/utils/request'

export function getCluster(params) {
    return request({
        url: '/api/component/list',
        method: 'get',
        params
    })
}

// 节点配置列表
export function getlistProps(params) {
    return request({
        url: '/api/component/listProps',
        method: 'get',
        params
    })
}

// /api/model/query
export function getmodellist(params) {
    return request({
        url: '/api/model/query',
        method: 'get',
        params
    })
}
// /api/model/unload 模型卸载
export function modelUnload(data) {
    return request({
        url: '/api/model/unload',
        method: 'post',
        data
    })
}

// /api/model/unbind 模型解绑
export function modelUnbind(data) {
    return request({
        url: '/api/model/unbind',
        method: 'post',
        data
    })
}

// /api/monitor/queryModel
export function queryModel(params) {
    return request({
        url: '/api/monitor/queryModel',
        method: 'get',
        params
    })
}

// /api/monitor/query
export function queryMonitor(params) {
    return request({
        url: '/api/monitor/query',
        method: 'get',
        params
    })
}

// /api/monitor/queryJvm
export function queryJvm(params) {
    return request({
        url: '/api/monitor/queryJvm',
        method: 'get',
        params
    })
}
