/**
 *
 *  Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

/**
 * dagInfo : api data
 *
 */
import Layer from '../Core'
import Progress, { FONT_SIZE, FONT_FAMILY } from './modules'
import { calculation } from '../Core/Paths/line'
import { measureText } from '../Core/Paths/text'

let newPort = false
const LINE_STLYE = '#BBBBC8'
const LINE_BRIGHT_STYLE = '#578FFF'
const LINE_WIDTH_FOR_LINKING = 2
const COMP_PADDING = 22
const COMP_HEIGHT_PADDING = 5

const TOPPEST = 20
const COMP_BETWEEN = 15
const INNER_BETWEEN = 30
const LEVEL_BETWEEN = 50

function struct() {
    const lay = this
    const dagInfo = lay.dagInfo
    const images = lay.images
    lay.lineWidthForLinking = lay.lineWidthForLinking || LINE_WIDTH_FOR_LINKING
    lay.padding = lay.padding || COMP_PADDING
    lay.paddingHeight = lay.paddingHeight || COMP_HEIGHT_PADDING
    lay.fontSizeForContent = lay.fontSizeForContent || FONT_SIZE
    lay.toppest = lay.toppest || { x: lay.$canvas.width / 2, y: TOPPEST }
    lay.componentBetween = (lay.componentBetween || COMP_BETWEEN)
    lay.innerBetween = (lay.innerBetween || INNER_BETWEEN)
    lay.levelBetween = (lay.levelBetween || LEVEL_BETWEEN)
    lay.info = new DiagramInfo(lay, dagInfo, images)
    lay.info.resetting()
    lay.$meta.set('clear', lay.info.getStyleOfDag())
    lay._inited = true
}

