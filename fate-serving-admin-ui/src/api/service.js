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
// 获取service 列表
export function getserviceList(params) {
    return request({
        url: `${pram}/api/service/list`,
        method: 'get',
        params
    })
}

//  更新服务
export function serviceUpdate(data) {
    return request({
        url: `${pram}/api/service/update`,
        method: 'post',
        data
    })
}
//  更新服务
export function validate(data, name) {
    return request({
        url: `${pram}/api/validate/` + name,
        method: 'post',
        data
    })
}
