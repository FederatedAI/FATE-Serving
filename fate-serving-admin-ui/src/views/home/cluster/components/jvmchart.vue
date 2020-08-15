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
            this.JVMseries = []
            var xDate = []
            var Memory = []
            var fullGcCount = []
            var yongGcCount = []
            var yongGcTime = []
            var fullGcTime = []
            var threadCount = []
            var Yname = ''
            this.callsData.JvmData.length > 0 && this.callsData.JvmData.forEach((item, index) => {
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
            var colorArr = []
            if (this.callsData.JVMInfo === 0) {
                this.JVMseries = []
                Yname = 'mb'
                // jvmrArr = ['old', 'eden', 'heap', 'nonHeap', 'survivor']
                jvmrArr = [{ name: 'old', textStyle: { color: '#4AA2FF' } },
                    { name: 'eden', textStyle: { color: '#00C99E' } },
                    { name: 'heap', textStyle: { color: '#FF9D00' } },
                    { name: 'nonHeap', textStyle: { color: '#FE6363' } },
                    { name: 'survivor', textStyle: { color: '#AD81EF' } }]
                colorArr = ['#4AA2FF', '#00C99E', '#FF9D00', '#FE6363', '#AD81EF']
                for (var a = 0; a < jvmrArr.length; a++) {
                    var pam = []
                    for (var q = 0; q < Memory.length; q++) {
                        pam.push(Memory[q][a])
                    }
                    pam = pam.map(item => {
                        return (item / 1024 / 1024).toFixed(2)
                    })
                    this.JVMseries.push({
                        name: jvmrArr[a].name,
                        data: pam,
                        type: 'line',
                        symbol: 'circle',
                        symbolSize: 4,
                        lineStyle: {
                            color: colorArr[a]
                        },
                        itemStyle: {
                            color: colorArr[a]
                        }
                    })
                }
            } else if (this.callsData.JVMInfo === 1) {
                this.JVMseries = []
                Yname = ''
                jvmrArr = [{ name: 'fullGcCount', textStyle: { color: '#4AA2FF' } },
                    { name: 'yongGcCount', textStyle: { color: '#00C99E' } }]
                this.JVMseries = [{
                    name: 'fullGcCount',
                    data: fullGcCount,
                    type: 'line',
                    symbol: 'circle',
                    symbolSize: 4,
                    lineStyle: {
                        color: '#4AA2FF'
                    },
                    itemStyle: {
                        color: '#4AA2FF'
                    }
                },
                {
                    name: 'yongGcCount',
                    data: yongGcCount,
                    type: 'line',
                    symbol: 'circle',
                    symbolSize: 4,
                    lineStyle: {
                        color: '#00C99E'
                    },
                    itemStyle: {
                        color: '#00C99E'
                    }
                }]
            } else if (this.callsData.JVMInfo === 2) {
                this.JVMseries = []
                Yname = 'ms'
                jvmrArr = [{ name: 'yongGcTime', textStyle: { color: '#4AA2FF' } },
                    { name: 'fullGcTime', textStyle: { color: '#00C99E' } }]
                this.JVMseries = [{
                    name: 'yongGcTime',
                    data: yongGcTime,
                    type: 'line',
                    symbol: 'circle',
                    symbolSize: 4,
                    lineStyle: {
                        color: '#4AA2FF'
                    },
                    itemStyle: {
                        color: '#4AA2FF'
                    }
                },
                {
                    name: 'fullGcTime',
                    data: fullGcTime,
                    type: 'line',
                    symbol: 'circle',
                    symbolSize: 4,
                    lineStyle: {
                        color: '#00C99E'
                    },
                    itemStyle: {
                        color: '#00C99E'
                    }
                }]
            } else if (this.callsData.JVMInfo === 3) {
                this.JVMseries = []
                Yname = ''
                this.JVMseries = [{
                    name: 'threadCount',
                    data: threadCount,
                    type: 'line',
                    symbol: 'circle',
                    symbolSize: 4,
                    lineStyle: {
                        color: '#4AA2FF'
                    },
                    itemStyle: {
                        color: '#4AA2FF'
                    }
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
