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
import request from '@/utils/request'
let url = window.location.href
let arr = url.split('/')
let pram
if (arr[3] === '#' || arr[3] === '') {
    pram = ''
} else {
    pram = '/' + arr[3]
}
export function getCluster(params) {
    return request({
        url: `${pram}/api/component/list`,
        method: 'get',
        params
    })
}

// 节点配置列表
export function getlistProps(params) {
    return request({
        url: `${pram}/api/component/listProps`,
        method: 'get',
        params
    })
}

// /api/model/query
export function getmodellist(params) {
    return request({
        url: `${pram}/api/model/query`,
        method: 'get',
        params
    })
}
// /api/model/unload 模型卸载
export function modelUnload(data) {
    return request({
        url: `${pram}/api/model/unload`,
        method: 'post',
        data
    })
}

// /api/model/unbind 模型解绑
export function modelUnbind(data) {
    return request({
        url: `${pram}/api/model/unbind`,
        method: 'post',
        data
    })
}

// /api/monitor/queryModel
export function queryModel(params) {
    return request({
        url: `${pram}/api/monitor/queryModel`,
        method: 'get',
        params
    })
}

// /api/monitor/query
export function queryMonitor(params) {
    return request({
        url: `${pram}/api/monitor/query`,
        method: 'get',
        params
    })
}

// /api/monitor/queryJvm
export function queryJvm(params) {
    return request({
        url: `${pram}/api/monitor/queryJvm`,
        method: 'get',
        params
    })
}

// /api/service/updateFlowRule
export function updateFlowRule(data) {
    return request({
        url: `${pram}/api/service/updateFlowRule`,
        method: 'post',
        data
    })
}