class DiagramInfo {
    constructor(lay, obj) {
        this.level = []
        this.root = []
        this.linking = {}
        this.components = new Map()
        this.size = 0
        this.lay = lay
        this.checkNewPort(obj.dependencies)
        this.componentInfo(obj.component_list)
        this.componentDisable(obj.component_need_run)
        this.componentBelone(obj.component_module, obj.dependencies)
        this.componentLink(obj.dependencies)
    }
    checkNewPort(obj) {
        newPort = Object.keys(obj).length > 0 ? !!obj[Object.keys(obj)[0]][0]['up_output_info'] : true
    }
    componentInfo(list) {
        for (const val of list) {
            this.components.set(val.component_name, new CompExpression(val))
        }
    }
    componentDisable(obj) {
        for (const key in obj) {
            this.components.get(key).setDisable(!obj[key])
            this.components.get(key).setImage(this.lay.images)
        }
    }
    componentBelone(obj, dep) {
        for (const key in obj) {
            this.components.get(key).setBelone(obj[key], this.lay.images, dep)
        }
    }
    componentLink(obj) {
        const setChild = (name) => {
            for (const key in this.linking) {
                for (const item of this.linking[key]) {
                    if (item.components[0] === name) {
                        const parent = this.components.get(item.components[0])
                        const child = this.components.get(item.components[1])
                        if (child.level <= parent.level) {
                            child.level = parent.level + 1
                            setChild(item.components[1])
                        }
                    }
                }
            }
        }
        for (const name in obj) {
            for (const item of obj[name]) {
                this.linking[item.type] = this.linking[item.type] || []
                this.linking[item.type].push({
                    components: [item.component_name, name],
                    outputType: item.up_output_info ? item.up_output_info.join('') : (item.type + '0')
                })
                setChild(item.component_name)
            }
        }
        for (const val of this.components) {
            this.level[val[1].level] = this.level[val[1].level] || []
            this.level[val[1].level].push(val[1])
            if (val[1].level === 0) {
                this.root.push(val[1])
            }
        }
    }
    componentCheckWidthAndHeight() {
        let longest = ''
        for (const val of this.components) {
            (val[0].length > longest.length) && (longest = val[0])
        }
        const styleInfo = measureText(this.lay.$ctx, longest, { font: this.lay.fontSizeForContent + 'px ' + FONT_FAMILY })
        const width = Math.ceil(styleInfo.width) + this.lay.padding * 2
        const height = this.lay.fontSizeForContent + this.lay.paddingHeight * 2
        this.compWidth = width
        this.compHeight = height
        for (const val of this.components) {
            val[1].width = width
            val[1].height = height
        }
    }
    checkPos() {
        const lay = this.lay
        if (!lay._inited) {
            const style = this.getStyleOfDag()
            if (style.height < (lay.$canvas.height - lay.toppest.y * 2)) {
                lay.toppest.y = (lay.$canvas.height - style.height) / 2
            }
        }
        let top = this.lay.toppest.y
        const width = this.compWidth
        const height = this.compHeight
        const center = this.lay.toppest.x
        const getInList = function(topto, size, index) {
            const firstX = center - (size - 1) * (width + lay.componentBetween) / 2
            return { x: firstX + index * (width + lay.componentBetween), y: index * (height + lay.innerBetween) + topto + height / 2 }
        }
        for (const val of this.level) {
            for (let i = 0; i < val.length; i++) {
                val[i].setPos(getInList(top, val.length, i))
            }
            top = val[val.length - 1].point.y + this.lay.levelBetween + height / 2
        }
    }
    componentGetInstance() {
        for (const val of this.components) {
            val[1].getComponentInstance(this.lay)
        }
    }
    getLinking() {
        this.linkPos = {}
        for (const key in this.linking) {
            this.linkPos[key] = this.linkPos[key] || []
            console.log(this.linking, 'linking')
            console.log(this.linkPos, 'this.linkPos')
            for (const val of this.linking[key]) {
                console.log(val, 'val-linking')
                console.log(this.lay, 'lay')
                console.log(key, 'key')
                const link = { line: [], name: '' }
                link.line.push(this.lay.$children.get(val.components[0]).$meta.get('port').get(val.outputType + 'output'))
                link.line.push(this.lay.$children.get(val.components[1]).$meta.get('port').get(key + 'input'))
                console.log(link, 'link')
                const p = this.components.get(val.components[0])
                const c = this.components.get(val.components[1])
                link.name = p.name + '|' + c.name + '|' + val.outputType + '|' + key + '|line'
                link.corssLevel = ((c.level - p.level) >= 2)
                this.linkPos[key].push(link)
            }
        }
    }
    getLinkInstance() {
        for (const key in this.linkPos) {
            for (const val of this.linkPos[key]) {
                console.log(val, 'val-line')
                const distance = this.lay.levelBetween
                const horizon = val.corssLevel
                    ? (key.match('data') ? -this.compWidth / 3 : this.compWidth / 3)
                    : false
                const points = calculation(val.line[0], val.line[1], horizon, distance)
                Layer.component.line.drawLine({
                    props: {
                        point: points,
                        curve: true,
                        stroke: true,
                        style: {
                            lineWidth: this.lay.lineWidthForLinking * (this.lay._lineBright && val.name.match(this.lay._lineBright) ? 1.5 : 1),
                            strokeStyle: this.lay._lineBright && val.name.match(this.lay._lineBright) ? LINE_BRIGHT_STYLE : LINE_STLYE
                        }
                    }
                }, this.lay, val.name)
            }
        }
    }
    getStyleOfDag() {
        let longest = 0
        let dagWidth = 0
        let dagHeight = 0
        for (const item of this.level) {
            if (item.length > longest) {
                longest = item.length
            }
            dagHeight += item.length * this.compHeight + (item.length - 1) * this.lay.innerBetween + this.lay.levelBetween
        }
        dagHeight -= this.lay.levelBetween
        dagWidth = (longest * this.compWidth) + (longest - 1) * this.lay.componentBetween
        return { width: dagWidth, height: dagHeight }
    }
    resetting() {
        this.componentCheckWidthAndHeight()
        this.checkPos()
        this.componentGetInstance()
        this.getLinking()
        this.getLinkInstance()
    }
}

