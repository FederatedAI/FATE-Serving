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
 * interactive: {
 *  event: {operation, props}
 * }
 */

const DEFAULT = {
    click: true,
    mouseup: true,
    mousemove: true,
    mousedown: true,
    mousewheel: true
}
const CANVAS_PADDING = 20

class canvasUtil {
    constructor(can, interactive, afterInit) {
        this.canvasDom = can
        this.interactive = (() => {
            const def = JSON.parse(JSON.stringify(DEFAULT))
            for (const key in interactive) {
                def[key] = interactive[key]
            }
            return def
        })()
        this.inited = false
        this.afterInit = afterInit
        this.width = 0
        this.height = 0

        this._inited()
    }
    _inited() {
        const that = this
        if (this.canvasDom) {
            this._suitable()
            let added = false
            if (this.lay) {
                added = true
                this.lay.deleteAllAboutChain()
            }
            this.lay = (function() {
                if (that.canvasDom) {
                    return that.afterInit(that.canvasDom)
                }
            })()
            this.scaleTime = 1
            this.translateX = 0
            this.translateY = 0
            this.holding = false
            this.moved = false
            this.originPos = null
            this.lay.emit('$translate', this.translateX, this.translateY)
            if (!added && this.lay) {
                this._addEvents()
            }
        }
    }
    _suitable() {
        if (this.canvasDom && this.canvasDom.parentElement) {
            const parent = this.canvasDom.parentElement
            const style = getComputedStyle(parent)
            const originWidth = parseInt(style.width)
            const originHeight = parseInt(style.height)
            this.canvasDom.setAttribute('style', 'width:' + originWidth + 'px;height:' + originHeight + 'px;overflow:hidden;')
            this.canvasDom.setAttribute('width', originWidth)
            this.canvasDom.setAttribute('height', originHeight)
            this.width = originWidth
            this.height = originHeight
            this.inited = true
        }
    }
    _addEvents() {
        if (this.inited) {
            const that = this
            that.canvasDom.addEventListener('mousedown', function(ev) {
                ev.stopPropagation()
                ev.preventDefault()
                that.holding = true
                that.moved = false
                if (that.interactive['mousedown'].operation) {
                    that.interactive['mousedown'].operation(that.lay)
                }
            })
            that.canvasDom.addEventListener('mousemove', function(ev) {
                ev.stopPropagation()
                ev.preventDefault()
                if (that.holding) {
                    if (that.originPos === null) {
                        that.originPos = that._getPos(ev)
                    } else {
                        const pos = that._getPos(ev)
                        if (pos.x !== that.originPos.x || pos.y !== that.originPos.y) {
                            that.moved = true
                            const dx = pos.x - that.originPos.x
                            const dy = pos.y - that.originPos.y
                            that.translateX += dx
                            that.translateY += dy
                            that.originPos = pos
                            if (that.interactive['mousemove'] === true) {
                                that.lay.$ctx.translate(dx, dy)
                                that.lay.emit('$translate', that.translateX, that.translateY)
                                that.lay.drawing()
                            } else if (that.interactive['mousemove']) {
                                that.interactive['mousemove'].operation(that.lay, that.holding, dx, dy)
                            }
                        }
                    }
                } else {
                    const pos = that._getPos(ev)
                    if (that.interactive['mousemove'] === true) {
                        that.lay.emit('showTips', pos)
                    } else if (that.interactive['mousemove']) {
                        that.interactive['mousemove'].operation(that.lay, that.holding, pos)
                    }
                }
            })
            that.canvasDom.addEventListener('mouseup', function(ev) {
                ev.stopPropagation()
                ev.preventDefault()
                that.holding = false
                that.originPos = null
                if (that.interactive['mouseup'].operation) {
                    that.interactive['mouseup'].operation(that.lay)
                }
            })
            that.canvasDom.addEventListener('click', function(ev) {
                ev.preventDefault()
                if (that.moved) {
                    that.moved = false
                } else {
                    const point = that._getPos(ev)
                    if (that.interactive['click'].beforeClick) {
                        that.interactive['click'].beforeClick()
                    }
                    if (that.interactive['click'].operation) {
                        that.interactive['click'].operation(that.lay, point)
                    } else if (that.interactive['click'] === true) {
                        that.lay.emit('choose', point)
                    } else if (that.interactive['click'].props) {
                        that.lay.emit('choose', point, ...that.interactive['click'].props)
                    }
                }
            })
            that.canvasDom.addEventListener('mouseout', function(ev) {
                ev.stopPropagation()
                ev.preventDefault()
                that.originPos = null
                that.holding = false
                if (that.interactive['mouseout']) {
                    that.interactive['mouseout'].operation(that.lay)
                }
            })
            const mouseWheel = function(ev) {
                ev.stopPropagation()
                ev.preventDefault()
                const point = that._getPos(ev)
                const wheelDelta = ev.wheelDelta || (-ev.detail * 24)
                let nowTimes = that.scaleTime + (wheelDelta / 1500)
                nowTimes = nowTimes <= 0.1 ? 0.1 : nowTimes
                const scale = nowTimes / that.scaleTime
                that.scaleTime = nowTimes
                if (that.interactive['mousewheel'] === true) {
                    that.lay.emit('scale', scale, point)
                } else if (that.interactive['mousewheel']) {
                    that.interactive['mousewheel'].operation(that.lay, scale, point)
                }
            }
            if (!/Firefox/i.test(window.navigator.userAgent)) {
                that.canvasDom.addEventListener('mousewheel', mouseWheel)
            } else {
                that.canvasDom.addEventListener('DOMMouseScroll', mouseWheel)
            }
            window.addEventListener('resize', function() {
                setTimeout(() => {
                    if (that.interactive['resize']) {
                        that._suitable()
                        that.interactive['resize']()
                    } else {
                        that._inited()
                    }
                }, 10)
            })
        }
    }
    _getPos(ev) {
        let x, y
        if (ev.layerX || ev.layerX === 0) {
            x = ev.layerX
            y = ev.layerY
        } else if (ev.offsetX || ev.offsetY === 0) {
            x = ev.offsetX
            y = ev.offsetY
        }
        return { x, y }
    }
    suitableForWhole(getWidth, checkPos, canvasPadding, limit = true) {
        if (!this.inited) {
            return false
        }
        canvasPadding = canvasPadding || CANVAS_PADDING
        const that = this
        const width = this.canvasDom.width - canvasPadding * 2
        const height = this.canvasDom.height - canvasPadding * 2
        const diagramStyle = getWidth.call(this.lay)
        const heightScale = height / diagramStyle.height
        const widthScale = width / diagramStyle.width
        let finalScale = 0
        if (heightScale < widthScale) {
            finalScale = heightScale
        } else {
            finalScale = widthScale
        }
        let nowScale = finalScale
        if (limit) {
            if (this.scaleTime * nowScale > 1) {
                nowScale = 1 / this.scaleTime
                this.scaleTime = 1
            }
        } else {
            this.scaleTime *= nowScale
        }
        if (that.interactive['mousewheel'] === true) {
            this.lay.emit('scale', nowScale, diagramStyle.point, () => {
                if (checkPos) checkPos()
                that.lay.$ctx.translate(-that.translateX, -that.translateY)
                that.translateX = 0
                that.translateY = 0
                that.lay.emit('$translate', that.translateX, that.translateY)
            })
        } else if (that.interactive['mousewheel']) {
            that.interactive['mousewheel'].operation(this.lay, nowScale, diagramStyle.point)
        }
        return true
    }
    scaleBigger() {
        const x = this.canvasDom.width / 2 - this.translateX
        const y = this.canvasDom.height / 2 - this.translateY
        const scale = (this.scaleTime + 0.1) / this.scaleTime
        this.scaleTime += 0.1
        this.lay.emit('scale', scale, { x, y })
    }
    scaleSmaller() {
        const x = this.canvasDom.width / 2 - this.translateX
        const y = this.canvasDom.height / 2 - this.translateY
        const scale = (this.scaleTime - 0.1) / this.scaleTime
        this.scaleTime -= 0.1
        this.lay.emit('scale', scale, { x, y })
    }
}

export default canvasUtil
