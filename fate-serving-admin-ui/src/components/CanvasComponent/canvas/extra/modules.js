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
 * state: {
 *  point: {x, y}
 *  width: Number,
 *  Height: Number, | padding: Number
 *  type: ENUMS:[],
 *  choose：Boolean || false,
 *  disable: Boolean,
 *  progress: Number,
 *  img: imageObject,
 *  input: [{name: String, tooltip:String, mult: Boolean}]
 *  output: [{text: String, tooltip:String, mult: Boolean}]
 *  time: string,
 *  text: String,
 *  radius: Number
 * }
 */

import Layer from '../Core'
import { measureText } from '../Core/Paths/text'

const LINEWIDTH = 8
const COMP_PADDING = 22
const BACKGROUND = '#ffffff'
const CHOOSE = '#4159D1'
const SUCCESS = '#0EC7A5'
const PROGRESS = 'rgba(36,182,139,0.6)'
const DISABLE_PROGRESS = 'rgba(187,187,200,0.6)'
const ERROR = '#FF4F38'
const UNRUN = '#e8e8ef'
const COULDNOTRUN = '#BBBBC8'
const RECT_RADIUS = 4

const BALCK_TEXT = '#6A6C75'
const WHITE_TEXT = '#ffffff'
const DISABLE_TEXT = '#534C77'
export const FONT_SIZE = 16
export const FONT_FAMILY = 'arial'
const TIME_TEXT = '#999BA3'
const LINK_TEXT = '#494ECE'

const BETWEEN_ICON_WITH_CONTENT = 6
const ICON_PADDING = 10

const PORT_WIDTH = 12
const SPEC_PORT_WIDTH = 20
const PORT_HEIGHT = 4
const PORT_RADIUS = 2
const PORT_BETWEEN = 2
const TOOLTIP_FONT = 12
const TRANGLE_SIZE = 5
const TOOLTIP_PADDING = 6
const TOOLTIP_RADIUS = 4
const TOOLTIP_BACKGROUND = 'rgba(127,125,142,0.7)'
const TOOLTIP_FONT_STYLE = 'rgba(255,255,255,1)'
const DATA_PORT_COLOR = '#E6B258'
// const SPECIAL_DATA_PORT_COLOR = '#E6B258'
const MODEL_PORT_COLOR = '#00cbff'
const DISABLE_INIT_COLOR = '#7F7D8E'
const DISABLE_NO_INIT_COLOR = '#7F7D8E'

