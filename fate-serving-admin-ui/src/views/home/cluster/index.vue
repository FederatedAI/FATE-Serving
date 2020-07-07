<template>
<div class="main">
    <div class="ip-selector">
        <div class="overview">
            <div class="cluster">
                Cluster
            </div>
            <ul>
                <li v-if="ArrProxy[0] && ArrProxy[0].children.length" class="proxy" :class="selected === 1 ? 'active' : ''" @click="tabNav(1)">serving-proxy</li>
                <li v-else class="proxy disabled">serving-proxy</li>
                <li class="admin">admin</li>
                <li v-if="ArrServing[0] && ArrServing[0].children.length" class="serving" :class="selected === 2 ? 'active' : ''" @click="tabNav(2)">serving-server</li>
                <li v-else class="serving disabled">serving-server</li>
                <li class="caret-l caret"><p></p><i class="el-icon-caret-bottom"></i></li>
                <li class="caret-r caret"><p></p><i class="el-icon-caret-bottom"></i></li>
                <li class="caret-c caret"><i class="el-icon-caret-left" /><p></p><i class="el-icon-caret-right"/></li>
            </ul>
        </div>
        <div class="ip-list">
            <div class="ip-list-top">
                <span class="ip-list-tit">{{ selected === 1 ? 'serving-proxy' : 'serving-server' }}</span>
                <span class="ip-list-des">Click IP to view the  instance details</span>
            </div>
            <div class="instance">
                <!-- <el-input
                    placeholder="search for instance"
                    v-model="instance"
                    @change="searchInstance">
                    <el-button slot="prepend" icon="el-icon-search"></el-button>
                </el-input> -->
            </div>
            <div class="ip-list-line" ></div>
            <div class="ip-list-main">
                <div>
                    <div class="ip-list-li" v-for="(item,index) in ipData.children" :key = index @click="selectIP(item,index)">
                        <span :class="activeip === index ? 'activeip' : ''">{{ item && item.name }}</span>
                        <span class="iptime" :class="activeip === index ? 'activetime' : ''">{{ item.timestamp | datefrom}}</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="ip-info">
        <el-popover
            placement="bottom"
            width="400"
            popper-class="titpopover"
            trigger="click">
            <div class="titpopover-main">
                <div class="titpopover-label" v-for="(item, index) in ipData.children" :key="index" @click="selectIP(item, index)">
                    {{item && item.label}}
                </div>
            </div>
            <div class="ip-info-tit" slot="reference">
                {{ ipchildrenData && ipchildrenData.label }}<i class="el-icon-caret-bottom"></i>
            </div>
        </el-popover>
        <ul class="ul" :class="ipInfo === 1 || ipInfo === 0   ? 'ulBasic' : ''">
            <li :class="ipInfo === 0 ? 'activeInfo' : ''" @click="tabipInfo(0)">Basic</li>
            <li v-if="selected === 2" :class="ipInfo === 1 ? 'activeInfo' : ''" @click="tabipInfo(1)">Models</li>
            <li :class="ipInfo === 2 ? 'activeInfo' : ''" @click="tabipInfo(2)">Traffic Monitor</li>
            <li :class="ipInfo === 3 ? 'activeInfo' : ''" @click="tabipInfo(3)">JVM</li>
        </ul>
        <div class="ip-info-main" :class="ipInfo === 3 ?'info-main' : ''">
            <div v-if="ipInfo === 0" class="ip-info-Basic">
                <div class="search">
                    <el-input
                        placeholder="Keyword"
                        v-model="searchkey"
                        @change="searchKeyword">
                        <el-button slot="prepend" icon="el-icon-search"></el-button>
                    </el-input>
                </div>
                <el-table
                    :data="basicData"
                    :header-cell-style="{background:'#fff'}"
                    style="width: 100%;margin-bottom: 20px;"
                    max-height="668px"
                    class="table">
                    <el-table-column sortable prop="key" label="key" show-overflow-tooltip />
                    <el-table-column sortable prop="value" label="value" show-overflow-tooltip >
                        <template slot-scope="scope">
                            <span>
                                {{ scope.row.value }}
                            </span>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
            <div v-if="ipInfo === 1" class="ip-info-Models">
                <div class="search">
                    <el-input
                        placeholder="Service ID"
                        v-model="serviceid"
                        @change="searchServiceID">
                        <el-button slot="prepend" icon="el-icon-search"></el-button>
                    </el-input>
                </div>
                <el-table
                    :data="ModelsData"
                    :header-cell-style="{background:'#fff'}"
                    style="width: 100%;margin-bottom: 20px;"
                    height="calc(100% - 320px)"
                    class="table">
                    <el-table-column sortable width="350" prop="namespace" label="Model ID" show-overflow-tooltip />
                    <el-table-column sortable width="220" prop="tableName" label="Model Version" show-overflow-tooltip>
                        <template slot-scope="scope">
                            <span>
                                {{ scope.row.tableName }}
                            </span>
                        </template>
                    </el-table-column>
                    <el-table-column sortable width="160" prop="serviceId" label="Service ID" show-overflow-tooltip />
                    <el-table-column sortable width="200" prop="RolePartyID" label="Role & Party ID">
                         <template slot-scope="scope">
                            <el-popover
                                placement="bottom"
                                trigger="hover">
                                <div>
                                    <div style="wadth:75px"><span v-for="(item,index) in scope.row.rolePartyMapList" :key="index">{{item.role}}-{{item.partId}}<span v-if="index+1 !== scope.row.rolePartyMapList.length">,</span></span></div>
                                </div>
                                <span slot="reference" style="white-space: nowrap;"><span v-for="(item,index) in scope.row.rolePartyMapList" :key="index">{{item.role}}-{{item.partId}}<span v-if="index+1 !== scope.row.rolePartyMapList.length">,</span></span></span>
                            </el-popover>
                        </template>
                    </el-table-column>
                    <el-table-column sortable width="180" prop="timestamp" label="Timestamp" show-overflow-tooltip>
                        <template slot-scope="scope">
                            <span>{{ scope.row.timestamp | datefrom }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column width="200" label="Operation">
                        <template slot-scope="scope">
                            <el-button type="text" style="font-size: 18px" class="marketing" size="mini" @click="showDialog(scope.row)"></el-button>
                            <el-button type="text" style="font-size: 14px" size="mini" @click="unload(scope.row)">Unload</el-button>
                            <el-button type="text" style="font-size: 14px" size="mini" @click="unbind(scope.row )">Unbind</el-button>
                        </template>
                    </el-table-column>
                </el-table>
                <div class="pagination">
                    <el-pagination
                        background
                        @current-change="handleCurrentChange"
                        :current-page.sync="currentPage1"
                        :page-size="20"
                        layout="total, prev, pager, next, jumper"
                        :total="total"
                    ></el-pagination>
                </div>
                <el-dialog title="Model monitor" width="50%" custom-class="dialogtit" :visible.sync="dialogVisible">
                    <div class="chart">
                         <modelchart :callsData="polar"/>
                         <!-- <div v-else class="no-data">
                             no Data
                         </div> -->
                    </div>
                </el-dialog>
                <el-dialog
                    :visible.sync="unbindVisible"
                    width="35%"
                    top="15%"
                    center>
                    <span>Unbind the Service "<span style="color:#217AD9">{{ rowData.serviceId }}</span>"</span><br>
                    <span>with this model ? ? </span>
                    <span slot="footer" class="dialog-footer dialog-but">
                        <el-button type="primary" @click="sureUnbind">Sure</el-button>
                        <el-button type="primary" @click="unbindVisible = false" style="background:#B8BFCC;border: 1px solid #B8BFCC">Cancel</el-button>
                    </span>
                </el-dialog>
                <el-dialog
                    :visible.sync="unloadVisible"
                    width="35%"
                    top="15%"
                    center>
                    <span>Unload model "<span style="color:#217AD9">{{ rowData.tableName }}</span>" ?</span>
                    <span slot="footer" class="dialog-footer dialog-but">
                        <el-button type="primary" @click="sureUnload">Sure</el-button>
                        <el-button type="primary" @click="unloadVisible = false" style="background:#B8BFCC;border: 1px solid #B8BFCC">Cancel</el-button>
                    </span>
                </el-dialog>
            </div>
            <div v-if="ipInfo === 2" class="ip-info-Monitor">
                <div class="Monitor-chart">
                    <div class="Monitor">
                        <Monitorchart v-loading="loadingEchart" :callsData="MonitorPolar"/>
                    </div>
                </div>
            </div>
            <div v-if="ipInfo === 3" class="ip-info-JVM">
                <ul class="JVM-ul" :class="JVMInfo === 1 || JVMInfo === 0  ? 'ulBasic' : ''">
                    <li :class="JVMInfo === 0 ? 'activeInfo' : ''" @click="tabJVMInfo(0)">Memory</li>
                    <li :class="JVMInfo === 1 ? 'activeInfo' : ''" @click="tabJVMInfo(1)">GC Frequency</li>
                    <li :class="JVMInfo === 2 ? 'activeInfo' : ''" @click="tabJVMInfo(2)">GC Time</li>
                    <li :class="JVMInfo === 3 ? 'activeInfo' : ''" @click="tabJVMInfo(3)">Thread Count</li>
                </ul>
                <div class="jvm-chart" ref="jvm">
                    <div class="jvm-ch">
                        <jvmchart v-if="ipInfo === 3" :callsData="{JvmData,JVMInfo}"/>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</template>

<script>
import moment from 'moment'
import jvmchart from './jvmchart'
import Monitorchart from './Monitorchart'
import modelchart from './modelchart'
import { getCluster, getlistProps, getmodellist, modelUnload, modelUnbind, queryModel, queryMonitor, queryJvm } from '@/api/cluster'
export default {
    name: 'cluster',
    filters: {
        datefrom(value) {
            return value ? moment(value).format('YYYY-MM-DD HH:mm:ss') : '--'
        }
    },
    components: {
        jvmchart,
        Monitorchart,
        modelchart
    },
    data() {
        return {
            polar: {}, // model图表信息
            MonitorPolar: {}, // Monitor图表信息
            JVMPolar: {}, // JVM图表信息
            loadingEchart: false,
            dialogVisible: false, // model图表弹窗
            currentPage1: 1, // 当前页
            total: 0, // 数据总数
            page: 1, // 页数
            pageSize: 20, // 每页条数
            selected: 1, // proxy，serving  tab
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
            flag: true,
            clusterTimer: null, // ip列表定时器
            ModelsData: [], // Models数据
            JVMInfo: 0, //  JVM tab
            JvmData: [], //  JVM 数据
            Monitordata: {}, // Traffic Monitor数据
            oledata: [], // Models 弹框图表数据
            JvmTimer: null, // Jvm 5秒定时期
            JTimer: null, // Jvm 1秒定时期
            trafficTimer: null, // Traffic Monitor定时器
            mTimer: null, // Traffic Monitor1秒定时期
            modelsTimer: null, // Models 弹框图表定时器
            moTimer: null, // Models 弹框图表1秒定时器
            JVMseries: [],
            ArrServing: [],
            ArrProxy: []
        }
    },
    watch: {
        dialogVisible: {
            handler: function(val) {
                if (!val) {
                    clearInterval(this.modelsTimer)
                    clearInterval(this.moTimer)
                }
            },
            immediate: true,
            deep: true
        }
    },
    computed: {},
    created() {
        this.getClusterlist()
        this.clusterTimer = setInterval(() => {
            this.getClusterlist()
        }, 5000)
    },
    beforeDestroy() {
        clearInterval(this.clusterTimer)
        clearInterval(this.trafficTimer)
        clearInterval(this.JvmTimer)
        clearInterval(this.JTimer)
        clearInterval(this.mTimer)
        clearInterval(this.modelsTimer)
        clearInterval(this.moTimer)
    },
    methods: {
        getClusterlist() { // 初始化数据
            getCluster().then(res => {
                this.clusterData = res.data.children
                this.tabNav()
            })
        },
        tabNav(index) { // proxy，serving tab切换
            if (index) {
                clearInterval(this.trafficTimer)
                clearInterval(this.JvmTimer)
                clearInterval(this.JTimer)
                clearInterval(this.mTimer)
            }
            this.searchkey = ''
            this.serviceid = ''
            // this.ipInfo = 0
            this.activeip = 0
            this.ArrServing = this.clusterData.filter(item => {
                return item.name === 'serving'
            }
            )
            this.ArrProxy = this.clusterData.filter(item => {
                return item.name === 'proxy'
            }
            )
            if (this.ArrServing[0] && this.ArrServing[0].children.length && !index) {
                index = 2
            } else if (this.ArrProxy[0] && this.ArrProxy[0].children.length && !index) {
                index = 1
            }
            this.selected = +index
            if (this.selected === 2) {
                this.ipDataArr = this.ArrServing
            } else {
                this.ipDataArr = this.ArrProxy
            }
            if (this.ipDataArr[0].children.length) {
                this.ipData = this.ipDataArr[0]
                this.ipchildrenData = this.ipData.children[0]
                this.ipPort = this.ipchildrenData.name.split(':')
                if (this.flag) {
                    this.listProps()
                    this.flag = false
                }
            } else {
                this.ipchildrenData = []
                this.basicData = []
                this.ipData = []
            }
        },
        listProps(data) { // 获取 Basic 数据
            const params = {
                host: this.ipPort[0],
                port: this.ipPort[1],
                keyword: data
            }
            getlistProps(params).then(res => {
                this.basicData = res.data.rows
                this.total = res.data.total
            })
        },
        unload(row) { // unload
            this.rowData = row
            this.unloadVisible = true
        },
        sureUnload() { // 确定unload
            this.unloadVisible = false
            const params = {
                host: this.ipPort[0],
                port: this.ipPort[1],
                tableName: this.rowData.tableName,
                namespace: this.rowData.namespace
            }
            modelUnload(params).then(res => {
                if (res.retcode) {
                    this.tabipInfo(this.ipInfo)
                }
            })
        },
        date(value) { // 时间格式函数
            return value ? moment(value).format('HH:mm:ss') : '--'
        },
        unbind(row) { // unbind
            this.rowData = row
            this.unbindVisible = true
        },
        sureUnbind() { // 确定unbind
            const params = {
                host: this.ipPort[0],
                port: this.ipPort[1],
                tableName: this.rowData.tableName,
                namespace: this.rowData.namespace,
                serviceId: this.rowData.serviceId
            }
            this.unbindVisible = false
            modelUnbind(params).then(res => {
                if (res.retcode) {
                    this.tabipInfo(this.ipInfo)
                }
            })
        },
        modelpolar() { // 获取model 弹框数据
            const xDate = []
            const yData = []
            this.oledata && this.oledata.forEach((item, index) => {
                if (item) {
                    xDate.push(this.date(item.timestamp))
                    yData.push(item.passQps)
                }
            })
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
                            width: '3'// y坐标轴线的宽度
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
                    show: !this.oledata,
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
                            width: '1'// y坐标轴线的宽度
                        }
                    },
                    axisLabel: {
                        textStyle: {
                            fontSize: 9
                        }
                    }
                },
                series: [{
                    name: '',
                    data: yData,
                    type: 'line',
                    itemStyle: {
                        color: '#4AA2FF'
                    }
                }]
            }
        },
        showDialog(row) { // 显示model 弹框
            this.dialogVisible = true
            const parmas = {
                host: this.ipPort[0],
                port: this.ipPort[1],
                source: row.resourceName
            }
            queryModel(parmas).then(res => {
                this.oledata = res.data[row.resourceName]
                if (!this.oledata) {
                    this.modelpolar(this.oledata)
                    return
                }
                this.modelpolar(this.oledata)
                this.modelsTimer = setInterval(() => {
                    queryModel(parmas).then(res => {
                        const oledata = res.data[row.resourceName]
                        if (!oledata) {
                            return
                        }
                        var im = 4
                        clearInterval(this.moTimer)
                        this.moTimer = setInterval(() => {
                            if (this.oledata.length > 100) {
                                this.oledata.shift()
                            }
                            this.oledata.push(oledata[im])
                            this.modelpolar(this.oledata)
                            im++
                        }, 1000)
                    })
                }, 5100)
            })
        },
        searchServiceID() { // Models 搜索ServiceID
            this.tabipInfo(this.ipInfo)
        },
        searchKeyword() { // Basic 搜索key
            this.listProps(this.searchkey)
        },
        searchInstance() { // ip搜索
            // console.log(this.instance)
        },
        selectIP(item, index) { // 选中ip
            this.activeip = +index
            this.ipchildrenData = item
            this.ipPort = this.ipchildrenData.name.split(':')
            if (this.ipInfo === 0) {
                this.listProps()
            } else {
                this.tabipInfo(this.ipInfo)
            }
        },
        tabJVMInfo(index) { // JVM tab
            this.JVMInfo = +index
        },
        tabipInfo(index) { //  Basic、Models  tab
            clearInterval(this.trafficTimer)
            clearInterval(this.JvmTimer)
            clearInterval(this.JTimer)
            clearInterval(this.mTimer)
            this.ipInfo = +index
            if (this.ipInfo === 1) {
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
                })
            } else if (this.ipInfo === 0) {
                if (this.ipPort.length === 0) {
                    return false
                }
                this.listProps()
            } else if (this.ipInfo === 2) {
                if (this.ipPort.length === 0) {
                    return false
                }
                const parmas = {
                    host: this.ipPort[0],
                    port: this.ipPort[1]
                }
                queryMonitor(parmas).then(res => {
                    this.Monitordata = res.data
                    var legendArr = []
                    for (var i in res.data) {
                        legendArr.push(i)
                    }
                    var MonitorArr = []
                    for (var j = 0; j < legendArr.length; j++) {
                        const mData = this.Monitordata[legendArr[j]]
                        if (mData.length === 16) {
                            mData.pop()
                        }
                        var xDate = []
                        var dataArr = []
                        for (var z = 0; z < mData.length; z++) {
                            if (mData[z]) {
                                dataArr.push(mData[z].passQps)
                                xDate.push(this.date(mData[z].timestamp))
                            }
                            var op = {
                                name: legendArr[j],
                                data: dataArr,
                                type: 'line'
                            }
                        }
                        MonitorArr.push(op)
                    }
                    this.TrafficMonitor(MonitorArr, xDate, legendArr)
                })
                this.trafficTimer = setInterval(() => {
                    queryMonitor(parmas).then(res => {
                        const data = res.data
                        var legendArr = []
                        for (var i in data) {
                            legendArr.push(i)
                        }
                        var q = 10
                        clearInterval(this.mTimer)
                        this.mTimer = setInterval(() => {
                            var MonitorArr = []
                            for (var j = 0; j < legendArr.length; j++) {
                                const mData = data[legendArr[j]]
                                const oldMdata = this.Monitordata[legendArr[j]]
                                if (oldMdata.length > 100) {
                                    oldMdata.shift()
                                }
                                // oldMdata.shift()
                                oldMdata.push(mData[q])
                                var xDate = []
                                var dataArr = []
                                for (var z = 0; z < oldMdata.length; z++) {
                                    if (oldMdata[z]) {
                                        dataArr.push(oldMdata[z].passQps)
                                        xDate.push(this.date(oldMdata[z].timestamp))
                                    }

                                    var op = {
                                        name: legendArr[j],
                                        data: dataArr,
                                        type: 'line'
                                    }
                                }
                                MonitorArr.push(op)
                            }
                            q++
                            this.TrafficMonitor(MonitorArr, xDate, legendArr)
                        }, 1000)
                    })
                }, 5120)
            } else if (this.ipInfo === 3) {
                if (this.ipPort.length === 0) {
                    return false
                }
                const parmas = {
                    host: this.ipPort[0],
                    port: this.ipPort[1]
                }
                queryJvm(parmas).then(res => {
                    this.JvmData = res.data.rows
                    this.tabJVMInfo(this.JVMInfo)
                    this.JvmTimer = setInterval(() => {
                        queryJvm(parmas).then(res => {
                            clearInterval(this.JTimer)
                            const JvmData = res.data.rows
                            var j = 4
                            this.JTimer = setInterval(() => {
                                if (this.JvmData.length > 100) {
                                    this.JvmData.shift()
                                }
                                this.JvmData.push(JvmData[j])
                                this.tabJVMInfo(this.JVMInfo)
                                j++
                            }, 1000)
                        })
                    }, 5100)
                })
            }
        },
        TrafficMonitor(MonitorArr, xDate, legendArr) { // TrafficMonitor 图表
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
                    data: MonitorArr,
                    textStyle: {
                        fontSize: 11
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
                            width: '1'// y坐标轴线的宽度
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
            this.listProps()
        }
    }
}
</script>

<style rel="stylesheet/scss" lang="scss">
@import 'src/styles/cluster.scss';
</style>
