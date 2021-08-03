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
        <div class="button-contral">
            <el-button class="add" type="primary" @click="openModify">Add</el-button>
        </div>
        <el-table
            :data="routerTableData"
            :header-cell-style="{background:'#fff'}"
            style="width: 100%;margin-bottom: 20px;"
            max-height="668px"
            class="table"
            @expand-change="setArrow"
            ref="refTable"
        >
            <el-table-column type="expand">
                <template slot-scope="scope">
                    <el-table :data="scope.row.routerList" style="width: calc(100% - 70px)" id="two-list">
                        <el-table-column width="200" prop="index" label="Index">
                            <template slot-scope="scope">
                                <span>{{scope.$index + 1}}</span>
                            </template>
                        </el-table-column>
                        <el-table-column width="200" prop="serverType" label="Type"></el-table-column>
                        <el-table-column prop="Network Access" label="Network Access">
                            <template slot-scope="scope">
                                <span>{{ scope.row.ipPort }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column
                            prop="useSSL"
                            label="Certificate"
                            width="200"
                            sortable
                            :sort-method="(a, b) => { return sortMix(a, b, 'useSSL') }">
                            <template slot-scope="scope">
                                <span>{{ scope.row.useSSL ? 'Configured' : 'Unconfigured' }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column width="200" prop="certChainFile" label="Certificate Path"></el-table-column>
                    </el-table>
                </template>
            </el-table-column>
            <el-table-column width="120" prop="$index" label="Index" show-overflow-tooltip>
                <template slot-scope="scope">
                    <span>{{ scope.$index + 1 }}</span>
                </template>
            </el-table-column>
            <el-table-column
                width="200"
                prop="partyId"
                label="PartyID"
                sortable
                :sort-method="(a, b) => { return sortMix(a, b, 'partyId') }"
                show-overflow-tooltip
            >
                <template slot-scope="scope">
                    <span>{{ scope.row.partyId }}</span>
                </template>
            </el-table-column>
            <el-table-column width="280" prop="networkAccess" label="Network Access">
                <template slot-scope="scope">
                    <span class="access-count" :class="{'up':scope.row.up === '2'}" @click="openList(scope.row)">
                        <span>{{scope.row.count}}</span>
                        <i v-if="scope.row.count > 0" class="el-icon-caret-bottom"></i>
                    </span>

                </template>
            </el-table-column>
            <el-table-column
                width="280"
                prop="createTime"
                sortable
                :sort-method="(a, b) => { return sortMix(a, b, 'createTime') }"
                label="Create Time"
            >
                <template slot-scope="scope">
                    <span>{{ scope.row.createTime | dateform }}</span>
                </template>
            </el-table-column>
            <el-table-column
                width="280"
                prop="updateTime"
                sortable
                :sort-method="(a, b) => { return sortMix(a, b, 'updateTime') }"
                label="updateTime">
                <template slot-scope="scope">
                    <span>{{ scope.row.updateTime | dateform }}</span>
                </template>
            </el-table-column>
            <el-table-column width="100" label="Operation">
                <template slot-scope="scope">
                    <el-button
                        type="text"
                        style="font-size: 18px"
                        class="edit"
                        size="mini"
                        @click="openModify(scope.row,scope.$index)"
                    ></el-button>
                    <el-button
                        type="text"
                        style="font-size: 18px"
                        class="delete"
                        :class="{'disable':scope.row.partyId === 'default'}"
                        :disabled="scope.row.partyId === 'default'"
                        size="mini"
                        @click="delete(scope.row)"
                    ></el-button>
                </template>
            </el-table-column>
        </el-table>
        <div class="pagination" v-if="page.total > 0">
            <el-pagination
                background
                @current-change="handleCurrentChange"
                :current-page.sync="page.currentPage"
                :page-size="page.size"
                layout="total, prev, pager, next, jumper"
                :total="page.total"
            ></el-pagination>
        </div>

        <div class="page-contral">
            <el-button
                type="primary"
                @click="modifyRouter = false"
                style="background:#B8BFCC;border-radius:0"
            >Cancel</el-button>
            <el-button type="primary" class="save-page" :class="{'disable':!hasChange}" :disabled="!hasChange" @click="saveSureDialog = true">Save</el-button>
        </div>

        <el-dialog
            :visible.sync="modifyRouter"
            :showClose="cancel"
            :title="modelType === 'add' ? 'Add' : 'Edit'"
            class="modifyModel"
            width="45%"
            top="10%"
            center>
            <div class="party-input">
                <span class="party-input-title" style="color:#217AD9;font-size:18px;">PartyID</span>
                <el-input v-model.number="modifyData.partyId" :class="{'disable':modelType === 'edit'}" :disabled="modelType === 'edit'"></el-input>
            </div>
            <div class="table-title-line">
                <span>Network Access Info</span>
                <span class="add-router-button" @click="addNewRouter"><i class="el-icon-circle-plus"></i>add router</span>
            </div>

           <el-table :data="modifyData.routerList" style="width: 100%" id="modify-table">
                <el-table-column width="60" prop="index" label="Index">
                    <template slot-scope="scope">
                        <span>{{scope.$index + 1}}</span>
                    </template>
                </el-table-column>
                <el-table-column width="120" prop="serverType" label="Type">
                    <template slot-scope="scope">
                        <el-select class="sel-role input-placeholder" v-model="scope.row.serverType" placeholder="">
                            <el-option key="default" label="Default" value="default"></el-option>
                            <el-option key="serving" label="Serving" value="serving"></el-option>
                        </el-select>
                    </template>
                </el-table-column>
                <el-table-column prop="Network Access" label="Network Access">
                    <template slot-scope="scope">
                        <el-input clearable v-model="scope.row.ipPort"></el-input>
                    </template>
                </el-table-column>
                <el-table-column
                    prop="useSSL"
                    label="Certificate"
                    width="150">
                    <template slot-scope="scope">
                        <el-switch
                            v-model="scope.row.useSSL"
                            active-color="#217AD9"
                            inactive-color="#E6EBF0"
                            active-text=""
                            inactive-text=""
                            @change="setCertificate(scope.row)">
                        </el-switch>
                    </template>
                </el-table-column>
                <el-table-column width="150" prop="certChainFile" label="Certificate Path">
                    <template slot-scope="scope">
                        <el-input clearable v-model="scope.row.certChainFile" :value="scope.row.certChainFile" :disabled="!scope.row.useSSL"></el-input>
                    </template>
                </el-table-column>
                <el-table-column width="90" label="Operation" align="center">
                <template slot-scope="scope">
                    <el-button
                        type="text"
                        style="font-size: 18px"
                        class="delete"
                        :class="{'disable':scope.row.partyId === 'default'}"
                        :disabled="scope.row.partyId === 'default'"
                        size="mini"
                        @click="deleteRouter(scope.$index)"
                    ></el-button>
                </template>
            </el-table-column>
            </el-table>

            <span slot="footer" class="dialog-footer dialog-but">
                <el-button type="primary" :disabled="!modifyData.partyId" @click="saveLocalRouter">OK</el-button>
                <el-button
                    type="primary"
                    @click="modifyRouter = false"
                    style="background:#B8BFCC;border: 1px solid #B8BFCC"
                >Cancel</el-button>
            </span>
        </el-dialog>
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
import { queryRouterList, addRouter, updateRouter, deleteRouter } from '@/api/cluster'
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
    data() {
        return {
            modifyRouter: false,
            cancel: false,
            cancelLeave: false,
            routerTableData: [],
            modelType: 'add',
            next: '',
            modifyData: {
                partyId: '',
                routerList: []
            },
            nowModelDataIndex: 0,
            hasChange: false,
            saveSureDialog: false,
            sureLeaveDialog: false,
            page: {
                currentPage: 1,
                size: 10,
                total: 0
            }
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
    methods: {
        sureLeave() {
            this.routerTableData = this.deepClone(this.cacheRouterTableData)
            this.hasChange = false
            this.sureLeaveDialog = false
            this.$emit('tabipInfo', this.next)
        },
        handleCurrentChange(val) {
            this.page.currentPage = val
            this.tabipInfo(this.ipInfo)
        },
        getTableData() {
            // 特异查询重置页码
            if (this.routerPartyID) {
                this.page.currentPage = 1
            }
            const param = {
                'serverHost': this.ipPort[0],
                'serverPort': this.ipPort[1],
                'page': this.page.currentPage,
                'pageSize': this.page.size,
                'routerTableList': [
                    {
                        'partyId': this.routerPartyID
                    }
                ]
            }
            queryRouterList(param).then(res => {
                this.page.total = res.data.total
                this.routerTableData = res.data.rows
                this.routerTableData.map(item => {
                    item.up = '1'
                    item.routerList.map(val => {
                        val.ipPort = val.ip ? `${val.ip}:${val.port}` : ''
                    })
                })
                this.cacheRouterTableData = this.deepClone(this.routerTableData)
                console.log(this.routerTableData, 'routerTableData')
                console.log(this.cacheRouterTableData, 'routerTableData')
            })
        },
        openList(row) {
            this.$refs.refTable.toggleRowExpansion(row)
        },
        sortMix(a, b, key) {
            if (Object.prototype.toString.call(a) === '[object Array]') {
                a = a[key][0] + ''
                b = b[key][0] + ''
            } else {
                a = a[key] + ''
                b = b[key] + ''
            }
            a = (a.split('')[0] || '').charCodeAt()
            b = (b.split('')[0] || '').charCodeAt()

            return a - b < 0 ? -1 : 1
        },
        setArrow() {
            console.log(arguments, 'open')
            let row = arguments[0]
            this.$nextTick(() => {
                this.routerTableData.map(item => {
                    if (item.partyId === row.partyId) {
                        row.up = row.up === '2' ? '1' : '2'
                    } else {
                        item.up = '1'
                    }
                })
            })
            console.log(this.routerTableData, 'this.routerTableData')
        },
        initModify() {
            this.modifyData.partyId = ''
            this.modifyData.routerList = [{
                caFile: null,
                certChainFile: null,
                ip: '',
                negotiationType: null,
                port: '',
                privateKeyFile: null,
                serverType: 'default',
                useSSL: false,
                ipPort: ''
            }]
        },
        openModify(row, index) {
            this.modelType = row.partyId ? 'edit' : 'add'
            this.modifyRouter = true
            if (this.modelType === 'edit') {
                this.modifyData.partyId = row.partyId
                this.modifyData.routerList = row.routerList
                this.nowModelDataIndex = index
            } else {
                this.initModify()
            }
            console.log(this.modifyData, 'modifyData')
        },
        delete() {
            deleteRouter().then(res => {
                console.log(res, 'res-delete')
            })
        },
        deleteRouter(index) {
            console.log(index, 'deleteRouter-index')
            this.modifyData.routerList.splice(index, 1)
        },
        saveLocalRouter() {
            this.modifyData.routerList.map(item => {
                if (item.ipPort.length <= 0) {
                    this.$message.warning('The params Network Access is required')
                }
            })
            if (this.modelType === 'edit') {
                this.$set(this.routerTableData[this.nowModelDataIndex], 'routerList', this.modifyData.routerList)
                this.$set(this.routerTableData[this.nowModelDataIndex], 'count', this.modifyData.routerList.length)
            } else {
                this.routerTableData.push({
                    partyId: this.modifyData.partyId,
                    count: this.modifyData.routerList.length,
                    routerList: this.modifyData.routerList,
                    up: '1'
                })
                this.page.total++
                // this.$set(this.routerTableData[this.routerTableData.length], 'routerList', this.modifyData.routerList)
                // this.$set(this.routerTableData[this.routerTableData.length], 'partyId', this.modifyData.partyId)
            }
            console.log(this.routerTableData, 'this.routerTableData')
            this.modifyRouter = false
            if (JSON.stringify(this.cacheRouterTableData) !== JSON.stringify(this.routerTableData)) {
                this.hasChange = true
            }
            console.log(this.hasChange, 'haschange')
        },
        setCertificate(row) {
            if (!row.useSSL) {
                row.certChainFile = ''
            }
        },
        setIpPort(row) {
            console.log(row, 'setIpPort-row')
        },
        addNewRouter() {
            this.modifyData.routerList.push({
                caFile: null,
                certChainFile: null,
                ip: '',
                negotiationType: null,
                port: '',
                privateKeyFile: null,
                serverType: 'default',
                useSSL: false,
                ipPort: ''
            })
        },
        sureSave() {
            console.log(this.routerTableData, 'routerTableData')
            this.saveSureDialog = false
            if (this.modelType === 'add') {
                addRouter().then(res => {
                    console.log(res, 'res-addRouter')
                })
            } else {
                updateRouter().then(res => {
                    console.log(res, 'res-updateRouter')
                })
            }
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
        }
        // selectIP(item, index) {
        //     this.$emit('selectIP', item, index)
        // }
    }
}
</script>

<style rel="stylesheet/scss" lang="scss" scope>
    .button-contral{
        display: flex;
        justify-content: flex-start;
        width: 100%;
        margin: 20px 0 10px;
        .add{
            width: 100px;
            height: 36px;
            background: #217AD9;
            color:'#fff';
            border-radius: 0;
        }
    }
    .ip-info-Router{
        tbody{
            color: #848C99;
        }
        .el-table__expanded-cell{
            .el-table{
                margin: 10px 30px;
                background: #FAFBFC;
                tr,th {
                    background-color: transparent;
                }
            }
        }
        .el-table__expand-icon{
            display: none;
        }
        .el-table__expand-column{
            // display: none;
            width: 10px;
        }
        .pagination {
            bottom: 100px !important;
        }
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
        #center-gray{
            display: inline-block;
            margin: 5px 0 20px;
            font-size: 18px;
            font-family: Product Sans;
            font-weight: 400;
            line-height: 20px;
            color: #848C99;
        }
    }
    #two-list{
        .el-table__body-wrapper {
            height: unset !important;
        }

    }
    .access-count{
        span{
            color: #217AD9;
            margin-right: 16px;
        }
        i{
            cursor: pointer;
            font-size: 16px;
            transition: all .4s;
        }
    }
    .access-count.up{
        i{
            transform: rotate(-180deg);
        }
    }
</style>
