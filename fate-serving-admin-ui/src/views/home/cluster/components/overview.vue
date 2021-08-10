<!--
  Copyright 2019 The FATE Authors. All Rights Reserved.

  Licensed under the Apache License, Version 2.0 (the 'License');
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an 'AS IS' BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

 -->
<template>
    <!-- <ul>
        <li
            v-if="ArrProxy[0] && ArrProxy[0].children.length"
            class="proxy"
            :class="selected === 1 ? 'active' : ''"
            @click="tabNav(1,1)"
        >serving-proxy</li>
        <li v-else class="proxy disabled">serving-proxy</li>
        <li class="admin">admin</li>
        <li
            v-if="ArrServing[0] && ArrServing[0].children.length"
            class="serving"
            :class="selected === 2 ? 'active' : ''"
            @click="tabNav(2,1)"
        >serving-server</li>
        <li v-else class="serving disabled">serving-server</li>
        <li class="caret-l caret">
            <p></p>
            <i class="el-icon-caret-bottom"></i>
        </li>
        <li class="caret-r caret">
            <p></p>
            <i class="el-icon-caret-bottom"></i>
        </li>
        <li class="caret-c caret">
            <i class="el-icon-caret-left" />
            <p></p>
            <i class="el-icon-caret-right" />
        </li>
    </ul> -->
    <div class="echart-overview">
        <div ref="callsLine" v-show="!isLarger" class="echarts"  />
        <div ref="largeCallsLine" v-show="isLarger" class="echarts"></div>
    </div>
</template>

<script>
import echarts from 'echarts'
import _ from 'lodash'

