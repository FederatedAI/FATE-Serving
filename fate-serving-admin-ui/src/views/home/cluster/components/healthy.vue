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
<div class="checkup">
    <el-dialog
            title="Cluster Checkup Wizard"
            custom-class="pipeline-dialog checkup-dialog"
            :visible.sync="healthyVisible"
            width="700px"
        >
            <div class="healthy-content">
                <div class="healthy-top">
                    <div>
                        <span v-if="checkupStatus === 1" class="healthy-status"><i class="el-icon-success"></i>Cluster is healthy.</span>
                        <span v-if="checkupStatus === 2" class="healthy-status"><i class="el-icon-loading" ></i><span class="span">Checkuping</span></span>
                        <span v-if="checkupStatus === 3" class="healthy-status" style="color:#FE6363"><i class="el-icon-error"></i><span class="span">Error occurs !</span></span>
                        <span v-if="checkupStatus === 4" class="healthy-status" style="color:#FF9D00"><i class="el-icon-warning"></i><span class="span">Warn</span></span>
                        <span class="healthy-time">Last checkup：{{ HealthData.timestamp | datefrom}}</span>
                    </div>
                    <el-button class="healthy-but" :disabled="checkup" :style="checkup ? 'background-color:#B8BFCC' : 'background-color:#217AD9'" @click="startCheckup">Start Checkup</el-button>
                </div>
                  <div class="healthy-bottom">
                    <div class="healthy-item" v-if="HealthData && HealthData.proxy">
                            <i v-if="proxypercentage !== 100" class="el-icon-loading"/>
                            <i v-else-if="errorFlag" class="el-icon-error"/>
                            <i v-else-if="warnFlag" class="el-icon-warning"/>
                            <i v-else-if="okFlag" class="el-icon-success"/>
                            <i v-else class="no-deta"/>
                        <span>Serving Proxy</span>
                        <el-progress :percentage="proxypercentage" :show-text="false" color="#217AD9"></el-progress>
                        <i class="el-icon-arrow-down" :class="proxyCK ? 'active-down' : ''" @click="proxyCK = !proxyCK"></i>
                       <div v-show="proxyCK">
                            <div class="healthy-run" v-for="(item,i) in proxyStatus" :key="i">
                                <i v-if="proxypercentage !== 100 && (((100 / proxyStatus.length) * (i + 1)) >= proxypercentage)" class="el-icon-loading"/>
                                <i v-else-if="item.healthCheckStatus === 'ok'" class="el-icon-success"/>
                                <i v-else-if="item.healthCheckStatus === 'warn'" class="el-icon-warning"/>
                                <i v-else-if="item.healthCheckStatus === 'error'" class="el-icon-error"/>
                                <span :title="item.ip + ' : ' + item.checkItemName" class="check-item">{{item.ip + ' : ' + item.checkItemName}}</span>
                                <!-- <span v-if="item.healthCheckStatus === 'ok'">Check up passed!</span> -->
                                <span :title="item.msg" class="check-msg">{{ item.msg }}</span>
                            </div>
                       </div>
                    </div>
                     <div class="healthy-item" v-if="HealthData && HealthData.serving">
                            <i v-if="checkupStatus === 2 " class="el-icon-loading"/>
                            <i v-else-if="SerrorFlag" class="el-icon-error"/>
                            <i v-else-if="SwarnFlag" class="el-icon-warning"/>
                            <i v-else-if="SokFlag" class="el-icon-success"/>
                            <i v-else class="no-deta"/>
                        <span style="margin-right:23px">Serving Server</span>
                        <el-progress :percentage="servingpercentage" :show-text="false" color="#217AD9"></el-progress>
                        <i class="el-icon-arrow-down" :class="serverCK ? 'active-down' : ''" @click="serverCK = !serverCK"></i>
                        <div v-show="serverCK">
                            <div class="healthy-run" v-for="(item,i) in servingStatus" :key="i">
                                <i v-if="servingpercentage !== 100 && (((100 / servingStatus.length) * (i + 1)) >= servingpercentage)" class="el-icon-loading"/>
                                <i v-else-if="item.healthCheckStatus === 'ok'" class="el-icon-success"/>
                                <i v-else-if="item.healthCheckStatus === 'warn'" class="el-icon-warning"/>
                                <i v-else-if="item.healthCheckStatus === 'error'" class="el-icon-error"/>
                                <span :title="item.ip + ' : ' + item.checkItemName"  class="check-item">{{item.ip + ' : ' + item.checkItemName}}</span>
                                <!-- <span v-if="item.healthCheckStatus === 'ok'">Check up passed!</span> -->
                                <span :title="item.msg"  class="check-msg">{{ item.msg }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import { selfCheck } from '@/api/cluster'
import moment from 'moment'
export default {
    name: 'Healthy',
    filters: {
        datefrom(value) {
            return value ? moment(value).format('YYYY-MM-DD HH:mm:ss') : '--'
        }
    },
    props: {
        HealthData: {
            type: Object,
            default() {
                return {}
            }
        }
    },
    data() {
        return {
            checkupStatus: 1,
            checkup: false,
            proxypercentage: 100,
            servingpercentage: 100,
            healthyVisible: false,
            proxyCK: false,
            serverCK: false,
            clusterTimer: null,
            proxyStatus: [],
            servingStatus: [],
            errorFlag: null,
            warnFlag: null,
            okFlag: null,
            SerrorFlag: null,
            SwarnFlag: null,
            SokFlag: null,
            pData: '',
            sData: ''
        }
    },
    watch: {
        healthyVisible: {
            handler: function(val) {
                if (val) {
                    this.initHealthData()
                }
            },
            immediate: true,
            deep: true
        }
    },
    created() {
        setTimeout(() => {
            this.initHealthData()
            this.$emit('checkup', this.checkupStatus)
        }, 100)
    },
    methods: {
        initHealthData() {
            this.proxyStatus = []
            this.servingStatus = []
            this.errorFlag = 0
            this.okFlag = 0
            this.warnFlag = 0
            this.SerrorFlag = 0
            this.SokFlag = 0
            this.SwarnFlag = 0
            this.pData = ''
            this.sData = ''
            for (let key in this.HealthData) {
                if (key === 'proxy') {
                    for (let key2 in this.HealthData[key]) {
                        this.pData = key2
                        for (let key3 in this.HealthData[key][key2]) {
                            this.HealthData[key][key2][key3].forEach(element => {
                                element.ip = key2
                            })
                            this.errorFlag += this.HealthData[key][key2].errorList && this.HealthData[key][key2].errorList.length
                            this.okFlag += this.HealthData[key][key2].okList && this.HealthData[key][key2].okList.length
                            this.warnFlag += this.HealthData[key][key2].warnList && this.HealthData[key][key2].warnList.length
                            this.proxyStatus = this.proxyStatus.concat(this.HealthData[key][key2][key3])
                        }
                    }
                } else if (key === 'serving') {
                    for (let key2 in this.HealthData[key]) {
                        this.sData = key2
                        for (let key3 in this.HealthData[key][key2]) {
                            this.HealthData[key][key2][key3].forEach(element => {
                                element.ip = key2
                            })
                            this.SerrorFlag += this.HealthData[key][key2].errorList && this.HealthData[key][key2].errorList.length
                            this.SokFlag += this.HealthData[key][key2].okList && this.HealthData[key][key2].okList.length
                            this.SwarnFlag += this.HealthData[key][key2].warnList && this.HealthData[key][key2].warnList.length
                            this.servingStatus = this.servingStatus.concat(this.HealthData[key][key2][key3])
                        }
                    }
                }
            }
            if (this.servingpercentage === 100) {
                if (this.errorFlag || this.SerrorFlag) {
                    this.checkupStatus = 3
                } else if (this.warnFlag || this.SwarnFlag) {
                    this.checkupStatus = 4
                } else if (this.okFlag || this.SokFlag || !this.sData || !this.pData) {
                    this.checkupStatus = 1
                }
                this.$emit('checkup', this.checkupStatus)
            }
        },

        startCheckup() {
            this.checkup = true
            this.checkupStatus = 2
            this.servingpercentage = 0
            this.proxypercentage = 0
            this.$emit('checkup', this.checkupStatus)
            selfCheck().then(res => {
                this.clusterTimer = setInterval(() => {
                    this.$emit('checkup', this.checkupStatus, res.data)
                    this.initHealthData()
                    if (this.proxypercentage < 100) {
                        this.proxypercentage += 1
                    } else if (this.servingpercentage < 100) {
                        this.servingpercentage += 1
                    } else {
                        clearInterval(this.clusterTimer)
                        this.checkup = false
                        this.initHealthData()
                        this.$emit('checkup', this.checkupStatus, res.data)
                    }
                }, 50)
            })
        }
    }
}
</script>

<style rel="stylesheet/scss" lang="scss">
.checkup {
    .el-dialog__wrapper {
        z-index: 9999999 !important;
    }
  .checkup-dialog {
    margin-top: 22vh !important;

}
}

.healthy-content {
    .no-deta {
        display: inline-block;
        width: 18px;
    }
    .healthy-top {
        width: 628px;
        height: 96px;
        background: #FAFBFC;
        display: flex;
        padding: 12px 24px 12px 12px;
        box-sizing: border-box;
        justify-content: space-between;
        .healthy-status {
             color: #4E5766;
             display: block;
            font-size: 24px;
            margin-bottom: 15px;
            i {
                font-size: 36px;
                vertical-align: -4px;
                margin-right: 5px;
            }
        }
        .healthy-time {
            font-size: 14px;
            font-weight: 400;
            color: #848C99;
        }
        .healthy-but {
            background: #217AD9;
            width: 160px;
            height: 36px;
            color: #fff;
            border: none;
            border-radius: 0px;
            margin-top: 18px;
        }
    }
     .healthy-bottom {
         font-size: 18px;
         margin-top: 24px;
         .healthy-item {
             margin-bottom: 20px;
         }
         .check-item,.check-msg {
             display: inline-block;
             width: 240px;
            white-space: nowrap;
            text-overflow: ellipsis;
            overflow: hidden;
         }
         .check-msg {
             width: 280px;
         }
         span {
            margin: 0px 30px 0 15px;
         }
         .el-progress-bar__inner,.el-progress-bar__outer {
             border-radius: 0;
         }
         .el-progress-bar {
             padding-right: 20px;
         }
        .el-progress {
            position: relative;
            width: 437px;
            line-height: 1;
            display: inline-block;
        }
       .el-progress__text {
            display: none;
        }
        .el-icon-arrow-down {
            cursor: pointer;
            color: #B8BFCC;
            transition: transform .3s;
            vertical-align: middle;
        }
        .active-down {
            transform: rotate(180deg);
        }
        .healthy-run {
            margin:15px 0 0 33px;
            transition: all .3s;

            span {
                color: #848C99;
                font-size: 14px;
                vertical-align:-2px;
                margin-right: 10px;

            }
        }

     }
       .el-icon-error {
            color: #FE6363;
        }
        .el-icon-warning {
            color: #FF9D00;
        }
        .el-icon-success,.el-icon-loading {
            color: #217AD9;
        }

}
</style>
