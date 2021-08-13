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
    <div style="padding:1px 0">
        <vue-json-editor
            v-model="routerTableData"
            :showBtns="false"
            :mode="'code'"
            lang="en"
            @json-change="onJsonChange"
            @json-save="onJsonSave"
            />

        <div class="page-contral" v-if="changeAble">
            <!-- <el-button
                type="primary"
                @click="getTableData"
                style="background:#B8BFCC;border-radius:0"
            >Cancel</el-button> -->
            <el-button type="primary" class="save-page" :class="{'disable':!hasChange}" :disabled="!hasChange" @click="saveSureDialog = true">Save</el-button>
        </div>

        <el-dialog
            :visible.sync="saveSureDialog"
            :showClose="cancel"
            class="model-synchronize"
            width="37%"
            top="15%"
            center
        >
            <span>Are you sure to save changes?</span>
            <br>
            <span id="center-gray">Router info of the Serving-proxy will update.</span>
            <span slot="footer" class="dialog-footer dialog-but">
                <el-button type="primary" @click="sureSave">Sure</el-button>
                <el-button
                    type="primary"
                    @click="saveSureDialog = false"
                    style="background:#B8BFCC;border: 1px solid #B8BFCC"
                >Cancel</el-button>
            </span>
        </el-dialog>

        <el-dialog
            :visible.sync="sureLeaveDialog"
            :showClose="cancelLeave"
            class="model-synchronize"
            width="37%"
            top="15%"
            center
        >
            <span>Are you sure to leave this page?</span>
            <br>
            <span id="center-gray">Your changes will be lost if you do not save them.</span>
            <span slot="footer" class="dialog-footer dialog-but">
                <el-button type="primary" @click="sureLeave">Sure</el-button>
                <el-button
                    type="primary"
                    @click="sureLeaveDialog = false"
                    style="background:#B8BFCC;border: 1px solid #B8BFCC"
                >Cancel</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import { queryRouterList, saveRouter } from '@/api/cluster'
import vueJsonEditor from 'vue-json-editor'
export default {
    name: 'routerTable',
    props: {
        showRouterTable: {
            type: Boolean,
            default() {
                return false
            }
        },
        ipPort: {
            type: Array,
            default() {
                return []
            }
        },
        routerPartyID: {
            type: String,
            default() {
                return ''
            }
        }
    },
    components: { vueJsonEditor },
    data() {
        return {
            cancel: false,
            cancelLeave: false,
            routerTableData: {},
            next: '',
            hasChange: false,
            changeAble: false,
            saveSureDialog: false,
            sureLeaveDialog: false
        }
    },
    watch: {
        showRouterTable: {
            handler(newVal, oldVal) {
                console.log(arguments, 'showRouterTable')
                if (newVal === true) {
                    this.getTableData()
                }
            },
            immediate: true
        }
    },
    created() {
        var app = document.getElementById('app')
        app.removeChild(document.getElementByClassName('jsoneditor-poweredBy')[0])
    },
    methods: {
        onJsonChange() {
            console.log(arguments, 'onJsonChange')
            this.hasChange = true
        },
        onJsonSave() {
            console.log(arguments, 'onJsonSave')
        },
        sureLeave() {
            this.routerTableData = this.deepClone(this.cacheRouterTableData)
            this.hasChange = false
            this.sureLeaveDialog = false
            this.$emit('tabipInfo', this.next)
        },
        getTableData() {
            const param = {
                'serverHost': this.ipPort[0],
                'serverPort': this.ipPort[1],
                'routerTableList': [
                    {
                        'partyId': this.routerPartyID
                    }
                ]
            }
            queryRouterList(param).then(res => {
                this.routerTableData = JSON.parse(res.data.routerTable)
                this.changeAble = res.data.changeAble
                // this.routerTableData.map(item => {
                //     item.routerList.map(val => {
                //         val.ipPort = val.ip ? `${val.ip}:${val.port}` : ''
                //     })
                // })
                this.cacheRouterTableData = this.deepClone(this.routerTableData)
                this.hasChange = false
            })
        },
        sureSave() {
            console.log(this.routerTableData, 'routerTableData-save')
            // let routerTableList = this.routerTableData.map(item => {
            //     return item.routerList.map(k => {
            //         k = this.nullToStr(k)
            //         return {
            //             partyId: item.partyId,
            //             ...k
            //         }
            //     })
            // }).flat()
            if (!this.isJson(JSON.stringify(this.routerTableData))) {
                this.$message.warning({
                    message: 'Illegal parameter format',
                    'duration': 3000
                }, true)
            }
            console.log('验证通过！')
            this.saveSureDialog = false
            let param = {
                'serverHost': this.ipPort[0],
                'serverPort': this.ipPort[1],
                'routerTableList': this.routerTableData
            }
            saveRouter(param).then(res => {
                console.log(res, 'res-addRouter')
                if (res.retcode === 0 && res.retmsg === 'success') {
                    this.$message.success('Save success!')
                    this.getTableData()
                }
            })
        },
        cancelSave() {
            this.routerTableData = this.deepClone(this.cacheRouterTableData)
        },
        isJson(data) {
            console.log(data, 'isjosn-data')
            if (typeof data === 'string') {
                try {
                    var obj = JSON.parse(data)
                    if (typeof obj === 'object' && obj) {
                        return true
                    } else {
                        return false
                    }
                } catch (e) {
                    return false
                }
            }
        },
        nullToStr(data) {
            for (var x in data) {
                if (data[x] === null) { // 如果是null 把直接内容转为 ''
                    data[x] = ''
                }
            }
            return data
        },
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
        },
        updateView(row, index) {
            let ipArr = row.ipPort.split(':')
            row.ip = ipArr[0]
            row.port = ipArr[1]
            // this.$set(this.modifyData.routerList[index], 'ipPort', row.ipPort)
        }
        // selectIP(item, index) {
        //     this.$emit('selectIP', item, index)
        // }
    }
}
</script>

<style rel="stylesheet/scss" lang="scss" scope>
    .jsoneditor-vue{
        margin: 12px 0;
        height: 600px;
        .jsoneditor-poweredBy{
            display: none;
        }
        .jsoneditor-menu{
            button{
                outline: none;
            }
        }
    }
    .ip-info-Router{
        .page-contral{
            clear: both;
            background: #fff;
            button{
                float: right;
                width: 126px;
                height: 36px;
                border-radius: 0;
                border: 0;
                margin-left: 24px;
            }
            .save-page:not(.disable){
                background: #217AD9;
            }
        }
    }

</style>