const progressComp = {
    drawProgress(obj, parent, name) {
        obj.canvas = parent ? parent.$canvas : obj.canvas
        obj.struct = struct
        let newLayer = true
        let currentlay = null
        if (parent) {
            if (!name) {
                name = Layer.getUUID('progress')
            }
            const mid = parent.drawLayer(name, obj)
            newLayer = !mid.old
            currentlay = mid.node
            if (newLayer && progressComp.type.RUNNING.match(currentlay.type.toUpperCase())) {
                progressComp.animations.tictok.call(currentlay)
                progressComp.animations.loading.call(currentlay)
            }
            return name
        } else {
            currentlay = new Layer(obj)
            if (progressComp.type.RUNNING.match(currentlay.type.toUpperCase())) {
                progressComp.animations.tictok.call(currentlay)
                progressComp.animations.loading.call(currentlay)
            }
            return currentlay
        }
    },
    clear,
    type: {
        UNRUN: 'UNRUN|WAITING',
        RUNNING: 'RUNNING',
        FAIL: 'FAILED|ERROR|CANCELED',
        SUCCESS: 'SUCCESS|COMPLETE'
    },
    events: {
        choose(point, afterChoose, ...props) {
            const lay = this
            if (lay.$here(point)) {
                lay.choose = true
            } else {
                lay.choose = false
            }
            lay.setCus('choose', () => {
                lay.containerStyle = _getContainerColor(lay.type, !!lay.choose, lay.disable)
                lay.contentStyle = _getContainerColor(lay.type, !!lay.choose, lay.disable, 'content')
                lay.fontStyle = _getTextColor(lay.type, !!lay.choose, lay.disable)
                if (afterChoose && typeof afterChoose === 'function') afterChoose(lay.text, lay.$here(point))
            })
        },
        linkChoose(point, afterChoose, ...props) {
            const lay = this
            if (lay.$here(point)) {
                lay.setCus('choose', () => {
                    if (afterChoose && typeof afterChoose === 'function') afterChoose(lay.$parent.text, lay.$here(point), true)
                })
                return 'finish'
            }
        },
        scale(time, point = { x: 0, y: 0 }) {
            const lay = this
            lay._scale = (!lay._scale ? 1 : lay._scale) * time
            lay.setCus('scale', () => {
                // lay.point = Layer.scaleDistanceForPoint(lay.point, point, time)
                // if (lay.width) lay.width = Layer.toFixed(lay.width * time)
                // if (lay.height) lay.height = Layer.toFixed(lay.height * time)
                // if (lay.padding) lay.padding = Layer.toFixed(lay.padding * time)
                lay.contentFontSize = Layer.toFixed(lay.contentFontSize * time)
                lay.iconPadding = Layer.toFixed(lay.iconPadding * time)
                lay.betweenIconWithContent = Layer.toFixed(lay.betweenIconWithContent * time)
                lay.lineWidth = Layer.toFixed(lay.lineWidth * time)
                lay.portWidth = Layer.toFixed(lay.portWidth * time)
                lay.portHeight = Layer.toFixed(lay.portHeight * time)
                lay.portRadius = Layer.toFixed(lay.portRadius * time)
                lay.betweenPortWidthTooltip = Layer.toFixed(lay.betweenPortWidthTooltip * time)
                // lay.tooltipFont = Layer.toFixed(lay.tooltipFont * time)
                // if (lay.tooltipFont < TOOLTIP_FONT || lay._scale < 1) lay.tooltipFont = TOOLTIP_FONT
                // lay.tipTrangleSize = Layer.toFixed(lay.tipTrangleSize * time)
                // if (lay.tipTrangleSize < TRANGLE_SIZE || lay._scale < 1) lay.tipTrangleSize = TRANGLE_SIZE
                lay.radius = Layer.toFixed(lay.radius * time)
                // lay.tooltipPadding = Layer.toFixed(lay.tooltipPadding * time)
                // if (lay.tooltipPadding < TOOLTIP_PADDING || lay._scale < 1) lay.tooltipPadding = TOOLTIP_PADDING
                // lay.tooltipRadius = Layer.toFixed(lay.tooltipRadius * time)
                // if (lay.tooltipRadius < TOOLTIP_RADIUS || lay._scale < 1) lay.tooltipRadius = TOOLTIP_RADIUS
            })
        },
        showTips(point) {
            const lay = this
            const check = function(name) {
                const tip = lay.$children.get(name)
                if (tip) {
                    if (tip.$here(point)) {
                        tip.setCus('showToolTip', () => {
                            lay.$children.get(name + 'Tooltip').emit('$showing')
                        })
                    } else {
                        if (lay.$children.get(name + 'Tooltip').$visiable) {
                            tip.setCus('hideTooltip', () => {
                                lay.$children.get(name + 'Tooltip').emit('$hide')
                            })
                        }
                    }
                }
            }
            if (lay.input.length > 0) {
                for (const item of lay.input) {
                    check(item.name)
                }
            }
            if (lay.output.length > 0) {
                for (const item of lay.output) {
                    check(item.name)
                }
            }
        }
    },
    animations: {
        tictok() {
            const lay = this
            lay.registerChainTranslate('tictok', true, lay.settingCus(() => {
                if (!lay.time.match(':')) {
                    lay.time = Layer.exchangeTime(lay.time)
                }
                const time = lay.time.split(':')
                let hou = parseInt(time[0])
                let min = parseInt(time[1])
                let sec = parseInt(time[2])
                if (sec + 1 >= 60) {
                    sec = 0
                    if (min + 1 >= 60) {
                        min = 0
                        hou += 1
                    } else {
                        min += 1
                    }
                } else {
                    sec += 1
                }
                hou = (hou.toString().length < 2 ? '0' : '') + hou
                min = (min.toString().length < 2 ? '0' : '') + min
                sec = (sec.toString().length < 2 ? '0' : '') + sec
                lay.time = hou + ':' + min + ':' + sec
            }, 900))
        },
        loading() {
            const lay = this
            const changeList = []
            changeList.push(lay.settingRGBA(lay.contentStyle, 'rgba(36,182,139,0)', (to) => { lay.contentStyle = to }, 500))
            changeList.push(lay.settingCus(() => {
                lay.progress = 0
                lay.contentStyle = _getContainerColor(lay.type || progressComp.type.UNRUN, !!lay.choose, lay.disable, 'content')
            }))
            changeList.push(lay.settingNum(0, 1, (to) => { lay.progress = to }, 3000, 20))
            lay.registerChainTranslate('loading', true, ...changeList)
        },
        toNewType(type, timeOrPic) {
            const lay = this
            if (type === lay.type) {
                // if (type === progressComp.type.RUNNING) {
                //   lay.setCus('setTime', () => {
                //     lay.time = Layer.exchangeTime(timeOrPic) || lay.time || '00:00:00'
                //   })
                // }
                return
            }
            const changeList = { toNewTypeContent: [], toNewTypeContainer: [], toNewTypeText: [] }
            if (type !== progressComp.type.RUNNING) {
                lay.deleteChainTranslate('loading')
                lay.deleteChainTranslate('tictok')
                changeList.toNewTypeContent.push(lay.settingNum(lay.progress || 1, 1, (to) => { lay.progress = to }, 200))
            }
            const toColor = _getContainerColor(type, !!lay.choose, lay.disable, 'content')
            changeList.toNewTypeContent.push(lay.settingRGBA(lay.contentStyle, toColor, (to) => { lay.contentStyle = to }, 200))
            changeList.toNewTypeContent.push(lay.settingCus(() => {
                lay.type = type
                if (type === progressComp.type.RUNNING) {
                    lay.progress = 0
                    lay.time = Layer.exchangeTime(timeOrPic) || lay.time || '00:00:00'
                    if (!lay.$children.get('time').$visiable) {
                        lay.$children.get('time').emit('$showing')
                    }
                    progressComp.animations.tictok.call(lay)
                    progressComp.animations.loading.call(lay)
                } else {
                    if (lay.$children.get('time').$visiable) {
                        lay.$children.get('time').emit('$hide')
                    }
                }
                if (type === progressComp.type.SUCCESS || type === progressComp.type.FAIL) {
                    lay.img = timeOrPic
                    if (!lay.$children.get('icon').$visiable) {
                        lay.$children.get('icon').emit('$showing')
                    }
                } else {
                    if (lay.$children.get('icon').$visiable) {
                        lay.$children.get('icon').emit('$hide')
                    }
                }
                if (type === progressComp.type.FAIL && !lay.$children.get('retryBtn').$visiable) {
                    lay.$children.get('retryBtn').emit('$showing')
                } else if (lay.$children.get('retryBtn').$visiable) {
                    lay.$children.get('retryBtn').emit('$hide')
                }
            }))
            const toContainerColor = _getContainerColor(type, !!lay.choose, lay.disable)
            changeList.toNewTypeContainer.push(lay.settingRGBA(lay.containerStyle, toContainerColor, (to) => { lay.containerStyle = to }, 200))
            const toFontStyle = _getTextColor(type, !!lay.choose, lay.disable)
            changeList.toNewTypeText.push(lay.settingRGBA(lay.fontStyle, toFontStyle, (to) => { lay.fontStyle = to }, 200))
            for (const key in changeList) {
                lay.registerChainTranslate(key, false, ...changeList[key])
            }
        }
    }
}