class CompExpression {
    constructor(obj) {
        this.name = obj.component_name
        this.time = obj.time
        this.images = null
        this.type = this.getStatus(obj.status || 'unrun')
        this.level = 0
        this.width = 0
        this.height = 0
    }
    getStatus(status) {
        const type = status.toUpperCase()
        if (Progress.type.SUCCESS.indexOf(type) >= 0) {
            return Progress.type.SUCCESS
        } else if (Progress.type.FAIL.indexOf(type) >= 0) {
            return Progress.type.FAIL
        } else if (Progress.type.RUNNING.indexOf(type) >= 0) {
            return Progress.type.RUNNING
        } else {
            return Progress.type.UNRUN
        }
    }
    setDisable(disable) {
        this.disable = disable
    }
    setImage(imgs) {
        this.img = imgs.get((this.disable ? 'DISABLE_' : '') + this.type.split('|')[0])
    }
    setBelone(Belone, ports, dep) {
        this.belone = Belone
        this.setPort(ports, dep)
    }
    setPos(point) {
        this.point = point
    }
    checkSetPort(name, model, dep) {
        const split = !newPort ? ['none']
            : ['secureboost', 'linr', 'lr', 'poisson', 'heteronn', 'homonn', 'localbaseline', 'fm', 'mf', 'svd', 'scdpp', 'gmf', 'ftl', 'psi', 'kmeans']
        const deps = dep[name]
        if (dep[name] && dep[name].length > 0) {
            for (const val of deps) {
                if (['model', 'data'].indexOf(val.type) < 0) {
                    return true
                }
            }
            return false
        } else {
            return model.toLowerCase().match(new RegExp('(' + split.join('|') + ')', 'i'))
        }
    }
    setPort(ports, dep) {
        this.input = this.input || []
        this.output = this.output || []
        const TRAINDATA = { name: 'train_datainput', tooltip: 'Train Data Input', type: 'data' }
        const VALDATA = { name: 'validate_datainput', tooltip: 'Validation Data Input', type: 'data' }
        const DATAINPUT = { name: 'datainput', tooltip: 'Data Input', type: 'data' }
        const DATAOUTPUT = { name: 'data0output', tooltip: 'Data Output', type: 'data' }
        const MODELINPUT = { name: 'modelinput', tooltip: 'Model Input', type: 'model' }
        const MODELOUTPUT = { name: 'model0output', tooltip: 'Model Output', type: 'model' }
        if (this.checkSetPort(this.name, this.belone, dep)) {
            this.input.push(...[TRAINDATA, VALDATA])
        } else if (!this.belone.toLowerCase().match('reader')) {
            this.input.push(DATAINPUT)
        }
        if (this.belone.toLowerCase().match(/evaluation|union/i)) {
            this.input[0].mult = ports.get('MULT_DATA_PORT')
        }
        if (!this.belone.toLowerCase().match(/(evaluation|upload|download|pearson|datasplit|statistics|psi|kmeans)/i)) {
            this.output.push(DATAOUTPUT)
        }
        if (this.belone.toLowerCase().match(/(kmeans)/i)) {
            this.output.push(...[{
                name: 'data0output', tooltip: 'Data Output_0', type: 'data'
            }, {
                name: 'data1output', tooltip: 'Data Output_1', type: 'data'
            }])
        }
        if (this.belone.toLowerCase().match(/(datasplit)/i)) {
            this.output.push(...[{
                name: 'data0output', tooltip: 'Train Data Output', type: 'data'
            }, {
                name: 'data1output', tooltip: 'Validation Data Output', type: 'data'
            }, {
                name: 'data2output', tooltip: 'Test Data Output', type: 'data'
            }])
        }
        if (!this.belone.toLowerCase().match(/(intersection|federatedsample|evaluation|upload|download|rsa|datasplit|reader|union|scorecard|sampleweight)/i)) {
            if (!this.belone.toLowerCase().match(/(statistics|pearson|psi)/i)) {
                this.input.push(MODELINPUT)
            }
            // if (!this.belone.toLowerCase().match(/(transformer)/)) {
            this.output.push(MODELOUTPUT)
            // }
            if (this.belone.toLowerCase().match(/(selection)/i)) {
                this.input[1].mult = ports.get('MULT_MODEL_PORT')
            }
        }
    }
    getComponentInstance(lay) {
        Progress.drawProgress({
            props: {
                point: this.point,
                text: this.name,
                width: this.width,
                height: this.height,
                type: lay.$children.get(this.name) ? lay.$children.get(this.name).type : this.type,
                disable: this.disable,
                input: this.input,
                output: this.output,
                specialDataInput: this.specialDataInput,
                contentFontSize: lay.fontSizeForContent,
                time: lay.$children.get(this.name) ? lay.$children.get(this.name).time : this.time,
                img: this.img
            },
            zindex: 1,
            events: Progress.events
        }, lay, this.name)
    }
}

