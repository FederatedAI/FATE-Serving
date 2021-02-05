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
  <div class="echart">
    <div ref="callsLine" class="echarts" />
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
            // this.echartInstance.setOption(this.options, true)
            this.echartInstance.setOption(this.options)
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
