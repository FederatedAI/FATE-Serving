<template>
  <div class="echart">
    <div ref="callsLine" class="echarts" style="height: 400px;" />
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
            this.options = this.callsData
            this.initChart()
        }

    },
    mounted() {
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
