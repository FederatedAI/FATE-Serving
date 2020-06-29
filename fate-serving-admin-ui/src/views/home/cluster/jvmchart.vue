<template>
  <div class="echart-Jvm">
    <div ref="callsLine" class="echarts" style="height: 650px;" />
  </div>
</template>

<script>
import echarts from 'echarts'
import moment from 'moment'
export default {
    name: 'EchartJvm',
    props: {
        callsData: {
            type: Object,
            default() {
                return {}
            }
        }
    },
    data() {
        return {
            echarts,
            echartInstance: null,
            options: {},
            JVMseries: []
        }
    },
    watch: {
        callsData: function() {
            // const xDateArr = [] // X轴数组
            // const providerCalls = [] // 提供方数据
            // const userCalls = [] // 应用方数据
            // this.callsData.providerDayList.forEach(item => {
            //     xDateArr.push(item.formatDs)
            //     providerCalls.push(item.reqDay)
            // })
            // this.callsData.userDayList.forEach(item => {
            //     userCalls.push(item.reqDay)
            // })
            // this.options.xAxis.data = xDateArr
            // this.options.series[0].data = userCalls
            const JvmData = this.callsData.JvmData
            const JVMInfo = this.callsData.JVMInfo
            this.JVMseries = []
            var xDate = []
            var Memory = []
            var fullGcCount = []
            var yongGcCount = []
            var yongGcTime = []
            var fullGcTime = []
            var threadCount = []
            var Yname = ''
            JvmData.length > 0 && JvmData.forEach((item, index) => {
                if (item) {
                    xDate.push(this.date(item.timestamp))
                    Memory.push([item.old.used, item.eden.used, item.heap.used, item.nonHeap.used, item.survivor.used])
                    fullGcCount.push(item.fullGcCount)
                    yongGcCount.push(item.yongGcCount)
                    yongGcTime.push(item.yongGcTime)
                    fullGcTime.push(item.fullGcTime)
                    threadCount.push(item.threadCount)
                }
            })
            var jvmrArr = []
            if (JVMInfo === 0) {
                this.JVMseries = []
                Yname = 'mb'
                jvmrArr = ['old', 'eden', 'heap', 'nonHeap', 'survivor']
                for (var a = 0; a < jvmrArr.length; a++) {
                    var pam = []
                    for (var q = 0; q < Memory.length; q++) {
                        pam.push(Memory[q][a])
                    }
                    pam = pam.map(item => {
                        return (item / 1024 / 1024).toFixed(2)
                    })
                    this.JVMseries.push({
                        name: jvmrArr[a],
                        data: pam,
                        type: 'line'
                    })
                }
            } else if (JVMInfo === 1) {
                this.JVMseries = []
                Yname = ''
                jvmrArr = ['fullGcCount', 'yongGcCount']
                this.JVMseries = [{
                    name: 'fullGcCount',
                    data: fullGcCount,
                    type: 'line'
                },
                {
                    name: 'yongGcCount',
                    data: yongGcCount,
                    type: 'line'
                }]
            } else if (JVMInfo === 2) {
                this.JVMseries = []
                Yname = 'ms'
                jvmrArr = ['yongGcTime', 'fullGcTime']
                this.JVMseries = [{
                    name: 'yongGcTime',
                    data: yongGcTime,
                    type: 'line'
                },
                {
                    name: 'fullGcTime',
                    data: fullGcTime,
                    type: 'line'
                }]
            } else if (JVMInfo === 3) {
                this.JVMseries = []
                Yname = ''
                this.JVMseries = [{
                    name: 'threadCount',
                    data: threadCount,
                    type: 'line'
                }]
            }
            this.options = {
                xAxis: {
                    type: 'category',
                    data: xDate,
                    boundaryGap: false,
                    axisLine: {
                        color: 'blur', //
                        lineStyle: {
                            type: 'solid',
                            color: '#848C99', // y轴的颜色
                            width: '3'// y坐标轴线的宽度
                        }
                    },
                    axisLabel: {
                        textStyle: {
                            fontSize: 12
                        }
                    }
                },
                tooltip: {
                    trigger: 'axis'
                },
                legend: {
                    right: '150px',
                    orient: 'horizontal',
                    top: 10,
                    data: jvmrArr,
                    textStyle: {
                        fontSize: 13
                    }
                },
                yAxis: {
                    type: 'value',
                    name: Yname,
                    axisLine: {
                        color: 'blur', //
                        lineStyle: {
                            type: 'solid',
                            color: '#848C99', // y轴的颜色
                            width: '1'// y坐标轴线的宽度
                        },
                        textStyle: {
                            fontSize: 12
                        }

                    },
                    axisLabel: {
                        margin: 2,
                        textStyle: {
                            fontSize: 12
                        }
                    }
                },
                series: this.JVMseries
            }
            this.initChart()
        }

    },
    mounted() {
    // this.initChart()
    },
    methods: {
        initChart() {
            this.echartInstance = this.echarts.init(this.$refs.callsLine)
            window.addEventListener('resize', this.resize)
            this.echartInstance.setOption(this.options, true)
        },
        resize() {
            this.echartInstance.resize()
        },
        date(value) { // 时间格式函数
            return value ? moment(value).format('HH:mm:ss') : '--'
        }
    }
}
</script>

<style rel="stylesheet/scss" lang="scss">
</style>
