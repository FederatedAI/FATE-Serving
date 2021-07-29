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
    <div>
        <el-dialog
            :width="pipelineWidth"
            title="Pipeline"
            custom-class="pipeline-dialog"
            :visible.sync="pipelineVisible"
            @close="closePipeline"
            :fullscreen="fullscreen"
            :top="top"
        >
            <div class="chart-content" :style="{'height':pipelineheight}">
                <div class="fullscreen">
                    <div class="enlarge" @click="setSize('1')" v-if="!fullscreen"><img src="@/assets/zoom-in.png" alt=""></div>
                    <div class="shrink" @click="setSize('2')" v-else><img src="@/assets/zoom-out.png" alt=""></div>
                </div>
                <div class="dag-chart ">
                    <div v-if="dagData.length > 0" :style="{'transform':`scale(${scale})`}">
                        <div class="pipeline-cube" @click="getDagInstance(item,index)" :class="{'active':index === activeIndex}" v-for="(item,index) in dagData" :key="index">{{item.name}}<span class="line" v-if="index < (dagData.length - 1)"></span></div>
                    </div>
                    <div v-else class="no-data">No Data</div>
                    <div v-if="dagData" class="buttonList">
                        <div class="opera_btn" @click="suitableWhole">
                            <i class="el-icon-full-screen" />
                        </div>
                        <div class="opera_btn" @click="bigger">
                            <i class="el-icon-plus" />
                        </div>
                        <div class="opera_btn" @click="small">
                            <i class="el-icon-minus" />
                        </div>
                    </div>
                </div>
                <div class="dag-detail">
                    <div class="detail-title">Parameters({{ parameterCount }})</div>
                    <div v-loading="msgLoading" class="msg bg-dark">
                        <el-tree v-if="treeRefresh" ref="foldParameterTree" :data="paramList" :empty-text="''" :default-expand-all="treeUnfoldAll" :props="defaultPropsForTree" class="bg-dark"/>
                        <div v-if="paramList && paramList.length > 0" class="unfold-tree" @click.stop="unfoldAll">{{ treeUnfoldAll ? 'Fold All' : 'Unfold All' }}</div>
                    </div>
                </div>
            </div>
        </el-dialog>
        <canvas v-show="false" id="historyForDetail" width="1" height="1" style="width:1px;height:1px"/>
    </div>
</template>

<script>
// import { parseTime, formatSeconds } from '@/filters'

export default {
    name: 'pipelinedialog',
    components: {
        // Dag
        // Confirm
    },
    props: {
        pipelineVisible: {
            type: Boolean,
            default() {
                return false
            }
        },
        dagData: {
            type: Array,
            default() {
                return []
            }
        }
    },
    data() {
        return {
            pipelineWidth: '60%',
            pipelineheight: '700px',
            parameterCount: 0,
            activeIndex: 0,
            msgLoading: false,
            treeRefresh: true,
            treeUnfoldAll: false,
            defaultPropsForTree: { label: 'label', children: 'children' },
            paramList: [{
                label: 'NO DATA',
                bold: true
            }],
            componentName: '',
            lastStatus: '',
            paraLoading: false,
            scale: 1,
            top: '12vh',
            fullscreen: false
        }
    },
    watch: {
        dagData: {
            handler(newVal, oldVal) {
                console.log(arguments, 'dagData')
                if (newVal && newVal[0] && newVal[0].name) {
                    this.getParams(newVal[0].name, 0)
                }
            },
            immediate: true
        }
    },
    methods: {
        closePipeline() {
            this.$emit('closePipeline')
        },
        getDagInstance(data, index) {
            console.log(data, 'data')
            this.activeIndex = index
            this.clickComponent(data.name, index)
        },
        clickComponent(componentName, dataIndex) {
            // this.lastStatus = status
            this.componentName = componentName
            // this.lastComponentName = componentName
            this.getParams(componentName, dataIndex)
        },
        getParams(componentName, index) {
            const vm = this
            console.log(this.dagData, 'dagData')
            this.paraLoading = true
            this.parameterCount = 0
            try {
                this.paraLoading = false
                const d = this.dagData[index].params
                // const d = JSON.parse(this.dagData[index].parameterData)
                const checkLevels = function(obj) {
                    const finalParameter = []
                    for (const key in obj) {
                        const midObj = {}
                        if (obj[key] === null) {
                            midObj.label = key + ': null'
                            vm.parameterCount++
                        } else if (typeof obj[key] === 'object' && !Array.isArray(obj[key])) {
                            midObj.label = key
                            midObj.children = checkLevels(obj[key])
                        } else {
                            if (Array.isArray(obj[key])) {
                                let hasObject = false
                                for (const val of obj[key]) {
                                    if (typeof val === 'object' && val) {
                                        hasObject = true
                                        break
                                    }
                                }
                                if (hasObject) {
                                    midObj.label = key + ': ['
                                    const middle = {}
                                    let index = 0
                                    for (const val of obj[key]) {
                                        middle[index] = val
                                        index++
                                    }
                                    midObj.children = checkLevels(middle)
                                    midObj.children.push({ label: ']' })
                                } else {
                                    midObj.label = key + ': [' + obj[key].join(', ') + ']'
                                }
                            } else {
                                midObj.label = key + ': ' + obj[key].toString()
                            }
                            vm.parameterCount++
                        }
                        if (key === 'module') {
                            finalParameter.unshift(midObj)
                        } else {
                            finalParameter.push(midObj)
                        }
                    }
                    return finalParameter
                }
                this.paramList = checkLevels(d)
            } catch {
                this.paraLoading = false
                this.paramList = [{
                    label: 'NO DATA',
                    bold: true
                }]
                this.parameterCount = 0
            }
        },
        unfoldAll() {
            const vm = this
            this.treeUnfoldAll = !this.treeUnfoldAll
            this.treeRefresh = false
            this.$nextTick(() => {
                vm.treeRefresh = true
            })
        },
        suitableWhole() {
            this.scale = 1
        },
        bigger() {
            this.scale += 0.1
        },
        small() {
            this.scale -= 0.1
        },
        setSize(type) {
            if (type === '1') {
                this.top = '0'
                this.fullscreen = true
                this.pipelineheight = '90vh'
            } else if (type === '2') {
                this.top = '12vh'
                this.fullscreen = false
                this.pipelineheight = '700px'
            }
        }
    }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
    .dag-chart{
        width: 100%;
        display: flex;
        vertical-align: middle;
        align-items: center;
        justify-content: center;
        flex-direction: column;
        .pipeline-cube{
            position: relative;
            width: 224px;
            height: 28px;
            line-height: 28px;
            background: #EBEDF0;
            border-radius: 4px;
            margin-bottom: 60px;
            font-family: Roboto;
            font-weight: bold;
            color: #6A6C75;
            text-align: center;
            cursor: pointer;
            span{
                position: absolute;
                top: 28px;
                left: 50%;
                transform: translateX(-50%);
                width: 1px;
                height: 60px;
                background: #DCDDE0;
            }
        }
        .pipeline-cube.active{
            color: #fff;
            background: #217AD9;
        }
        .pipeline-cube:not(:nth-of-type(1))::before{
            position: absolute;
            content: '▼';
            color: #DCDDE0;
            font-size: 16px;
            top:-18px;
            left: 50%;
            transform: translateX(-50%);
        }
    }

    .confirm-dialog {
        .el-dialog {
        height: auto !important;
            .el-dialog__body {
                height: auto !important;
                padding: 30px 20px;
            }
        }
    }
    .disable-color {
        color: #c6c8cc;
        cursor: default;
    }
    .el-tree{
        background: #f5f8fa;
    }

</style>
