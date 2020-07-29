<template>
    <div  class="service-main">
        <div style="height:100%">
            <div class="service-tit">
                Service
            </div>
            <el-table
                :data="serviceData"
                 max-height="690px"
                :header-cell-style="{background:'#fff'}"
                class="table">
                <el-table-column prop="project" label="Project" show-overflow-tooltip />
                <el-table-column sortable  prop="environment" label="Environment" show-overflow-tooltip />
                <el-table-column prop="name" label="Name" show-overflow-tooltip />
                <el-table-column prop="host" label="Host" show-overflow-tooltip />
                <el-table-column prop="port" label="Port" show-overflow-tooltip />
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
                        <el-input class="input" v-else v-model.number="weight" @input='inputweight' placeholder=""></el-input>
                    </template>
                </el-table-column>
                <el-table-column label="Operation">
                    <template slot-scope="scope">
                        <el-button v-if="!(showEdit === scope.$index)" type="text" style="font-size: 16px;margin-left:15px" class="el-icon-edit" @click="edit(scope.row,scope.$index)"></el-button>
                        <span v-else>
                            <el-button type="text" style="font-size: 16px" class="el-icon-check" @click="confirmEdit"></el-button>
                            <el-button type="text" style="font-size: 16px;color:#C6C8CC" class="el-icon-close" @click="showEdit = -1"></el-button>
                        </span>
                        <el-button v-if="scope.row.needVerify" type="text" style="font-size: 16px;margin-left:15px" @click="verify(scope.row)">Verify</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div v-if="fverifyVisible">
                <el-dialog :visible.sync="fverifyVisible" title="Request body" :showClose='cancel' class="fverifydialog" width="35%" :top="fverifydialogTop" center>
                <div class="verifyinput">
                    <el-form ref="json" :model="json" :rules="rules" label-width="0px">
                        <el-form-item prop="verifydata">
                        <el-input
                            :rows="7"
                            v-model="json.verifydata"
                            type="textarea"
                            @blur="inputCompleted"/>
                        </el-form-item>
                    </el-form>
                </div>
                 <span class="dialog-footer  dialog-but">
                    <el-button
                        :disabled="!json.verifydata"
                        type="primary"
                        @click="confirmVerify"
                    >Verify</el-button>
                </span>
                <div v-if="responseVisible">
                    <div class="response">Response</div>
                    <div class="verifyinput">
                        <div class="response-data">
                            {{ responseData }}
                        </div>
                    </div>
                <span slot="footer" class="dialog-footer  dialog-but">
                    <el-button
                        type="primary"
                        @click="fverifyVis"
                    >Close</el-button>
                </span>
                </div>
            </el-dialog>
            </div>
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
import { getserviceList, serviceUpdate, validate } from '@/api/service'
export default {
    name: 'service',
    components: {},
    data() {
        const checkRule = (rule, value, callback) => {
            if (this.isJSON(value)) {
                callback()
            } else {
                callback(new Error('Please enter the text in JSON.'))
            }
        }
        return {
            currentPage: 1,
            total: 0,
            cancel: false,
            serviceData: [],
            showEdit: -1,
            version: '',
            weight: 0,
            rowData: {},
            page: 1,
            pageSize: 20,
            fverifyVisible: false,
            responseVisible: false,
            fverifydialogTop: '15%',
            verifydata: '',
            responseData: '',
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
            routerMode: '',
            json: {
                verifydata: ''
            },
            rules: {
                verifydata: [
                    { trigger: 'blur', validator: checkRule }
                ]
            }
        }
    },
    watch: {
        fverifyVisible: function(val) {
            if (!val) {
                setTimeout(() => {
                    this.responseVisible = false
                    this.fverifydialogTop = '15%'
                    this.json.verifydata = ''
                }, 200)
            }
        }
    },
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
        verify(row) {
            this.rowData = row
            this.fverifyVisible = true
            // this.version = row.version
            // this.weight = row.weight
        },
        fverifyVis() {
            this.fverifyVisible = false
            this.initserviceList()
        },
        isJSON(str) {
            if (typeof str === 'string') {
                try {
                    var obj = JSON.parse(str)
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
        inputCompleted() {
            if (this.isJSON(this.json.verifydata)) {
                this.json.verifydata = JSON.stringify(JSON.parse(this.json.verifydata), null, 4)
            } else {
                return false
            }
        },
        confirmVerify() {
            var reqJson = ''
            this.$refs['json'].validate((valid) => {
                if (valid) {
                    reqJson = this.json.verifydata
                } else {
                    return false
                }
            })
            if (!reqJson) {
                return
            }
            this.responseVisible = true
            this.fverifydialogTop = '9%'
            let params = this.json.verifydata
            params = JSON.parse(params)
            params.host = this.rowData.host
            params.port = this.rowData.port
            // params = JSON.stringify(params)
            validate(params, this.rowData.callName).then(res => {
                // this.responseData = res.data
                this.responseData = JSON.stringify(res.data, null, 4)
                console.log(this.responseData)
            })
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
        // inputversion() {
        //     if (this.version > 100) {
        //         this.version = 100
        //     } else if (this.version < 0 && this.weight !== '') {
        //         this.version = 0
        //     }
        // },
        inputweight() {
            var re = /^[0-9]+$/
            if (!re.test(this.weight)) {
                this.weight = ''
            }
            this.weight = this.weight + ''
            if (this.weight.length >= 3 && this.weight !== '100') {
                if (this.weight.substring(0, 3) === '100') {
                    this.weight = this.weight.substring(0, 3)
                } else {
                    this.weight = this.weight.substring(0, 2)
                }
            } else if (this.weight < 0 && this.weight !== '') {
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
    .el-table td {
        padding: 0;
    }
    .el-table{
        height: calc(100% - 100px);
        overflow-y: auto;
    }
    .el-table th {

        padding: 5px 0;
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
    .fverifydialog {
        .el-dialog__header {
            text-align: left;
            padding: 36px 0px 0px 36px;
            .el-dialog__title {
                font-size:32px;
                font-weight:bold;
                color:rgba(33,122,217,1);
            }
        }
        .response {
            font-size:32px;
            font-weight:bold;
            color:rgba(33,122,217,1);
            padding-bottom: 24px;
        }
         .el-dialog__body {
            padding-left: 36px;
            padding-bottom: 15px;
        }
        .dialog-footer {
            margin: 0 calc(50% - 100px);
            .el-button{
                width:200px;
                height:36px;
                // background:rgba(33,122,217,1);
                opacity:1;
                margin: 24px 0 24px;
            }
        }
        .verifyinput {

            .el-textarea {
                .el-textarea__inner {
                    width: 98%;
                    height: 160px;
                    background-color: #F5F8FA;
                    font-size: 14px;
                    border: none;
                    font-family:Product Sans;
                }
            }
           .response-data {
               overflow-y: auto;
               font-family:Product Sans;
                width: 98%;
                height: 160px;
                background-color: #F5F8FA;
                font-size: 14px;
                padding: 10px 16px;
                box-sizing: border-box;
           }
        }
    }
    .el-form-item__error {
        font-size: 14px;
        margin-top: 3px;
    }
}
.el-popper .popper__arrow {
    display: none;
}
.router-mode {
    margin-left: -20px;
}
</style>