const flowDiagram = {
    drawDiagram(obj, parent, name) {
        obj.canvas = parent ? parent._$canvas : obj.canvas
        obj.struct = struct
        if (parent) {
            if (!name) {
                name = Layer.getUUID('progress')
            }
            parent.drawLayer(name, obj)
            return name
        } else {
            return new Layer(obj)
        }
    },
    SUCCESS: Progress.type.SUCCESS,
    FAIL: Progress.type.FAIL,
    RUNNING: Progress.type.RUNNING,
    UNRUN: Progress.type.UNRUN,
    explaination: DiagramInfo,
    events: {
        scale(time, point = { x: 0, y: 0 }, after) {
            const lay = this
            const trans = lay.$meta.get('$translate') || { x: 0, y: 0 }
            point = { x: point.x - trans.x, y: point.y - trans.y }
            lay.setCus('scale', () => {
                lay.toppest = Layer.scaleDistanceForPoint(lay.toppest, point, time)
                lay.padding = Layer.toFixed(lay.padding * time)
                lay.paddingHeight = Layer.toFixed(lay.paddingHeight * time)
                lay.fontSizeForContent = Layer.toFixed(lay.fontSizeForContent * time)
                lay.lineWidthForLinking = Layer.toFixed(lay.lineWidthForLinking * time)
                lay.componentBetween = Layer.toFixed(lay.componentBetween * time)
                lay.innerBetween = Layer.toFixed(lay.innerBetween * time)
                lay.levelBetween = Layer.toFixed(lay.levelBetween * time)
                if (after) {
                    after.call(lay)
                }
            })
        },
        toSuccess(name, img) {
            const lay = this
            Progress.animations.toNewType.call(lay.$children.get(name), Progress.type.SUCCESS, img)
        },
        toComplete(name, img) {
            const lay = this
            Progress.animations.toNewType.call(lay.$children.get(name), Progress.type.SUCCESS, img)
        },
        toFail(name, img) {
            const lay = this
            Progress.animations.toNewType.call(lay.$children.get(name), Progress.type.FAIL, img)
        },
        toFailed(name, img) {
            const lay = this
            Progress.animations.toNewType.call(lay.$children.get(name), Progress.type.FAIL, img)
        },
        toCanceled(name, img) {
            const lay = this
            Progress.animations.toNewType.call(lay.$children.get(name), Progress.type.FAIL, img)
        },
        toCancel(name, img) {
            const lay = this
            Progress.animations.toNewType.call(lay.$children.get(name), Progress.type.FAIL, img)
        },
        toError(name, img) {
            const lay = this
            Progress.animations.toNewType.call(lay.$children.get(name), Progress.type.FAIL, img)
        },
        toRunning(name, time) {
            const lay = this
            Progress.animations.toNewType.call(lay.$children.get(name), Progress.type.RUNNING, time)
        },
        toWaiting(name) {
            const lay = this
            Progress.animations.toNewType.call(lay.$children.get(name), Progress.type.UNRUN)
        },
        toUnrun(name) {
            const lay = this
            Progress.animations.toNewType.call(lay.$children.get(name), Progress.type.UNRUN)
        },
        toType(obj) {
            const lay = this
            for (const key in obj) {
                Progress.animations.toNewType.call(lay.$children.get(key), obj[key].type, obj[key].props)
            }
        },
        lineBright(text) {
            this._lineBright = text
            const connection = []
            this.$children.forEach((val, key) => {
                if (text && key === text) {
                    val.$zindex = 2
                } else if (text && key.match(text) && !key.match('_' + text) && !key.match(text + '_')) {
                    const name = key.split('|')
                    name.splice(name.indexOf(text), 1)
                    val.$zindex = 1
                    val.style.strokeStyle = LINE_BRIGHT_STYLE
                    val.style.lineWidth *= 1.5
                    this.$children.get(name[0]).containerLightUp = LINE_BRIGHT_STYLE
                    this.$children.get(name[0]).$struct()
                    connection.push(name[0])
                } else if (key.match('line')) {
                    val.$zindex = 0
                    val.style.strokeStyle = LINE_STLYE
                    val.style.lineWidth = this.lineWidthForLinking
                } else if (connection.indexOf(key) < 0) {
                    val.$zindex = 1
                    val.containerLightUp = ''
                }
            })
        }
    }
}

export default flowDiagram
