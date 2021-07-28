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
            title="Pipeline"
            :width="pipelineWidth"
            custom-class="pipeline-dialog"
            :visible.sync="pipelineVisible"
            @close="closePipeline"
        >
            <div class="chart-content">
                <div class="dag-chart ">
                    <dag ref="dagForJobFlow" :dag-info="dagData" @choose="getDagInstance" @retry="jobRetry"/>
                </div>
                <div class="dag-detail">
                    <div class="detail-title">Parameters({{ parameterCount }})</div>
                    <div v-loading="msgLoading" class="msg bg-dark">
                        <el-tree v-if="treeRefresh" ref="foldParameterTree" :data="paramList" :empty-text="''" :default-expand-all="treeUnfoldAll" :props="defaultPropsForTree" class="bg-dark"/>
                        <div v-if="paramList && paramList.length > 0" class="unfold-tree" @click.stop="unfoldAll">{{ treeUnfoldAll ? 'fold all' : 'unfold all' }}</div>
                    </div>
                </div>
            </div>
        </el-dialog>
        <canvas v-show="false" id="historyForDetail" width="1" height="1" style="width:1px;height:1px"/>
        <!-- <confirm ref="confirm" class="confirm-dialog"/> -->
    </div>
</template>

<script>

import Dag from '@/components/CanvasComponent/flowDiagram'
import { parseTime, formatSeconds } from '@/filters'
// import Confirm from '@/views/job-dashboard/board/Confirm'