function _getContainerColor(type, choose, disable, part) {
    if (choose) {
        if (part === 'content' && type === progressComp.type.RUNNING) {
            if (disable) {
                return DISABLE_PROGRESS
            } else {
                return PROGRESS
            }
        } else {
            return CHOOSE
        }
    } else if (disable && type !== progressComp.type.UNRUN) {
        return UNRUN
    } else if (disable && type === progressComp.type.UNRUN) {
        return COULDNOTRUN
    } else if (type === progressComp.type.SUCCESS) {
        return SUCCESS
    } else if (type === progressComp.type.RUNNING) {
        if (part === 'content') {
            if (disable) {
                return DISABLE_PROGRESS
            } else {
                return PROGRESS
            }
        } else {
            return SUCCESS
        }
    } else if (type === progressComp.type.FAIL) {
        return ERROR
    } else if (type === progressComp.type.UNRUN) {
        return UNRUN
    }
}

function _getTextColor(type, choose, disable) {
    if (disable && type !== progressComp.type.UNRUN) {
        if (choose) {
            return WHITE_TEXT
        } else {
            return BALCK_TEXT
        }
    } else if (disable && type === progressComp.type.UNRUN) {
        if (choose) {
            return WHITE_TEXT
        } else {
            return DISABLE_TEXT
        }
    } else if (type === progressComp.type.SUCCESS) {
        return WHITE_TEXT
    } else if (type === progressComp.type.RUNNING) {
        return BALCK_TEXT
    } else if (type === progressComp.type.FAIL) {
        return WHITE_TEXT
    } else if (type === progressComp.type.UNRUN) {
        return BALCK_TEXT
    }
}

