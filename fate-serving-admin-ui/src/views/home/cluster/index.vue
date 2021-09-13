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
    <div class="main" :class="{'larger':isLarger}">
        <div class="ip-selector">
            <div class="overview">
                <div class="cluster">Cluster
                        <span class="healthy"  @click="heal">
                        <span v-if="checkupStatus === 1"><i class="el-icon-success"></i><span class="span">Cluster is healthy.</span></span>
                        <span v-if="checkupStatus === 2"><i class="el-icon-loading"></i><span class="span">Checkuping</span></span>
                        <span v-if="checkupStatus === 3"><i class="el-icon-error"></i><span class="span"  style="color: #FE6363;">Error occurs !</span></span>
                        <span v-if="checkupStatus === 4" style="color:#FE6363"><i class="el-icon-warning"></i><span class="span" style="color: #FF9D00;">Warn</span></span>
                    </span>
                </div>
                <div class="enlarge" @click="setSize('1')" v-if="!isLarger"><img src="@/assets/zoom-in.png" alt=""></div>
                <div class="shrink" @click="setSize('2')" v-else><img src="@/assets/zoom-out.png" alt=""></div>
                <overview ref="overview" :chartArr="chartArr" :isLarger="isLarger" @tabNav="tabNav"/>
            </div>
            <div class="ip-list">
                <div class="ip-list-top">
                    <span
                        class="ip-list-tit"
                    >{{ selected === 1 ? 'serving-proxy' : 'serving-server' }}</span>
                    <span class="ip-list-des">Click IP to view the instance details</span>
                </div>
                <div class="ip-list-line"></div>
                <div class="ip-list-main">
                    <iplist :activeip="activeip" :ipData="ipData" @selectIP="selectIP"/>
                </div>
            </div>
        </div>
        <div class="ip-info">
            <div v-if="noSelectedData" class="ip-info-welcome">welcome</div>
            <div v-else>
                <el-popover placement="bottom" width="400" v-model="titvisible" popper-class="titpopover" trigger="click">
                    <div class="titpopover-main">
                        <div
                            class="titpopover-label"
                            v-for="(item, index) in ipData.children"
                            :key="index"
                            @click="selectIP(item, index)"
                        >{{item && item.label}}</div>
                    </div>
                    <div class="ip-info-tit" slot="reference">
                        {{ ipchildrenData && ipchildrenData.label }}
                        <i class="el-icon-caret-bottom"></i>
                    </div>
                </el-popover>
                <ul class="ul" :class="[0,1,4].includes(ipInfo) ? 'ulBasic' : ''">
                    <li :class="ipInfo === 0 ? 'activeInfo' : ''" @click="tabipInfo(0)">Basic</li>
                    <li
                        v-if="selected === 2"
                        :class="ipInfo === 1 ? 'activeInfo' : ''"
                        @click="tabipInfo(1)"
                    >Models</li>
                    <li
                        v-if="selected === 1"
                        :class="ipInfo === 4 ? 'activeInfo' : ''"
                        @click="tabipInfo(4)"
                    >Router</li>
                    <li :class="ipInfo === 2 ? 'activeInfo' : ''" @click="tabipInfo(2)">Traffic Monitor</li>
                    <li :class="ipInfo === 3 ? 'activeInfo' : ''" @click="tabipInfo(3)">JVM</li>
                </ul>
                <div class="ip-info-main" :class="ipInfo === 3 ?'info-main' : ''">
                    <div v-if="ipInfo === 0" class="ip-info-Basic">
                        <div class="search">
                            <el-input placeholder="Keyword" v-model="searchkey" @change="searchKeyword">
                                <el-button slot="prepend" icon="el-icon-search"></el-button>
                            </el-input>
                        </div>
                        <el-table
                            :data="basicData"
                            :header-cell-style="{background:'#fff'}"
                            style="width: 100%;margin-bottom: 20px;"
                            max-height="668px"
                            class="table"
                        >
                            <el-table-column sortable prop="key" label="key" show-overflow-tooltip>
                                <template slot-scope="scope">
                                    <span>{{ scope.row.key }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column sortable :sort-method="(a, b) => { return sortMix(a, b, 'value') }" prop="value" label="value" show-overflow-tooltip>
                                <template slot-scope="scope">
                                    <span>{{ scope.row.value }}</span>
                                </template>
                            </el-table-column>
                        </el-table>
                    </div>
                    <div v-if="ipInfo === 1" class="ip-info-Models">
                        <div class="search">
                            <el-input
                                placeholder="Service ID"
                                v-model="serviceid"
                                @change="searchServiceID"
                            >
                                <el-button slot="prepend" icon="el-icon-search"></el-button>
                            </el-input>
                        </div>
                        <el-table
                            :data="ModelsData"
                            :header-cell-style="{background:'#fff'}"
                            style="width: 100%;margin-bottom: 20px;"
                            max-height="668px"
                            class="table"
                        >
                            <el-table-column
                                width="280"
                                prop="namespace"
                                label="Model ID"
                                sortable
                                :sort-method="(a, b) => { return sortMix(a, b, 'namespace') }"
                                show-overflow-tooltip
                            >
                                <template slot-scope="scope">
                                    <span>{{ scope.row.namespace }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column
                                width="200"
                                prop="tableName"
                                label="Model Version"
                                sortable
                                :sort-method="(a, b) => { return sortMix(a, b, 'tableName') }"
                                show-overflow-tooltip
                            >
                                <template slot-scope="scope">
                                    <span>{{ scope.row.tableName }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column sortable width="120" :sort-method="(a, b) => { return sortMix(a, b, 'serviceIds') }" prop="serviceId" label="Service ID">
                                <template slot-scope="scope">
                                    <el-popover
                                        v-if="scope.row.serviceIds && scope.row.serviceIds[0] !== '--'"
                                        popper-class="dark-popover"
                                        placement="top"
                                        trigger="hover">
                                        <div>
                                            <div style="wadth:75px"><p v-for="(item,index) in scope.row.serviceIds" :key="index">{{item}}</p></div>
                                        </div>
                                        <span slot="reference" class="service_id" style="white-space: nowrap;">
                                            <span v-for="(item,index) in scope.row.serviceIds" :key="index">{{item}}
                                                <span v-if="index+1 !== scope.row.serviceIds.length">,</span>
                                            </span>
                                        </span>
                                    </el-popover>
                                    <span v-else style="wadth:75px"><span v-for="(item,index) in scope.row.serviceIds" :key="index">{{item}}</span></span>
                                </template>
                            </el-table-column>
                            <el-table-column
                                width="180"
                                prop="RolePartyID"
                                label="Role & Party ID"
                            >
                                <template slot-scope="scope">
                                    <el-popover
                                        v-if="scope.row.rolePartyMapList.length > 0"
                                        placement="top"
                                        effect="dark"
                                        width="340"
                                        popper-class="party-popover-content"
                                        trigger="hover">
                                        <div class="party-popover"><span v-for="(item,index) in scope.row.rolePartyMapList" :key="index">{{item.role}}-{{item.partId}}<span v-if="index+1 !== scope.row.rolePartyMapList.length">,</span></span></div>
                                       <span slot="reference" class="service_id" style="white-space: nowrap;max-width:170px">
                                            <span v-for="(item,index) in scope.row.rolePartyMapList" :key="index">{{item.role}}-{{item.partId}}
                                                <span v-if="index+1 !== scope.row.rolePartyMapList.length">,</span>
                                            </span>
                                        </span>
                                    </el-popover>
                                    <span v-else
                                        v-for="(item,index) in scope.row.rolePartyMapList"
                                        :key="index"
                                    >
                                        {{item.role}}-{{item.partId}}
                                        <span
                                            v-if="index+1 !== scope.row.rolePartyMapList.length"
                                        >,</span>
                                    </span>
                                </template>
                            </el-table-column>
                            <el-table-column
                                width="200"
                                prop="resourceAdress"
                                label="FATE Flow Address"
                            >
                                <template slot-scope="scope" >
                                    <el-popover
                                        v-if="scope.row.resourceAdress.length > 0"
                                        :visible-arrow="false"
                                        placement="bottom"
                                        popper-class="dark-popover"
                                        @show="handleArrow(scope.$index,'2')"
                                        @hide="handleArrow(scope.$index,'1')"
                                        trigger="hover">
                                        <div>
                                            <div><p v-for="(item,index) in scope.row.resourceAdress" :key="index">{{item}}</p></div>
                                        </div>
                                       <span slot="reference" style="white-space: nowrap;">
                                            <span v-for="(item,index) in scope.row.resourceAdress" :key="index">{{item}}
                                                <span v-if="index+1 !== scope.row.resourceAdress.length">,</span>
                                            </span>
                                        </span>
                                    </el-popover>
                                    <span v-else
                                        v-for="(item,index) in scope.row.resourceAdress"
                                        :key="index" >
                                        {{item}}
                                    </span>
                                    <span v-if="scope.row.resourceAdress" :class="{'flow_address':scope.row.resourceAdress.length > 0,'flow_address_up':scope.row.up === '2'}"> </span>
                                </template>
                            </el-table-column>
                            <el-table-column
                                sortable
                                width="180"
                                prop="timestamp"
                                label="Timestamp"
                                show-overflow-tooltip
                            >
                                <template slot-scope="scope">
                                    <span>{{ scope.row.timestamp | dateform }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column width="150" label="Operation">
                                <template slot-scope="scope">
                                    <el-button
                                        type="text"
                                        style="font-size: 18px"
                                        class="marketing"
                                        size="mini"
                                        @click="showDialog(scope.row)"
                                    ></el-button>
                                    <el-button
                                        type="text"
                                        style="font-size: 18px"
                                        class="pipeline"
                                        size="mini"
                                        @click="showPipeLineDialog(scope.row)"
                                    ></el-button>
                                    <el-dropdown class="function-dropdown" placement="bottom" :hide-on-click="false" @command="moreFunction" trigger="click">
                                        <span class="el-dropdown-link function-list" @click="getRow(scope.row)">
                                        </span>
                                        <el-dropdown-menu slot="dropdown">
                                            <el-dropdown-item command="Unload" >Unload</el-dropdown-item>
                                            <el-dropdown-item command="Unbind" v-if="scope.row.serviceIds && scope.row.serviceIds[0] !=='--'" >Unbind</el-dropdown-item>
                                            <el-dropdown-item command="FlowControl" >Flow control</el-dropdown-item>
                                            <el-dropdown-item command="ModelSynchronize">Model synchronize</el-dropdown-item>
                                        </el-dropdown-menu>
                                    </el-dropdown>
                                    <!-- <el-button
                                        type="text"
                                        style="font-size: 14px"
                                        size="mini"
                                        @click="flowControl(scope.row)"
                                    >FlowControl</el-button> -->
                                    <!-- <el-button
                                        type="text"
                                        style="font-size: 14px"
                                        size="mini"
                                        @click="unload(scope.row)"
                                    >Unload</el-button> -->
                                    <!-- <el-button
                                        v-if="scope.row.serviceIds && scope.row.serviceIds[0] !=='--'"
                                        type="text"
                                        style="font-size: 14px"
                                        size="mini"
                                        @click="unbind(scope.row)"
                                    >Unbind</el-button> -->
                                </template>
                            </el-table-column>
                        </el-table>
                        <div class="pagination">
                            <el-pagination
                                background
                                @current-change="handleCurrentChange"
                                :current-page.sync="currentPage"
                                :page-size="20"
                                layout="total, prev, pager, next, jumper"
                                :total="total"
                            ></el-pagination>
                        </div>
                        <div v-if="dialogVisible">
                            <el-dialog
                                title="Model monitor"
                                width="50%"
                                custom-class="dialogtit"
                                :visible.sync="dialogVisible"
                            >
                                <div class="chart">
                                    <div class="modelchart">
                                        <modelchart :callsData="polar" />
                                    </div>
                                </div>
                            </el-dialog>
                        </div>
                        <el-dialog :visible.sync="unbindVisible" :showClose='cancel' width="35%" top="15%" center>
                            <span>Please select the Service IDs</span><br>
                            <span>you want to unbind with this model,</span>
                            <div class="choose-serviceid">
                                <el-checkbox-group v-if="rowData.serviceIds && rowData.serviceIds[0] !== '--'" v-model="serviceIDCheckList">
                                    <el-checkbox v-for="(item,index) in rowData.serviceIds"
                                        :key="index" :label="item">
                                    </el-checkbox>
                                </el-checkbox-group>
                            </div>
                            <span slot="footer" class="dialog-footer dialog-but">
                                <el-button type="primary" :disabled="!serviceIDCheckList.length" @click="sureUnbind">Sure</el-button>
                                <el-button
                                    type="primary"
                                    @click="unbindVisible = false"
                                    style="background:#B8BFCC;border: 1px solid #B8BFCC"
                                >Cancel</el-button>
                            </span>
                        </el-dialog>
                        <el-dialog :visible.sync="unloadVisible" :showClose='cancel' width="35%" top="15%" center>
                            <span>
                                Unload model "
                                <span style="color:#217AD9">{{ rowData.tableName }}</span>" ?
                            </span>
                            <span slot="footer" class="dialog-footer dialog-but">
                                <el-button type="primary" @click="sureUnload">Sure</el-button>
                                <el-button
                                    type="primary"
                                    @click="unloadVisible = false"
                                    style="background:#B8BFCC;border: 1px solid #B8BFCC"
                                >Cancel</el-button>
                            </span>
                        </el-dialog>
                        <el-dialog :visible.sync="flowControlVisible" :showClose='cancel' title="FlowControl" class="flowControl" width="35%" top="15%" center>
                            <div class="name">
                                <el-input v-model.number="qps" @input='inputqps'></el-input>
                            </div><span style="color:#217AD9;font-size:18px;">qps</span>
                            <span slot="footer" class="dialog-footer  dialog-but">
                                <el-button type="primary" :disabled="!qps" @click="sureflowControl">OK</el-button>
                                <el-button
                                    type="primary"
                                    @click="flowControlVisible = false"
                                    style="background:#B8BFCC;border: 1px solid #B8BFCC"
                                >Cancel</el-button>
                            </span>
                        </el-dialog>
                        <el-dialog :visible.sync="modelSynchronizeVisible" :showClose='cancel' title="Model synchronize" class="model-synchronize" width="37%" top="15%" center>
                            <span>The model will synchronize to the serving-server you select.</span>
                            <el-select class="serving-server-select input-placeholder" placeholder=" " v-model="servingServer" filterable clearable >
                                <el-option
                                    v-for="item in servingServerList"
                                    :key="item.value"
                                    :label="item.label"
                                    :value="item.value"
                                ></el-option>
                            </el-select>
                            <span slot="footer" class="dialog-footer  dialog-but">
                                <el-button type="primary"  @click="checkModel">OK</el-button>
                                <el-button
                                    type="primary"
                                    @click="modelSynchronizeVisible = false"
                                    style="background:#B8BFCC;border: 1px solid #B8BFCC"
                                >Cancel</el-button>
                            </span>
                        </el-dialog>
                        <el-dialog :visible.sync="secSureSynchronizeVisible" :showClose='cancel' title="" class="model-synchronize" width="40%" top="15%" center>
                            <span>The model already exsits in {{ servingServer }},</span>
                            <br>
                            <span>continue synchronizing and cover it?</span>
                            <span slot="footer" class="dialog-footer  dialog-but">
                                <el-button type="primary"  @click="selectSynchron">OK</el-button>
                                <el-button
                                    type="primary"
                                    @click="secSureSynchronizeVisible = false"
                                    style="background:#B8BFCC;border: 1px solid #B8BFCC"
                                >Cancel</el-button>
                            </span>
                        </el-dialog>
                    </div>
                    <div v-if="ipInfo === 2" class="ip-info-Monitor">
                        <div class="Monitor-chart">
                            <div class="Monitor">
                                <monitorchart v-loading="loadingEchart" :callsData="MonitorPolar" />
                            </div>
                        </div>
                    </div>
                    <div v-if="ipInfo === 3" class="ip-info-JVM">
                        <ul class="JVM-ul" :class="JVMInfo === 1 || JVMInfo === 0  ? 'ulBasic' : ''">
                            <li :class="JVMInfo === 0 ? 'activeInfo' : ''" @click="tabJVMInfo(0)">Memory</li>
                            <li
                                :class="JVMInfo === 1 ? 'activeInfo' : ''"
                                @click="tabJVMInfo(1)"
                            >GC Frequency</li>
                            <li
                                :class="JVMInfo === 2 ? 'activeInfo' : ''"
                                @click="tabJVMInfo(2)"
                            >GC Time</li>
                            <li
                                :class="JVMInfo === 3 ? 'activeInfo' : ''"
                                @click="tabJVMInfo(3)"
                            >Thread Count</li>
                        </ul>
                        <div class="jvm-chart" ref="jvm">
                            <div class="jvm-ch">
                                <jvmchart v-if="ipInfo === 3" :callsData="{JvmData,JVMInfo}" />
                            </div>
                        </div>
                    </div>
                    <div v-if="ipInfo === 4" class="ip-info-Router" >
                        <div class="search">
                            <el-input placeholder="Party ID" v-model="routerPartyID">
                                <el-button slot="prepend" icon="el-icon-search" @click="searchRouter"></el-button>
                            </el-input>
                        </div>
                        <routertable ref="routertable" v-loading="loadingRouterTable" :showRouterTable="showRouterTable" :ipPort="ipPort" :routerPartyID="routerPartyID" @tabipInfo="tabipInfo" />
                    </div>
                    <div>
                        <pipelinedialog
                        ref="pipelinedialog"
                        @closePipeline="closePipeline"
                        v-loading="loadingPipeline"
                        :pipelineVisible="pipelineVisible"
                        :dagDataBak="dagDataBak"
                        :dagData="dagData"  />
                    </div>

                </div>
            </div>

        </div>
         <healthy ref="healthy" :HealthData='HealthData' @checkup="checkup"> </healthy>
    </div>
</template>

<script>
import moment from 'moment'
import overview from './components/overview'
import iplist from './components/iplist'
import jvmchart from './components/jvmchart'
import monitorchart from './components/monitorchart'
import modelchart from './components/modelchart'
import healthy from './components/healthy'
import routertable from './components/routerlist'
import pipelinedialog from './components/pipelinedialog'
import _ from 'lodash'

import {
    getCluster,
    getlistProps,
    getmodellist,
    modelUnload,
    modelUnbind,
    queryModel,
    queryMonitor,
    queryJvm,
    updateFlowRule,
    checkHealth,
    transfer
} from '@/api/cluster'
export default {
    name: 'cluster',
    components: {
        overview,
        iplist,
        jvmchart,
        monitorchart,
        modelchart,
        healthy,
        routertable,
        pipelinedialog
    },
    data() {
        return {
            titvisible: false,
            qps: '',
            noSelectedData: true,
            serviceIDCheckList: [],
            cancel: false,
            polar: {}, // model图表信息
            MonitorPolar: {}, // Monitor图表信息
            JVMPolar: {}, // JVM图表信息
            loadingEchart: false,
            dialogVisible: false, // model图表弹窗
            currentPage: 1, // 当前页
            total: 0, // 数据总数
            page: 1, // 页数
            pageSize: 20, // 每页条数
            selected: 2, // proxy，serving  tab
            activeip: 0, // ip列表选中高亮
            ipInfo: 0, // Basic，model  tab
            serviceid: '', // 按serviceid搜索
            instance: '', //  按ip搜索值
            searchkey: '', // 按key搜索
            clusterData: [], // proxy，serving 列表数据
            basicData: [], // basic 数据
            ipData: {}, // ip列表数据
            rowData: {}, //  Models 表格选中一行数据
            ipPort: [], // ip和pora
            ipchildrenData: {}, // ip 子集数据
            unbindVisible: false, // unbind
            unloadVisible: false, // unload
            flowControlVisible: false,
            modelSynchronizeVisible: false,
            secSureSynchronizeVisible: false,
            flag: true,
            ModelsData: [], // Models数据
            JVMInfo: 0, //  JVM tab
            JvmData: [], //  JVM 数据
            Monitordata: {}, // Traffic Monitor数据
            MonitorXdate: [], // Traffic Monitor数据横坐标
            MonitorYvalue: [],
            oledata: [], // Models 弹框图表数据
            JVMseries: [],
            ArrServing: [],
            ArrProxy: [],
            chartArr: {},
            clusterTimer: null, // ip列表定时器
            chartTimer: null, // 视图定时器
            servingServer: '',
            servingServerList: [
                {
                    label: '111',
                    value: '111'
                },
                {
                    label: '222',
                    value: '222'
                }
            ],
            pipelineVisible: false,
            pipelineWidth: '60%',
            checkupStatus: 0,
            HealthData: {},
            isLarger: false,
            routerPageData: {
                page: 1,
                pageSize: 10
            },
            routerPartyID: '',
            loadingRouterTable: false,
            showRouterTable: false,
            loadingPipeline: false,
            dagData: [],
            dagDataBak: {}
        }
    },
    watch: {
        dialogVisible: {
            handler: function(val) {
                if (!val) {
                    this.clearChartTimer()
                }
            },
            immediate: true,
            deep: true
        },
        flowControlVisible: {
            handler: function(val) {
                if (!val) {
                    this.qps = ''
                }
            },
            immediate: true,
            deep: true
        },
        ipPort: {
            handler: function(val, old) {
                if (old) {
                    if (val[0] !== old[0] || val[1] !== old[1]) {
                        this.clearChartTimer()
                        this.listProps()
                        // 2.1.0 模型同步后需要跳转到迁移目标的model tab 故此处不能设定ipInfo
                        this.ipInfo = 0
                    }
                }
            },
            immediate: true,
            deep: true
        },
        ipchildrenData: {
            handler: function(val, old) {
                if (!val) {
                    this.activeip = 0
                    this.ipchildrenData = this.ipData.children[0]
                }
            },
            immediate: true,
            deep: true
        }
    },
    computed: {},
    created() {
        this.getClusterlist()
        this.getHealthData()

        this.clusterTimer = setInterval(() => {
            this.getClusterlist()
        }, 5000)
    },
    beforeDestroy() {
        this.clearChartTimer()
        clearInterval(this.clusterTimer)
    },
    methods: {
        checkup (index, HealthData) {
            if (index) {
                this.checkupStatus = index
            }
            if (HealthData) {
                this.HealthData = HealthData
            }
        },
        heal() {
            this.$refs.healthy.healthyVisible = true
        },
        getHealthData() {
            checkHealth().then(res => {
                this.HealthData = res.data
                setTimeout(() => {
                    this.$refs.healthy.initHealthData()
                })
            })
        },
        getClusterlist() {
            // 初始化数据
            getCluster().then(res => {
                this.clusterData = res.data.children
                this.tabNav({ index: this.selected })
            })
        },
        tabNav({ index, info, listIndex }) {
            if (info) {
                this.searchkey = ''
                this.serviceid = ''
                this.ipInfo = 0
                this.selected = index
                this.clearChartTimer()
                this.activeip = 0
            }
            // this.activeip = 0
            this.ArrServing = this.clusterData.filter(item => {
                return item.name === 'serving'
            })
            this.ArrProxy = this.clusterData.filter(item => {
                return item.name === 'proxy'
            })
            this.chartArr = {
                'serving': this.ArrServing,
                'proxy': this.ArrProxy
            }
            if (
                this.ArrServing[0] &&
                !this.ArrServing[0].children.length
            ) {
                index = 1
            } else if (
                this.ArrProxy[0] &&
                !this.ArrProxy[0].children.length
            ) {
                index = 2
            }
            if (this.ArrProxy.length > 0 && this.ArrProxy[0].children.length === 0 && this.ArrServing && this.ArrServing[0].children.length === 0) {
                this.clearChartTimer()
                this.noSelectedData = true
            } else {
                this.noSelectedData = false
            }
            this.selected = index

            if (this.selected === 1) {
                this.ipDataArr = this.ArrProxy
            } else {
                this.ipDataArr = this.ArrServing
            }
            if (this.ipDataArr[0] && this.ipDataArr[0].children.length) {
                this.ipData = this.ipDataArr[0]
                this.ipchildrenData = this.ipData.children[this.activeip]
                setTimeout(() => {
                    this.ipPort = this.ipData.children[this.activeip].name.split(':')
                })
                if (listIndex >= 0) {
                    this.selectIP(this.ipDataArr[0].children[listIndex], listIndex)
                }
            } else {
                this.ipchildrenData = []
                this.basicData = []
                this.ipData = {}
            }
        },
        listProps(data) {
            // 获取 Basic 数据
            const params = {
                host: this.ipPort[0],
                port: this.ipPort[1],
                keyword: data
            }
            getlistProps(params).then(res => {
                this.basicData = res.data.rows
                for (var i = 0; i < this.basicData.length; i++) {
                    this.basicData[i].value = this.basicData[i].value + ''
                }
            })
        },
        sureUnload() {
            // 确定unload
            this.unloadVisible = false
            const params = {
                host: this.ipPort[0],
                port: this.ipPort[1],
                tableName: this.rowData.tableName,
                namespace: this.rowData.namespace
            }
            modelUnload(params).then(res => {
                if (+res.retcode === 0) {
                    this.tabipInfo(this.ipInfo)
                }
            })
        },
        date(value) {
            // 时间格式函数
            return value ? moment(value).format('HH:mm:ss') : '--'
        },
        sureUnbind() {
            // 确定unbind
            const params = {
                host: this.ipPort[0],
                port: this.ipPort[1],
                tableName: this.rowData.tableName,
                namespace: this.rowData.namespace,
                serviceIds: this.serviceIDCheckList
            }
            this.unbindVisible = false
            modelUnbind(params).then(res => {
                if (+res.retcode === 0) {
                    this.tabipInfo(this.ipInfo)
                }
            })
        },
        inputqps() {
            var re = /^[0-9]+$/
            if (!re.test(this.qps)) {
                this.qps = ''
            }
            this.qps = this.qps + ''
            if (this.qps.length >= 10) {
                this.qps = this.qps.substring(0, 10)
            } else if (this.qps < 0 && this.qps !== '') {
                this.qps = 0
            }
        },
        sureflowControl() {
            const params = {
                host: this.ipPort[0],
                port: this.ipPort[1],
                source: this.rowData.resourceName,
                allowQps: this.qps - 0
            }
            this.unbindVisible = false
            updateFlowRule(params).then(res => {
                this.flowControlVisible = false
                this.tabipInfo(this.ipInfo)
            })
        },
        checkModel() {
            const params = {
                host: this.servingServer.split(':')[0],
                port: this.servingServer.split(':')[1],
                tableName: this.rowData.tableName,
                namespace: this.rowData.namespace
            }
            console.log(params, 'params')
            getmodellist(params).then(res => {
                console.log(res)
                if (_.get(res, 'data.total', '') > 0) {
                    this.secSureSynchronizeVisible = true
                } else {
                    this.selectSynchron()
                }
            })
        },
        selectSynchron() {
            console.log(this.rowData, 'rowData-selectSynchron')
            const params = {
                host: this.ipPort[0],
                port: this.ipPort[1],
                targetHost: this.servingServer.split(':')[0],
                targetPort: this.servingServer.split(':')[1],
                tableName: this.rowData.tableName,
                namespace: this.rowData.namespace
            }
            transfer(params).then(res => {
                console.log(res)
                if (_.get(res, 'retcode', '') === 0) {
                    this.$message.success('Model synchronize success')
                    // 成功后跳转到目标迁移机器的model tab
                    let listIndex = ''
                    let ipArr = _.get(this, 'ipDataArr[0].children', [])
                    ipArr.map((item, index) => {
                        if (item.name === this.servingServer) {
                            listIndex = index
                        }
                    })
                    this.selectIP(this.ipDataArr[0].children[listIndex], listIndex)
                } else {
                    const h = this.$createElement
                    this.$message.warning({
                        message: h('div', null, [
                            h('div', null, 'Model synchronize failed'),
                            h('div', { style: 'font-size:12px;color:#999;margin-top:6px' }, res.retmsg)
                        ]),
                        'duration': 3000
                    }, true)
                }
                this.closeSynchronizeModel()
                console.log(this.ipDataArr, 'this.ipDataArr')
            })
        },
        closeSynchronizeModel() {
            this.modelSynchronizeVisible = false
            this.secSureSynchronizeVisible = false
        },
        modelpolar() {
            // 获取model 弹框数据
            const xDate = []
            const yData = []
            const eData = []
            this.oledata &&
                this.oledata.forEach((item, index) => {
                    if (item) {
                        xDate.push(this.date(item.timestamp))
                        yData.push(item.passQps)
                        eData.push(item.exceptionQps)
                    }
                })
            const Arr = [{ name: 'passQps', textStyle: { color: '#00C99E' } },
                { name: 'exceptionQps', textStyle: { color: '#eb3941' } }]
            this.polar = {
                xAxis: {
                    type: 'category',
                    data: xDate,
                    boundaryGap: false,
                    axisLine: {
                        color: 'blur', //
                        lineStyle: {
                            type: 'solid',
                            color: '#848C99', // y轴的颜色
                            width: '3' // y坐标轴线的宽度
                        }
                    },
                    axisLabel: {
                        textStyle: {
                            fontSize: 9
                        }
                    }
                },
                tooltip: {
                    trigger: 'axis'
                },
                title: {
                    show: !this.oledata.length,
                    text: 'no data',
                    left: 'center',
                    top: 'center',
                    textStyle: {
                        fontSize: 12,
                        color: '#848C99'
                    }
                },
                yAxis: {
                    type: 'value',
                    name: 'qps',
                    axisLine: {
                        color: 'blur', //
                        lineStyle: {
                            type: 'solid',
                            color: '#848C99', // y轴的颜色
                            width: '1' // y坐标轴线的宽度
                        }
                    },
                    axisLabel: {
                        textStyle: {
                            fontSize: 9
                        }
                    }
                },
                legend: {
                    right: '100px',
                    orient: 'horizontal',
                    top: 28,
                    data: Arr,
                    textStyle: {
                        fontSize: 13
                    }
                },
                series: [
                    {
                        name: 'passQps',
                        data: yData,
                        type: 'line',
                        symbol: 'circle',
                        symbolSize: 4,
                        itemStyle: {
                            color: '#00C99E'
                        }
                    },
                    {
                        name: 'exceptionQps',
                        data: eData,
                        type: 'line',
                        symbol: 'circle',
                        symbolSize: 4,
                        itemStyle: {
                            color: '#eb3941'
                        }
                    }
                ]
            }
        },
        showDialog(row) {
            // 显示model 弹框
            this.oledata = []
            this.dialogVisible = true
            const params = {
                host: this.ipPort[0],
                port: this.ipPort[1],
                source: row.resourceName
            }
            this.handleModelData(params)
            this.setChartTimer(() => {
                this.handleModelData(params)
            })
        },
        handleModelData(params) {
            // 1获得数据
            queryModel(params).then(res => {
                let data, incomingData
                ;[data, incomingData] = this.getIncomingData(
                    this.oledata,
                    res.data[params.source]
                )
                if (!incomingData) {
                    return
                }
                if (data.length > 60) {
                    data = data.slice(incomingData.length - 1)
                }
                this.oledata = data
                this.modelpolar(this.oledata)
            })
        },
        searchServiceID() {
            // Models 搜索ServiceID
            this.tabipInfo(this.ipInfo)
        },
        searchKeyword() {
            // Basic 搜索key
            this.listProps(this.searchkey)
        },
        selectIP(item, index) {
            let type = item.label.split('-')[1]
            let name = item.name
            // 选中ip
            this.searchkey = ''
            this.serviceid = ''
            this.activeip = +index
            this.ipchildrenData = item
            this.titvisible = false
            this.ipPort = this.ipchildrenData.name.split(':')
            this.$refs.overview.selectItem = [name, type]
            console.log([name, type], '[name, type]')
            if (this.ipInfo === 0) {
                this.listProps()
            } else {
                this.$nextTick(() => {
                    this.tabipInfo(this.ipInfo)
                })
            }
        },
        tabJVMInfo(index) {
            // JVM tab
            this.JVMInfo = +index
        },
        tabipInfo(index) {
            //  Basic、Models  tab
            this.clearChartTimer()
            if (this.$refs.routertable && this.$refs.routertable.hasChange === true) {
                this.$refs.routertable.sureLeaveDialog = true
                this.$set(this.$refs.routertable, 'next', index)
            } else {
                this.showRouterTable = false
                this.ipInfo = +index
                if (this.ipInfo === 0) {
                    this.tabToBasic()
                } else if (this.ipInfo === 1) {
                    this.tabToModels()
                } else if (this.ipInfo === 2) {
                    this.tabToMonitor()
                } else if (this.ipInfo === 3) {
                    this.tabToJvm()
                } else if (this.ipInfo === 4) {
                    this.tabToRouter()
                }
            }
        },
        tabToBasic() {
            if (this.ipPort.length === 0) return false
            this.listProps()
        },
        tabToModels() {
            this.MonitorPolar = {}
            const params = {
                page: this.page,
                pageSize: this.pageSize,
                host: this.ipPort[0],
                port: this.ipPort[1],
                serviceId: this.serviceid
            }
            getmodellist(params).then(res => {
                this.ModelsData = res.data.rows
                this.total = res.data.total
                for (var i = 0; i < this.ModelsData.length; i++) {
                    if (this.ModelsData[i].serviceIds && this.ModelsData[i].serviceIds.length === 0) {
                        this.ModelsData[i].serviceIds.push('--')
                    }
                    // 初始化flow address 下拉列表箭头状态
                    this.$set(this.ModelsData[i], 'up', '1')
                    // mock
                    if (this.ModelsData[i]['resourceAdress']) {
                        this.ModelsData[i]['resourceAdress'] = [this.ModelsData[i]['resourceAdress']]
                    } else {
                        this.ModelsData[i]['resourceAdress'] = []
                    }
                }
                console.log(this.ModelsData, 'ModelsData')
            })
        },
        tabToMonitor() {
            if (this.ipPort.length === 0) return false
            const params = {
                host: this.ipPort[0],
                port: this.ipPort[1]
            }
            this.Monitordata = {}
            this.MonitorXdate = []
            this.handleMonitorData(params)
            // 5计时器，重复1-4
            this.setChartTimer(() => {
                this.handleMonitorData(params)
            })
        },
        tabToJvm() {
            if (this.ipPort.length === 0) return false
            const params = {
                host: this.ipPort[0],
                port: this.ipPort[1]
            }
            this.JvmData = []
            this.handleJvmData(params)
            // 5设定计时器
            this.setChartTimer(() => {
                this.handleJvmData(params)
            })
        },
        tabToRouter() {
            this.showRouterTable = true
        },
        handleMonitorData(params) {
            queryMonitor(params).then(res => {
                var legendArr = []
                var incomingData = {}
                for (var i in res.data) {
                    if (this.Monitordata[i] === undefined) {
                        this.Monitordata[i] = []
                    }
                    ;[
                        this.Monitordata[i],
                        incomingData[i]
                    ] = this.getIncomingData(this.Monitordata[i], res.data[i])

                    legendArr.push(i)
                }

                var MonitorArr = []
                var xDate = []
                var monitorXdate = []
                for (var j = 0; j < legendArr.length; j++) {
                    // 每一组新增的数据,
                    const mData = incomingData[legendArr[j]]
                    // if (mData.length === 16) {
                    //     mData.pop()
                    // }
                    var dataArr = []
                    for (var z = 0; z < mData.length; z++) {
                        if (mData[z]) {
                            dataArr.push(mData[z].passQps)
                            if (j === 0) {
                                // 只需要拿某一组的横坐标，都是统一的
                                xDate.push(this.date(mData[z].timestamp))
                                // xDate.push(mData[z].timestamp)
                            }
                        }
                    }

                    this.MonitorYvalue[legendArr[j]] =
                            this.MonitorYvalue[legendArr[j]] &&
                            this.MonitorYvalue[legendArr[j]].length > 0
                                ? this.MonitorYvalue[legendArr[j]].concat(
                                    dataArr
                                )
                                : dataArr
                    var op = {
                        name: legendArr[j],
                        data: this.MonitorYvalue[legendArr[j]],
                        type: 'line',
                        symbol: 'circle',
                        symbolSize: 4
                    }
                    MonitorArr.push(op)
                }
                monitorXdate = this.MonitorXdate.concat(xDate)
                if (monitorXdate.length > 60) {
                    let index = monitorXdate.length - 60
                    monitorXdate = monitorXdate.slice(index)
                    MonitorArr.forEach((m) => {
                        m.data = m.data.slice(index)
                        this.MonitorYvalue[m.name] = m.data
                    })
                }
                this.MonitorXdate = monitorXdate

                this.TrafficMonitor(MonitorArr, this.MonitorXdate, legendArr)
            })
        },
        handleJvmData(params) {
            queryJvm(params).then(res => {
                let data
                ;[data] = this.getIncomingData(this.JvmData, res.data.rows)
                if (data.length > 60) {
                    data = data.slice(data.length - 60)
                }
                this.JvmData = data
                this.tabJVMInfo(this.JVMInfo)
            })
        },
        getIncomingData(current, incoming = []) {
            var index = 0
            if (current.length > 0) {
                const lastTimeStamp = current[current.length - 1].timestamp
                incoming.forEach((item, i) => {
                    if (+item.timestamp === +lastTimeStamp) {
                        index = i + 1
                    }
                })
            }
            current = current.concat(incoming.slice(index))
            return [current, incoming.slice(index)]
        },
        TrafficMonitor(MonitorArr, xDate, legendArr) {
            // TrafficMonitor 图表
            var arr = []
            var MonArr = [{ name: 'I_QUERY_METRICS', textStyle: { color: '#b74c8d' } },
                { name: 'I_UPDATE_FLOW_RULE', textStyle: { color: '#eb75d7' } },
                { name: 'I_LIST_PROPS', textStyle: { color: '#ad81ef' } },
                { name: 'I_QUERY_JVM', textStyle: { color: '#6666ff' } },
                { name: 'I_UPDATE_SERVICE', textStyle: { color: '#4aa2ff' } },
                { name: 'I_unaryCall', textStyle: { color: '#29bccc' } },
                { name: 'I_inference', textStyle: { color: '#30ddf0' } },
                { name: 'I_batchInference', textStyle: { color: '#00c99e' } },
                { name: 'I_singleInference', textStyle: { color: '#85ab4b' } },
                { name: 'I_hostInferenceProvider', textStyle: { color: '#abcc29' } },
                { name: 'I_MODEL_LOAD', textStyle: { color: '#fad900' } },
                { name: 'I_MODEL_PUBLISH_ONLINE', textStyle: { color: '#ff9d00' } },
                { name: 'I_QUERY_MODEL', textStyle: { color: '#ef6363' } },
                { name: 'I_UNLOAD', textStyle: { color: '#c65f5f' } },
                { name: 'I_UNBIND', textStyle: { color: '#848c99' } },
                { name: 'I_CHECK_HEALTH', textStyle: { color: '#c23431' } },
                { name: 'I_QUERY_ROUTER', textStyle: { color: '#d049c9' } },
                { name: 'I_SAVE_ROUTER', textStyle: { color: '#7949d0' } }]
            for (var y = 0; y < legendArr.length; y++) {
                for (var p = 0; p < MonArr.length; p++) {
                    if (legendArr[y] === MonArr[p].name) {
                        arr.push(MonArr[p])
                    }
                    if (MonitorArr[y].name === MonArr[p].name) {
                        MonitorArr[y].lineStyle = {
                            color: MonArr[p].textStyle.color
                        }
                        MonitorArr[y].itemStyle = {
                            color: MonArr[p].textStyle.color
                        }
                    }
                }
            }
            this.MonitorPolar = {
                xAxis: {
                    type: 'category',
                    data: xDate,
                    boundaryGap: false,
                    axisLine: {
                        color: 'blur', //
                        lineStyle: {
                            type: 'solid',
                            color: '#848C99', // y轴的颜色
                            width: '3' // y坐标轴线的宽度
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
                    top: -5,
                    data: arr,
                    textStyle: {
                        fontSize: 11
                    }
                },
                grid: {
                    left: '3%'
                    // 是否显示网格
                    // show: true,
                    // // 是否显示刻度标签
                    // containLabel: true,
                    // borderColor: 'green'

                },
                yAxis: {
                    type: 'value',
                    name: 'qps',
                    axisLine: {
                        color: 'blur', //
                        lineStyle: {
                            type: 'solid',
                            color: '#848C99', // y轴的颜色
                            width: '1' // y坐标轴线的宽度
                        }
                    },
                    axisLabel: {
                        textStyle: {
                            fontSize: 12
                        }
                    }
                },
                series: MonitorArr
            }
        },
        handleCurrentChange(val) {
            this.page = val
            this.tabipInfo(this.ipInfo)
        },
        setChartTimer(cb, interval) {
            clearInterval(this.chartTimer)
            this.chartTimer = setInterval(cb, interval || 5000)
        },
        clearChartTimer() {
            clearInterval(this.chartTimer)
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
        getRow(row) {
            this.rowData = row
        },
        moreFunction(command) {
            if (command === 'Unload') {
                this.unloadVisible = true
            } else if (command === 'Unbind') {
                this.unbindVisible = true
                this.serviceIDCheckList = []
            } else if (command === 'FlowControl') {
                this.qps = this.rowData.allowQps
                this.flowControlVisible = true
            } else if (command === 'ModelSynchronize') {
                this.modelSynchronizeVisible = true
                this.servingServer = ''
                if (this.ipData && this.ipData.children) {
                    // 过滤出除当前serving的下拉菜单
                    this.servingServerList = this.ipData.children.filter(item => item.name !== this.ipchildrenData.name)
                    this.servingServerList = this.servingServerList.map(item => {
                        return {
                            label: item.name,
                            value: item.name
                        }
                    })
                    console.log(this.servingServerList, 'servingServerList')
                }
            }
        },
        showPipeLineDialog(row) {
            this.dagData = row ? row.components : []
            // 在小窗状态打开
            this.$refs['pipelinedialog'].setSize('2')
            this.pipelineVisible = true
        },
        handleArrow(index, type) {
            this.$set(this.ModelsData[index], 'up', type)
        },
        setSize(type) {
            this.isLarger = type === '1'
        },
        searchRouter() {
            this.$refs['routertable'].getTableData()
        },
        closePipeline() {
            this.pipelineVisible = false
        }
    }
}
</script>

<style rel="stylesheet/scss" lang="scss">
@import 'src/styles/cluster.scss';
.el-tooltip__popper {
    transform:translateY(100px);
     font-size:14px;
    font-family:Product Sans;
    font-weight:400;
    line-height:17px;
    color:rgba(45,54,66,1);
}
.el-tooltip__popper.is-dark {
    background: #fff;
    color: #606266;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
    height: 28px;
    line-height: 28px;
    .popper__arrow {
        display: none !important;
    }
}
.el-dropdown-menu__item{
    color: #4d95e1 !important;
}
.party-popover-content{
    // overflow: hidden;
    padding: 10px;
    background: rgba(0,0,0,.8);
    color: #fff !important;

    .popper__arrow{
        top: unset !important;
    }
    .popper__arrow::after{
        border-top-color: rgba(0,0,0,.8) !important;
    }

    .titpopover-label {
        color: #fff;
    }
    .party-popover{
        display: flex;
        flex-wrap: wrap;
        width: 350px;
        max-height: 200px;
        overflow: auto;
        span{
            flex-shrink: 0;
            margin-right: 3px;
            margin-bottom: 5px;
        }
    }
}

.el-table .cell{
    padding-top: 5px;
    line-height: 14px;

}
.ip-info-Models{
    tbody {
        td:nth-of-type(5){
            padding-right: 30px !important;
            .cell{
                display: flex;
                overflow: unset;
                &>span[effect="dark"]{
                    overflow: hidden;
                    max-width: 160px;
                    display: inline-block;
                    text-overflow: ellipsis;
                }
            }
        }
    }
}
.flow_address{
    position: relative;
    &::after{
        position: absolute;
        top:50%;
        right: -20px;
        transform: translateY(-50%) rotate(0);
        content: '▼';
        color: #4E5766;
        font-size: 12px;
        line-height: 1;
        transition: all .5s;
    }
}
.flow_address_up{
    &::after{
        transform: translateY(-50%) rotate(-180deg);
    }
}

</style>