export default {
    name: 'pipelinedialog',
    components: {
        Dag
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
            type: Object,
            default() {
                return {}
            }
        }
    },
    data() {
        return {
            pipelineWidth: '60%',
            parameterCount: 0,
            msgLoading: false,
            treeRefresh: true,
            treeUnfoldAll: false,
            dataOutputShow: true,
            defaultPropsForTree: { label: 'label', children: 'children' },
            paramList: [],
            jobId: this.$route.query.job_id,
            role: this.$route.query.role,
            partyId: this.$route.query.party_id,
            jobFrom: this.$route.query.from,
            summaryLoading: true,
            roleList: [],
            jobInfo: {},
            componentName: '',
            lastStatus: '',
            logLoading: false,
            dagInstance: null,
            // graphOptions,
            // outputGraphOptions: graphOptions,
            paraLoading: false,
            DAGData: null,
            outputVisible: false,
            modelOutputType: '',
            outputTitle: '',
            currentTab: 'model',
            logWebsocket: null,
            timer: null,
            modelOutputShowing: true,
            noteHint: false,
            notePopover: false,
            foldButtonForNote: true,
            foldPForNote: 'notes-content-p',
            scrollTopPos: 0,
            refreshCheck: false,
            scrollHoldChange: false,
            breads: [],
            popover: [],
            downloadList: [],
            useLogic: false,
            variableMap: []

        }
    },
    watch: {
        dagData: {
            handler(newVal, oldVal) {
                console.log(arguments, 'dagData')
                if (newVal && newVal.component_list) {
                    this.$emit('showPipeline')
                }
            },
            immediate: true
        }
    },
    methods: {
        closePipeline() {
            this.$emit('closePipeline')
        },
        getDagInstance(data) {
            console.log(data, 'data')
            // if (data.model === this.modelNameMap.correlation || data.model === this.modelNameMap.evaluation) {
            //     this.dataOutputShow = false
            // } else {
            //     this.dataOutputShow = true
            // }
            this.clickComponent(data.name, data.dataIndex, data.model, data.disable, data.status)
        },
        clickComponent(componentName, dataIndex, componentType, disable, status) {
            let couldBeNeedRefresh = false
            if (componentName === this.componentName) {
                couldBeNeedRefresh = this.lastStatus !== status
            }
            this.lastStatus = status
            this.componentName = componentName
            this.lastComponentName = componentName
            // this.modelOutputType = componentType || ''
            // this.outputTitle = this.modelOutputType ? `${componentType}: ${componentName}` : ''
            // // this.clickComponentChangeStyle(this.graphOptions.series[0].data, dataIndex)
            // // this.dagInstance.setOption(this.graphOptions)
            if (!disable) {
                this.getParams(componentName, dataIndex)
            } else {
                this.paramList = [{
                    label: 'NO DATA',
                    bold: true
                }]
                this.parameterCount = 0
                this.componentName = ''
            }
            if (couldBeNeedRefresh) {
                this.$refs['outputDialog'].refresh()
            }
        },
        getParams(componentName, index) {
            const vm = this
            console.log(this.dagData, 'dagData')
            this.paraLoading = true
            this.parameterCount = 0
            try {
                this.paraLoading = false
                const d = JSON.parse(this.dagData[index].parameterData)
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
        jobRetry(name) {
            // const vm = this
            // const confirmText = [`The job will continue from where it ${this.jobInfo.status}`, 'it may take few seconds to  update job status.']
            // this.$refs.confirm
            //     .confirm(...confirmText)
            //     .then(() => {
            //         vm.restartJobWebsocket(name)
            //     })
        },
        restartJobWebsocket(name) {
            // retryJob({
            //     job_id: this.jobId,
            //     componentName: name
            // }).then(res => {
            //     this.initJobSocket()
            // })
        },
        // initJobSocket() {
        //     if (!this.ws) {
        //         const { jobId, role, partyId } = this
        //         if (!jobId || !role || !partyId) {
        //             console.warn(`Missing required parameters`)
        //         }
        //         this.ws = new ReconnectingWebSocket(
        //             `/websocket/progress/${jobId}/${role}/${partyId}`
        //         )
        //         this.summaryLoading = true
        //         this.ws.addEventListener('message', event => {
        //             this.summaryLoading = false
        //             let data
        //             try {
        //                 data = JSON.parse(event.data)
        //             } catch (error) {
        //                 this.ws.close()
        //                 data = null
        //                 return
        //             }
        //             this.handleMessage(data)
        //         })
        //     }
        //     return this.ws
        // },
        handleMessage(data) {
            if (!data) {
                return
            }
            this.updateJob(data)
            this.summaryLoading = false
            this.setData(data)
            if (this.isDone(data.status)) {
                if (this.ws) {
                    this.ws.close()
                    this.ws = ''
                }
            }
        },
        setData(data) {
            const { summary_date: { job, dataset: _dataset }, dependencyData } = data
            if (_dataset) {
                this.roleList = this.transformDataset(_dataset)
            }
            if (job) {
                this.jobInfo = this.transformJobInfo(job)
                this.$nextTick(() => {
                    this.notesHint()
                })
            }
            if (dependencyData) {
                this.DAGData = this.transformDAGData(dependencyData)
            }
        },
        transformDataset({ roles, dataset }) {
            return Object.keys(roles).map(role => {
                const datasetList = roles[role].map(name => {
                    let set = ''
                    if (dataset[role]) {
                        set = Object.values(dataset[role][name]).join(', ')
                    }
                    return {
                        name,
                        dataset: set
                    }
                })
                return {
                    role: role.toUpperCase(),
                    datasetList
                }
            })
        },
        transformJobInfo(job) {
            return {
                submmissionTime: job.fCreateTime ? parseTime(new Date(job.fCreateTime)) : '',
                startTime: job.fStartTime ? parseTime(new Date(job.fStartTime)) : '',
                endTime: job.fEndTime ? parseTime(new Date(job.fEndTime)) : '',
                duration: job.fElapsed ? formatSeconds(job.fElapsed) : '',
                status: job.fStatus ? job.fStatus : '',
                notes: job.fDescription ? job.fDescription : ''
            }
        },
        transformDAGData(data) {
            return data
        },
        notesHint() {
            const cvs = document.getElementById('historyForDetail').getContext('2d')
            const width = this.measureText(cvs, this.jobInfo.notes || '', { size: 14, weight: 'bold' }).width
            const acWidth = parseInt(getComputedStyle(document.getElementById('notesP')).width.replace('px', ''))
            this.noteHint = width > (acWidth * 3) - 45
        },
        measureText(ctx, text, style) {
            for (const key in style) {
                ctx[key] = style[key]
            }
            return ctx.measureText(text)
        },
        unfoldAll() {
            const vm = this
            this.treeUnfoldAll = !this.treeUnfoldAll
            this.treeRefresh = false
            this.$nextTick(() => {
                vm.treeRefresh = true
            })
        }
    }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
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
