<template>
    <div  class="service-main">
        <div style="height:100%">
            <div class="service-tit">
                Service
            </div>
            <el-table
                :data="serviceData"
                :header-cell-style="{background:'#fff'}"
                height="calc(100% - 100px)"
                class="table">
                <el-table-column sortable  prop="project" label="Project" show-overflow-tooltip />
                <el-table-column sortable  prop="environment" label="Environment" show-overflow-tooltip />
                <el-table-column sortable  prop="name" label="Name" show-overflow-tooltip />
                <el-table-column sortable  prop="host" label="Host" show-overflow-tooltip />
                <el-table-column sortable  prop="port" label="Port" show-overflow-tooltip />
                <!-- <el-table-column sortable width="200px" prop="routerMode" label="Router Mode" show-overflow-tooltip>
                    <template slot-scope="scope">
                        <span v-if="!(showEdit === scope.$index)">
                            {{ scope.row.routerMode }}
                        </span>
                        <div v-else>
                            <el-select popper-class="router-mode" v-model="routerMode" placeholder="请选择">
                                <el-option
                                v-for="item in options"
                                :key="item.value"
                                :label="item.label"
                                :value="item.value">
                                </el-option>
                            </el-select>
                        </div>
                    </template>
                </el-table-column> -->
                <!-- <el-table-column sortable  prop="version" label="Version" show-overflow-tooltip>
                    <template slot-scope="scope">
                        <span v-if="!(showEdit === scope.$index)">
                            {{ scope.row.version }}
                        </span>
                        <el-input class="input"  v-else v-model.number="version" @input='inputversion' placeholder="请输入version"></el-input>
                    </template>
                </el-table-column> -->
                <el-table-column sortable  prop="weight" label="Weight" show-overflow-tooltip>
                    <template slot-scope="scope">
                        <span v-if="!(showEdit === scope.$index)">
                            {{ scope.row.weight }}
                        </span>
                        <el-input class="input" v-else v-model.number="weight" @input='inputweight' placeholder="请输入weight"></el-input>
                    </template>
                </el-table-column>
                <el-table-column label="Operation">
                    <template slot-scope="scope">
                        <el-button v-if="!(showEdit === scope.$index)" type="text" style="font-size: 16px;margin-left:15px" class="el-icon-edit" @click="edit(scope.row,scope.$index)"></el-button>
                        <span v-else>
                            <el-button type="text" style="font-size: 16px" class="el-icon-check" @click="confirmEdit"></el-button>
                            <el-button type="text" style="font-size: 16px" class="el-icon-close" @click="showEdit = -1"></el-button>
                        </span>
                    </template>
                </el-table-column>
            </el-table>
            <div class="pagination">
                <el-pagination
                    background
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                    :current-page.sync="currentPage"
                    :page-size="20"
                    layout="total, prev, pager, next, jumper"
                    :total="total"
                ></el-pagination>
            </div>
        </div>
    </div>
</template>

<script>
import { getserviceList, serviceUpdate } from '@/api/service'
export default {
    name: 'service',
    components: {},
    data() {
        return {
            currentPage: 1,
            total: 0,
            serviceData: [],
            showEdit: -1,
            version: '',
            weight: '',
            rowData: {},
            page: 1,
            pageSize: 20,
            options: [{
                value: 'ALL_ALLOWED',
                label: 'ALL_ALLOWED'
            }, {
                value: 'VERSION_BIGER',
                label: 'VERSION_BIGER'
            }, {
                value: 'VERSION_BIGTHAN_OR_EQUAL',
                label: 'VERSION_BIGTHAN_OR_EQUAL'
            }, {
                value: 'VERSION_SMALLER',
                label: 'VERSION_SMALLER'
            }, {
                value: 'VERSION_EQUALS',
                label: 'VERSION_EQUALS'
            }],
            routerMode: ''
        }
    },
    watch: {},
    computed: {},
    created() {
        this.initserviceList()
    },
    methods: {
        initserviceList() {
            const params = {
                page: this.page,
                pageSize: this.pageSize
            }
            getserviceList(params).then(res => {
                this.serviceData = res.data.rows
                this.total = res.data.total
            })
        },
        handleSizeChange(val) {
        },
        handleCurrentChange(val) {
            this.page = val
            this.initserviceList()
        },
        edit(row, index) {
            this.showEdit = index
            this.rowData = row
            // this.routerMode = row.routerMode
            // this.version = row.version
            this.weight = row.weight
        },
        confirmEdit() {
            this.showEdit = -1
            const params = {
                host: this.rowData.host,
                port: this.rowData.port,
                url: this.rowData.url,
                // routerMode: this.routerMode,
                // version: this.version,
                weight: this.weight
            }
            serviceUpdate(params).then(res => {
                this.initserviceList()
            })
        },
        inputversion() {
            if (this.version > 100) {
                this.version = 100
            } else if (this.version < 0 && this.weight !== '') {
                this.version = 0
            }
        },
        inputweight() {
            if (this.weight < 0 && this.weight !== '') {
                this.weight = 0
            }
        }
    }
}
</script>

<style rel="stylesheet/scss" lang="scss">
.service-main {
    height: calc(100vh - 158px);
    background: #fff;
    margin: 20px 40px 20px 40px ;
    padding-left: 20px;
    .service-tit {
        font-family:Product Sans;
        font-weight:bold;
        line-height:20px;
        color:rgba(33,122,217,1);
        opacity:1;
        font-size: 24px;
        margin-left: 10px;
        padding-top: 25px;
    }
    .el-select {
        border-bottom: 2px solid #B8BFCC;
        .el-input__inner {
            width: 125px;
        }
        .el-input__suffix {
            right: -25px;
        }
    }
    .el-select,.el-input {
        width: 85%;
        .el-input__icon {
            width: 25px;
            line-height: 28px;
        }
        .el-input__inner {
            padding: 0;
            background-color: transparent;
            height: 28px;
            line-height: 25px;
        }
        .el-input__inner:hover {
            background: #f5f7fa;
        }
    }
    .input {
        background-color: #F5F8FA;
        width: 110px;
        padding: 0 5px;
        border-bottom: 2px solid #B8BFCC;
    }
}
.el-popper .popper__arrow {
    display: none;
}
.router-mode {
    margin-left: -20px;
}
</style>
