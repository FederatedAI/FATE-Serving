import request from '@/utils/request'
// 获取service 列表
export function getserviceList(params) {
    return request({
        url: '/api/service/list',
        method: 'get',
        params
    })
}

//  更新服务
export function serviceUpdate(data) {
    return request({
        url: '/api/service/update',
        method: 'post',
        data
    })
}
