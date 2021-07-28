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
 *  point: [{x, y}]
 *  style: {},
 *  curve: boolean,
 *  stroke: boolean
 * }
 */
import Layer from '../Basic'

const lineComp = {
    drawLine(obj, parent, name) {
        obj.canvas = parent ? parent.$canvas : obj.canvas
        obj.path = path
        // obj.__inCanvas__ = inCanvas
        if (parent) {
            if (!name) {
                name = Layer.getUUID('line')
            }
            parent.drawLayer(name, obj)
            return name
        } else {
            return new Layer(obj)
        }
    }
}

// function inCanvas() {
//   const lay = this
//   const canvas = lay.$canvas
//   const width = canvas.width
//   const height = canvas.height
//   const cp = lay.$meta.get('$translate') || { x: 0, y: 0 }
//   const points = lay.point
//   for (const val of points) {
//     const x = val.x || val[0] || 0
//     const y = val.y || val[1] || 0
//     if ((x > cp.x && x < cp.x + width) && (y > cp.y && y < cp.y + height)) {
//       return true
//     }
//   }
//   return false
// }

// path for line-drawing component
function path() {
    const lay = this
    if (!lay.curve || lay.point.length <= 2) {
        brokeLine.call(this)
    } else {
        curve.call(this)
    }
}

export function brokeLine() {
    const lay = this
    const ctx = lay.$ctx
    Layer.stroke(ctx, lay.style, (ctx) => {
        for (let i = 0; i < lay.point.length; i++) {
            const x = lay.point[i].x || lay.point[i][0] || 0
            const y = lay.point[i].y || lay.point[i][1] || 0
            if (i === 0) {
                ctx.moveTo(x, y)
            } else {
                ctx.lineTo(x, y)
            }
        }
    })
}

export function curve() {
    const lay = this
    const ctx = lay.$ctx
    Layer.stroke(ctx, lay.style, (ctx) => {
        for (let i = 0; i < lay.point.length; i++) {
            const x = lay.point[i].x || lay.point[i][0] || 0
            const y = lay.point[i].y || lay.point[i][1] || 0
            if (i === 0) {
                ctx.moveTo(x, y)
            } else {
                if (lay.point[i + 1]) {
                    let endX = 0
                    let endY = 0
                    if (lay.point[i + 2]) {
                        endX = (x + (lay.point[i + 1].x || lay.point[i + 1][0])) / 2
                        endY = (y + (lay.point[i + 1].y || lay.point[i + 1][1])) / 2
                    } else {
                        endX = lay.point[i + 1].x || lay.point[i + 1][0]
                        endY = lay.point[i + 1].y || lay.point[i + 1][1]
                    }
                    ctx.quadraticCurveTo(x, y, endX, endY)
                } else {
                    ctx.lineTo(x, y)
                }
            }
        }
    })
}

export function calculation(startP, endP, horizen, levelBetween, max, min) {
    console.log(arguments, 'arg-calculation')
    const finalPoints = []
    let deviate = max || levelBetween || 0
    if (!levelBetween || (max && min)) {
        max = max || 50
        min = min || 20
        if (max <= min) {
            const middle = max
            max = min
            min = middle
        }
        deviate = Math.random() * (max - min) + min
    }
    const distanceRate = 0.4
    const betweenPoints = endP.y - startP.y
    levelBetween = levelBetween || betweenPoints / 2
    if ((betweenPoints / 2) < levelBetween) {
        levelBetween = betweenPoints / 2
    }
    finalPoints.push(startP)
    if (horizen || (startP.x !== endP.x && startP.y !== endP.y)) {
        const sx = startP.x || startP[0] || 0
        const sy = startP.y || startP[1] || 0
        const ex = endP.x || endP[0] || 0
        const ey = endP.y || endP[1] || 0
        const distance = levelBetween
        const ps = { x: sx, y: ey > sy ? sy + distanceRate * distance : sy - distanceRate * distance }
        const pe = { x: ex, y: ey > sy ? ey - distanceRate * distance : ey + distanceRate * distance }
        finalPoints.push(ps)
        if (horizen && sx === ex) {
            if (horizen === 'left') {
                horizen = -deviate
            } else if (horizen === 'right') {
                horizen = deviate
            }
            const pm1 = { x: sx + horizen, y: ey > sy ? sy + distanceRate * distance * 2 : sy - distanceRate * distance * 2 }
            finalPoints.push(pm1)
            const pm2 = { x: sx + horizen, y: ey > sy ? ey - distanceRate * distance * 2 : ey + distanceRate * distance * 2 }
            finalPoints.push(pm2)
        }
        finalPoints.push(pe)
    }
    finalPoints.push(endP)
    return finalPoints
}

export default lineComp