function getStyle(type, choose, disable, lay) {
    if (lay) {
        lay.containerStyle = lay.containerStyle || _getContainerColor(type || progressComp.type.UNRUN, !!choose, disable)
        lay.contentStyle = lay.contentStyle || _getContainerColor(type || progressComp.type.UNRUN, !!choose, disable, 'content')
        lay.fontStyle = lay.fontStyle || _getTextColor(type || progressComp.type.UNRUN, !!choose, disable)
    }
    const style = {
        container: {
            fillStyle: lay ? lay.containerStyle : _getContainerColor(type || progressComp.type.UNRUN, !!choose, disable)
        },
        content: {
            fillStyle: lay ? lay.contentStyle : _getContainerColor(type || progressComp.type.UNRUN, !!choose, disable, 'content')
        },
        text: {
            font: lay.contentFontSize + 'px ' + FONT_FAMILY,
            fillStyle: lay.fontStyle
        },
        time: {
            font: lay.contentFontSize * 0.8 + 'px ' + FONT_FAMILY,
            fillStyle: TIME_TEXT
        },
        link: {
            font: lay.contentFontSize * 0.8 + 'px ' + FONT_FAMILY,
            fillStyle: LINK_TEXT,
            cursor: 'pointer',
            underline: true
        }
    }
    lay.style = style
    return lay.style
}