// 图片路径先在外部拼接完成
let adminUrl = `image://${require('@/assets/admin_default.svg')}`
let proxyUrl = `image://${require('@/assets/proxy_default.svg')}`
let servingUrl = `image://${require('@/assets/server_default.svg')}`
let selectedProxyUrl = `image://${require('@/assets/proxy_select.svg')}`
let selectedServingUrl = `image://${require('@/assets/server_select.svg')}`
let symbolSize = 30
export default {
    name: 'overview',
    props: {
        chartArr: {
            type: Object,
            default() {
                return {}
            }
        },
        isLarger: {
            type: Boolean,
            default() {
                return false
            }
        }
    },
    data() {
        return {
            echarts,
            echartInstance: null,
            maxWith: 100,
            adminY: 0,
            proxyY: 0,
            servingY: 0,
            proxySpace: [0, -35],
            servingSpace: [0, 20],
            inLargeModel: false,
            selected: '',
            selectItem: '',
            smallGrig: {
                top: 0,
                bottom: 0,
                left: '2%',
                right: '2%'
            },
            largerGrig: {
                top: 0,
                bottom: 0,
                left: '12%',
                right: '12%'
            },
            options: {
                tooltip: {
                    formatter: (params) => {
                        return `${params.name}(${params.data.x},${params.data.y})`
                    }
                },
                animation: false,
                // selectedMode: 'single',
                // animationDurationUpdate: 1500,
                // animationEasingUpdate: 'quinticInOut',
                series: [
                    {
                        type: 'graph',
                        symbolSize: symbolSize,
                        roam: true,
                        label: {
                            show: true,
                            position: 'bottom',
                            color: '#217AD9',
                            offset: [0, 5],
                            fontSize: 10,
                            formatter: (params) => {
                                if (params.data.needFormatterLabel && !this.isLarger) {
                                    return `${params.name.slice(0, 10)}...`
                                }
                                return params.name
                            }
                        },
                        bottom: 10,
                        data: [],
                        // links: [],
                        links: [],
                        lineStyle: {
                            width: 1,
                            color: 'rgba(33, 122, 217, .3)',
                            offset: [0, 30],
                            label: {
                                show: false
                            }
                        }
                    }
                ]
            }
        }
    },
    watch: {
        chartArr: function(newVal, oldVal) {
            // this.options = this.overviewData
            if (JSON.stringify(newVal) !== JSON.stringify(oldVal)) {
                // console.log(newVal, 'charData')
                this.setOption()
            }
        },
        isLarger: function(newVal, oldVal) {
            if (JSON.stringify(newVal) !== JSON.stringify(oldVal)) {
                // console.log(newVal, 'charData')
                this.$nextTick(() => {
                    if (JSON.stringify(newVal) === 'true') {
                        this.setSize('large')
                        this.setOptionLarge()
                    } else {
                        this.setSize('small')
                        this.setOption()
                    }
                })
            }
        },
        selectItem: function(newVal, oldVal) {
            // console.log(newVal, 'selectItem')
            if (!newVal) return
            let name = newVal[0]
            let type = newVal[1]
            let data = JSON.parse(JSON.stringify(this.options.series[0].data))
            data.forEach((item, k) => {
                if (k === 0) return
                item.symbol = item.dataName === 'proxy' ? proxyUrl : servingUrl
                if (item.name === name) {
                    this.selected = k
                    item.symbol = type === 'proxy' ? selectedProxyUrl : selectedServingUrl
                }
            })
            this.$set(this.options.series[0], 'data', data)
            this.echartInstance.setOption(this.options)
            if (this.optionsLarge && this.optionsLarge.series[0]) {
                this.$set(this.optionsLarge.series[0], 'data', data)
                this.echartInstanceLarge.setOption(this.optionsLarge)
            }
        }
    },
    created() {
        this.$nextTick(() => {
            this.initChart()
        })
    },
    methods: {
        tabNav(e) {
            console.log(e, 'e')
            if (e.name !== 'admin') {
                let type = e.data.dataName
                let index = type === 'proxy' ? 1 : 2
                let selected = e.dataIndex
                let data = JSON.parse(JSON.stringify(this.options.series[0].data))
                let obj = {
                    index: index,
                    listIndex: e.data.listIndex
                }
                this.$emit('tabNav', obj)
                // 设置选中状态
                data.forEach((item, k) => {
                    if (k === 0) return
                    if (selected !== k) {
                        item.symbol = item.dataName === 'proxy' ? proxyUrl : servingUrl
                    } else {
                        data[selected].symbol = type === 'proxy' ? selectedProxyUrl : selectedServingUrl
                    }
                })
                // 记录选中序号，放大缩小后标记选中
                this.selected = selected
                this.$set(this.options.series[0], 'data', data)
                this.echartInstance.setOption(this.options)
                if (this.optionsLarge && this.optionsLarge.series[0]) {
                    this.$set(this.optionsLarge.series[0], 'data', data)
                    this.echartInstanceLarge.setOption(this.optionsLarge)
                }
            }
        },
        setSize(type) {
            if (type === 'large') {
                // this.$set(this.options.tooltip, 'show', false)
                this.$set(this.options.series[0].label, 'fontSize', 16)
                this.$set(this.options.series[0], 'symbolSize', 35)
                this.$set(this.proxySpace, 1, -45)
                if (this.options.series[0].data.length > 8) {
                    let { left, right } = this.largerGrig
                    this.$set(this.options.series[0], 'left', left)
                    this.$set(this.options.series[0], 'right', right)
                }
            } else {
                // this.$set(this.options.tooltip, 'show', true)
                this.$set(this.options.series[0].label, 'fontSize', 10)
                this.$set(this.options.series[0], 'symbolSize', 30)
                this.$set(this.proxySpace, 1, -35)
                delete this.options.series[0].left
                delete this.options.series[0].right
            }
        },
        initChart() {
            this.echartInstance = this.echarts.init(this.$refs.callsLine)
            this.echartInstance.setOption(this.options)
            window.addEventListener('resize', this.resize)
            this.echartInstance.on('click', this.tabNav)
        },
        initLarge() {
            // 放大改用svg渲染 避免模糊
            this.echartInstanceLarge = this.echarts.init(this.$refs.largeCallsLine, null, { renderer: 'svg' })
            this.optionsLarge = this.deepClone(this.options)
            this.echartInstanceLarge.setOption(this.optionsLarge)
            window.addEventListener('resize', this.resizeLarge)
            this.echartInstanceLarge.on('click', this.tabNav)
        },
        resize() {
            this.echartInstance.resize()
        },
        resizeLarge() {
            this.echartInstanceLarge.resize()
        },
        setOption() {
            let proxy = _.get(this, 'chartArr.proxy[0].children', [])
            let serving = _.get(this, 'chartArr.serving[0].children', [])
            // let proxy = [this.chartArr.proxy[0].children[0]] || []
            // let serving = [this.chartArr.serving[0].children[0]] || []
            let proxyArr = proxy.map(item => item.name)
            let servingArr = serving.map(item => item.name)
            if (proxyArr.length < 1 && servingArr.length < 1) return
            let diffLength = proxyArr.length - servingArr.length
            let longArrName = diffLength > 0 ? 'proxy' : 'serving'
            let shortArrName = diffLength > 0 ? 'serving' : 'proxy'
            let longArr = diffLength > 0 ? proxyArr : servingArr
            let shortArr = diffLength > 0 ? servingArr : proxyArr
            let longArrPostionArr = this.setLongArrPosition(longArr, longArrName)
            let shortArrPostionArr = this.setShortArrPosition(shortArr, shortArrName)
            let longLen = longArrPostionArr.length
            let shortLen = shortArrPostionArr.length
            let newSeriesData = []
            let newSeriesLink = []
            // 添加admin坐标
            let adminX = 150
            let center = ''
            // console.log(longArrPostionArr, 'longArrPostionArr')
            // console.log(shortArrPostionArr, 'shortArrPostionArr')
            if (longLen === 1) {
                adminX = longArrPostionArr[0].x
                if (shortArrPostionArr.length > 0) {
                    shortArrPostionArr[0].x = longArrPostionArr[0].x
                }
            } else if (longLen === shortLen) {
                longArrPostionArr.map((item, index) => {
                    shortArrPostionArr[index].x = item.x
                })
                adminX = this.setAdminPosition(longArrPostionArr)
            } else {
                if (longLen % 2 !== 0) {
                    console.log('set-admin-by-long')
                    center = ((longLen - 1) / 2) + 1
                    adminX = longArrPostionArr[center - 1].x
                } else if (shortLen % 2 !== 0) {
                    console.log('set-admin-by-short')
                    center = ((shortLen - 1) / 2) + 1
                    adminX = shortArrPostionArr[center - 1].x
                } else {
                    console.log('set-admin-by-width')
                    if (longLen > 2) {
                        adminX = this.setAdminPosition(longArrPostionArr)
                    } else if (shortLen > 2) {
                        adminX = this.setAdminPosition(shortArrPostionArr)
                    }
                }
            }

            let adminPosition = {
                name: 'admin',
                symbol: adminUrl,
                x: adminX,
                tooltip: {
                    show: false
                },
                y: this.adminY
            }
            if (longArrName === 'proxy') {
                newSeriesData = [adminPosition, ...longArrPostionArr, ...shortArrPostionArr]
                newSeriesLink = this.setOptionsLink(longArrPostionArr, shortArrPostionArr)
            } else {
                newSeriesData = [adminPosition, ...shortArrPostionArr, ...longArrPostionArr]
                newSeriesLink = this.setOptionsLink(shortArrPostionArr, longArrPostionArr)
            }
            console.log(newSeriesData, 'newSeriesData')
            // let xmap = newSeriesData.map(item => item.x)
            // console.log(xmap, 'xmap')
            if (this.selected) {
                console.log(this.selected, 'small-selected')
                newSeriesData[this.selected].symbol = newSeriesData[this.selected].dataName === 'proxy' ? selectedProxyUrl : selectedServingUrl
            } else {
                // 未手动选择过则设置默认选中第一个serving
                if (longArrName === 'proxy' && shortArrPostionArr[0]) {
                    this.selectItem = [shortArrPostionArr[0].name, shortArrPostionArr[0].dataName]
                } else if (longArrName === 'serving' && longArrPostionArr[0]) {
                    this.selectItem = [longArrPostionArr[0].name, longArrPostionArr[0].dataName]
                }
            }
            this.$set(this.options.series[0], 'data', newSeriesData)
            this.$set(this.options.series[0], 'links', newSeriesLink)
            this.initChart()
        },
        setOptionLarge() {
            // let proxy = [this.chartArr.proxy[0].children[0]] || []
            // let serving = [this.chartArr.serving[0].children[0]] || []
            let proxy = _.get(this, 'chartArr.proxy[0].children', [])
            let serving = _.get(this, 'chartArr.serving[0].children', [])
            let proxyArr = proxy.map(item => item.name)
            let servingArr = serving.map(item => item.name)
            if (proxyArr.length < 1 && servingArr.length < 1) return
            let diffLength = proxyArr.length - servingArr.length
            let longArrName = diffLength > 0 ? 'proxy' : 'serving'
            let shortArrName = diffLength > 0 ? 'serving' : 'proxy'
            let longArr = diffLength > 0 ? proxyArr : servingArr
            let shortArr = diffLength > 0 ? servingArr : proxyArr
            let longArrPostionArr = this.setLongArrPosition(longArr, longArrName)
            this.adminY = this.maxWith / 5 * 1.5
            this.proxyY = this.maxWith / 5 * 2
            this.servingY = this.maxWith / 5 * 2.5
            let shortArrPostionArr = this.setShortArrPosition(shortArr, shortArrName)
            let longLen = longArrPostionArr.length
            let shortLen = shortArrPostionArr.length
            let newSeriesData = []
            let newSeriesLink = []
            // 添加admin坐标
            let adminX = 500
            let center = ''
            if (longLen === 1) {
                adminX = longArrPostionArr[0].x
                if (shortArrPostionArr[0])shortArrPostionArr[0].x = longArrPostionArr[0].x
            } else if (longLen === shortLen) {
                longArrPostionArr.map((item, index) => {
                    shortArrPostionArr[index].x = item.x
                })
                adminX = this.setAdminPosition(longArrPostionArr)
            } else {
                if (longLen % 2 !== 0) {
                    center = ((longLen - 1) / 2) + 1
                    adminX = longArrPostionArr[center - 1].x
                } else if (shortLen % 2 !== 0) {
                    center = ((shortLen - 1) / 2) + 1
                    adminX = shortArrPostionArr[center - 1].x
                } else {
                    if (longLen > 2) {
                        adminX = this.setAdminPosition(longArrPostionArr)
                    } else if (shortLen > 2) {
                        adminX = this.setAdminPosition(shortArrPostionArr)
                    }
                }
            }

            let adminPosition = {
                name: 'admin',
                symbol: adminUrl,
                x: adminX,
                tooltip: {
                    show: false
                },
                y: this.adminY
            }
            if (longArrName === 'proxy') {
                newSeriesData = [adminPosition, ...longArrPostionArr, ...shortArrPostionArr]
                newSeriesLink = this.setOptionsLink(longArrPostionArr, shortArrPostionArr)
            } else {
                newSeriesData = [adminPosition, ...shortArrPostionArr, ...longArrPostionArr]
                newSeriesLink = this.setOptionsLink(shortArrPostionArr, longArrPostionArr)
            }
            if (this.selected) {
                console.log(this.selected, 'large-selected')
                newSeriesData[this.selected].symbol = newSeriesData[this.selected].dataName === 'proxy' ? selectedProxyUrl : selectedServingUrl
            }

            this.$set(this.options.series[0], 'data', newSeriesData)
            this.$set(this.options.series[0], 'links', newSeriesLink)
            this.initLarge()
        },
        setAdminPosition(arr) {
            let len = arr.length
            let arrLx = arr[(len / 2) - 1].x
            let arrRx = arr[(len / 2)].x
            // console.log((arrLx + arrRx) / 2)
            return (arrLx + arrRx) / 2
        },
        setLongArrPosition(arr, arrName) {
            let positionArr = []
            let len = arr.length
            if (len === 0) return positionArr
            let symbol = arrName === 'proxy' ? proxyUrl : servingUrl
            let symbolOffset = arrName === 'proxy' ? this.proxySpace : this.servingSpace
            // let maxWith = this.maxWith
            let x = 50
            let needFormatterLabel = arr.length > 3
            positionArr = arr.map((item, index) => {
                return {
                    name: item,
                    symbol: symbol,
                    symbolOffset: symbolOffset,
                    x: x + (index + 1) * 100,
                    // x: parseInt((maxWith / len) * (index + 1)),
                    needFormatterLabel: needFormatterLabel,
                    dataName: arrName,
                    listIndex: index
                }
            })
            this.maxWith = positionArr[positionArr.length - 1].x + 50
            this.adminY = (this.maxWith / 5) * 0.2
            this.proxyY = (this.maxWith / 5) * 1.8
            this.servingY = Math.min((this.maxWith / 5) * 2.5, this.adminY * 11)
            let y = arrName === 'proxy' ? this.proxyY : this.servingY
            positionArr.map(item => {
                item.y = y
            })
            // console.log(positionArr, 'positionArr')
            return positionArr
        },
        setShortArrPosition(arr, arrName) {
            let positionArr = []
            let symbol = arrName === 'proxy' ? proxyUrl : servingUrl
            let symbolOffset = arrName === 'proxy' ? this.proxySpace : this.servingSpace
            let y = arrName === 'proxy' ? this.proxyY : this.servingY
            let len = arr.length
            let needFormatterLabel = len > 3
            let maxWith = this.maxWith
            let space = maxWith / (len + 1)
            let x = 50
            // 根据长行计算的宽度边界值均布短行内容
            arr.map((item, index) => {
                x += space + 5
                positionArr.push({
                    name: item,
                    symbol: symbol,
                    symbolOffset: symbolOffset,
                    // x: parseInt((maxWith / (len + 1)) * (index + 1)) + 50,
                    x: x,
                    y: y,
                    needFormatterLabel: needFormatterLabel,
                    dataName: arrName,
                    listIndex: index
                })
            })
            // console.log(positionArr, 'positionArr')
            return positionArr || []
        },
        setOptionsLink(arr1, arr2) {
            if (arr1.length < 1 || arr2.length < 1) return []
            let link = Object.keys(arr1).map(i => {
                return Object.keys(arr2).map(k => {
                    return {
                        'source': +i + 1,
                        'target': +k + 1 + arr1.length
                    }
                })
            }).flat()
            return link
        },
        // 定义一个深拷贝函数  接收目标target参数
        deepClone(target) {
            // 定义一个变量
            let result
            // 如果当前需要深拷贝的是一个对象的话
            if (typeof target === 'object') {
                // 如果是一个数组的话
                if (Array.isArray(target)) {
                    result = [] // 将result赋值为一个数组，并且执行遍历
                    for (let i in target) {
                        // 递归克隆数组中的每一项
                        result.push(this.deepClone(target[i]))
                    }
                    // 判断如果当前的值是null的话；直接赋值为null
                } else if (target === null) {
                    result = null
                    // 判断如果当前的值是一个RegExp对象的话，直接赋值
                } else if (target.constructor === RegExp) {
                    result = target
                } else {
                    // 否则是普通对象，直接for in循环，递归赋值对象的所有值
                    result = {}
                    for (let i in target) {
                        result[i] = this.deepClone(target[i])
                    }
                }
                // 如果不是对象的话，就是基本数据类型，那么直接赋值
            } else {
                result = target
            }
            // 返回最终结果
            return result
        }
    }
}
</script>

<style>
</style>
