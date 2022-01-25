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
import moment from 'moment'

export const dateform = (value) => {
    return value ? moment(value).format('YYYY-MM-DD HH:mm:ss') : '--'
}

export const parseTime = (time, fm) => { // 解析时间  time: 时间戳或者实践对象 fm: 格式 默认是{y}-{m}-{d} {h}:{i}:{s}
    if (!time && !fm) {
        return null
    }
    const format = fm || '{y}-{m}-{d} {h}:{i}:{s}'
    let date
    if (typeof time === 'object') {
        date = time
    } else {
        if (('' + time).length === 10) time = parseInt(time) * 1000
        date = new Date(time)
    }
    const formatObj = {
        y: date && date.getFullYear(),
        m: date && date.getMonth() + 1,
        d: date && date.getDate(),
        h: date && date.getHours(),
        i: date && date.getMinutes(),
        s: date && date.getSeconds(),
        a: date && date.getDay()
    }
    const time_str = format.replace(/{(y|m|d|h|i|s|a)+}/g, (result, key) => {
        let value = formatObj[key]
        if (key === 'a') return ['一', '二', '三', '四', '五', '六', '日'][value - 1]
        if (result.length > 0 && value < 10) {
            value = '0' + value
        }
        return value || 0
    })
    return time_str
}

export const formatSeconds = (value) => {
    var theTime = parseInt(value)// 秒
    var theTime1 = 0// 分
    var theTime2 = 0// 小时
    if (theTime > 60) {
        theTime1 = parseInt(theTime / 60)
        theTime = parseInt(theTime % 60)
        if (theTime1 > 60) {
            theTime2 = parseInt(theTime1 / 60)
            theTime1 = parseInt(theTime1 % 60)
        }
    }

    var result = '' + parseInt(theTime)// 秒
    if (theTime < 10 > 0) {
        result = '0' + parseInt(theTime)// 秒
    } else {
        result = '' + parseInt(theTime)// 秒
    }

    if (theTime1 < 10 > 0) {
        result = '0' + parseInt(theTime1) + ':' + result// 分，不足两位数，首位补充0，
    } else {
        result = '' + parseInt(theTime1) + ':' + result// 分
    }
    if (theTime2 > 0) {
        result = '' + parseInt(theTime2) + ':' + result// 时
    }
    return result
}