function struct() {
    const lay = this
    lay.choose = !!lay.choose
    lay.contentFontSize = lay.contentFontSize || FONT_SIZE
    lay.padding = lay.padding || COMP_PADDING
    lay.tooltipRadius = lay.tooltipRadius || TOOLTIP_RADIUS
    lay.iconPadding = lay.iconPadding || ICON_PADDING
    lay.betweenIconWithContent = lay.betweenIconWithContent || BETWEEN_ICON_WITH_CONTENT
    lay.lineWidth = lay.lineWidth || LINEWIDTH
    lay.portWidth = lay.portWidth || (!lay.specialDataInput ? PORT_WIDTH : SPEC_PORT_WIDTH)
    lay.portHeight = lay.portHeight || PORT_HEIGHT
    lay.portRadius = lay.portRadius || PORT_RADIUS
    lay.betweenPortWidthTooltip = lay.betweenPortWidthTooltip || PORT_BETWEEN
    lay.tooltipFont = lay.tooltipFont || TOOLTIP_FONT
    lay.tipTrangleSize = lay.tipTrangleSize || TRANGLE_SIZE
    lay.radius = lay.radius || RECT_RADIUS
    lay.tooltipPadding = lay.tooltipPadding || TOOLTIP_PADDING
    if (lay.type === progressComp.type.RUNNING) {
        lay.time = Layer.exchangeTime(lay.time) || '00:00:00'
    }

    const input = lay.input
    const output = lay.output
    const x = lay.point.x || lay.point[0] || 0
    const y = lay.point.y || lay.point[1] || 0
    const drawingStyle = getStyle(lay.type, lay.choose, lay.disable, lay)
    const fontStyle = measureText(lay.$ctx, lay.text, drawingStyle.text)
    drawingStyle.container.fillStyle = lay.containerLightUp || drawingStyle.container.fillStyle
    const w = lay.width || Math.ceil(fontStyle.width) + lay.padding * 2 + lay.lineWidth * 2
    const h = lay.height || lay.contentFontSize + lay.padding
    const type = lay.type
    const inoutPutmap = new Map()

    const iconW = h - (lay.iconPadding)
    const betweenIconWithContent = lay.betweenIconWithContent
    // drawing maintain
    Layer.component.rect.drawRect({
        props: {
            point: { x, y },
            width: w,
            height: h,
            radius: lay.radius,
            style: drawingStyle.container,
            fill: true
        }
    }, lay, 'container')
    Layer.component.rect.drawRect({
        props: {
            point: { x, y },
            width: w - lay.lineWidth * 0.6,
            height: h - lay.lineWidth * 0.6,
            radius: lay.radius,
            style: {
                fillStyle: BACKGROUND
            },
            fill: true
        }
    }, lay, 'containerInner')
    Layer.component.rect.drawRect({
        props: {
            point: { x, y },
            width: w - lay.lineWidth * 0.5,
            height: h - lay.lineWidth * 0.5,
            progress: lay.progress !== undefined ? lay.progress : 1,
            radius: lay.radius,
            style: drawingStyle.content,
            fill: true
        }
    }, lay, 'content')
    Layer.component.text.drawText({
        props: {
            point: { x, y },
            text: lay.text,
            style: drawingStyle.text,
            position: Layer.component.text.CENTER
        }
    }, lay, 'text')
    // drawing appendix
    Layer.component.icon.drawIcon({
        props: {
            point: { x: x + w / 2 + betweenIconWithContent + iconW / 2, y },
            width: iconW,
            img: lay.img
        },
        visiable: !!lay.img
    }, lay, 'icon')
    Layer.component.text.drawText({
        props: {
            point: { x: x + w / 2 + betweenIconWithContent, y },
            text: lay.time,
            style: drawingStyle.time
        },
        visiable: type === progressComp.type.RUNNING
    }, lay, 'time')

    // drawing retry
    Layer.component.text.drawText({
        props: {
            point: { x: x + w / 2 + betweenIconWithContent * 2 + iconW, y },
            text: 'retry',
            style: drawingStyle.link
        },
        visiable: type === progressComp.type.FAIL,
        events: {
            choose: progressComp.events.linkChoose
        }
    }, lay, 'retryBtn')

    // drawing in-out put port stuff
    if (input.length > 0) {
        const len = input.length
        const ypos = y - h / 2
        for (let i = 0; i < len; i++) {
            const xpos = len === 1 ? x : x + w / 2 * (i / (len - 1) - 0.5)
            const point = { x: xpos, y: ypos }
            const portHeight = lay.portHeight
            if (!input[i].mult) {
                Layer.component.rect.drawRect({
                    props: {
                        point,
                        width: lay.portWidth,
                        height: portHeight,
                        radius: lay.portRadius,
                        style: {
                            fillStyle: lay.disable
                                ? (type !== progressComp.type.UNRUN
                                    ? DISABLE_INIT_COLOR
                                    : DISABLE_NO_INIT_COLOR)
                                : (input[i].type === 'data' ? DATA_PORT_COLOR : MODEL_PORT_COLOR)
                        },
                        fill: true
                    }
                }, lay, input[i].name)
            } else {
                Layer.component.icon.drawIcon({
                    props: {
                        point,
                        width: lay.portWidth * 7 / 6,
                        height: portHeight * 3 / 2,
                        img: input[i].mult
                    }
                }, lay, input[i].name)
            }
            inoutPutmap.set(input[i].name, point)
            Layer.component.tooltip.drawTooltip({
                props: {
                    point: { x: point.x, y: point.y - lay.portHeight / 2 - lay.betweenPortWidthTooltip },
                    position: Layer.component.tooltip.BOTTOM,
                    text: input[i].tooltip || input[i].name,
                    trangleSize: lay.tipTrangleSize,
                    radius: lay.tooltipRadius,
                    padding: lay.tooltipPadding,
                    containerStyle: {
                        fillStyle: TOOLTIP_BACKGROUND
                    },
                    textStyle: {
                        font: lay.tooltipFont + 'px ' + FONT_FAMILY,
                        fillStyle: TOOLTIP_FONT_STYLE
                    }
                },
                zindex: 1,
                visiable: false
            }, lay, input[i].name + 'Tooltip')
        }
    }

    if (output.length > 0) {
        const len = output.length
        const ypos = y + h / 2
        for (let i = 0; i < len; i++) {
            const xpos = len === 1 ? x : x + w / 2 * (i / (len - 1) - 0.5)
            const point = { x: xpos, y: ypos }
            const portHeight = lay.portHeight
            if (!output[i].mult) {
                Layer.component.rect.drawRect({
                    props: {
                        point,
                        width: lay.portWidth,
                        height: portHeight,
                        radius: lay.portRadius,
                        style: {
                            fillStyle: lay.disable
                                ? (type !== progressComp.UNRUN
                                    ? DISABLE_INIT_COLOR
                                    : DISABLE_NO_INIT_COLOR)
                                : (output[i].type === 'data' ? DATA_PORT_COLOR : MODEL_PORT_COLOR)
                        },
                        fill: true
                    }
                }, lay, output[i].name)
            } else {
                Layer.component.icon.drawIcon({
                    props: {
                        point,
                        width: lay.portWidth * 7 / 6,
                        height: portHeight * 3 / 2,
                        img: output[i].mult
                    }
                }, lay, output[i].name)
            }
            inoutPutmap.set(output[i].name, point)
            Layer.component.tooltip.drawTooltip({
                props: {
                    point: { x: point.x, y: point.y + lay.portHeight / 2 + lay.betweenPortWidthTooltip },
                    position: Layer.component.tooltip.UP,
                    text: output[i].tooltip || output[i].name,
                    trangleSize: lay.tipTrangleSize,
                    radius: lay.tooltipRadius,
                    padding: lay.tooltipPadding,
                    containerStyle: {
                        fillStyle: TOOLTIP_BACKGROUND
                    },
                    textStyle: {
                        font: lay.tooltipFont + 'px ' + FONT_FAMILY,
                        fillStyle: TOOLTIP_FONT_STYLE
                    }
                },
                zindex: 1,
                visiable: false
            }, lay, output[i].name + 'Tooltip')
        }
    }
    lay.$meta.set('port', inoutPutmap)
}

function clear() {
    const lay = this
    lay.$ctx.clearRect(0, 0, lay.$canvas.width, lay.$canvas.height)
}

export default progressComp
