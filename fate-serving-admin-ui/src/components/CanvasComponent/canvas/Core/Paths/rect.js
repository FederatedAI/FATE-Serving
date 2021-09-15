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
 *  point:{x, y},
 *  radius: Number,
 *  width: Number,
 *  height: Number,
 *  progress: Number (0 - 1)
 *  position: enum:[LEFT_UP, LEFT_DOWN, RIGHT_UP, RIGHT_DOWN, CENTER]
 *  style: {}
 *  stroke: boolaan
 *  fill: boolean
 *  justPath: boolean
 * }
 */

import COMMON from './common'
import Layer from '../Basic'

const rectComp = {
  drawRect(obj, parent, name) {
    obj.canvas = parent ? parent.$canvas : obj.canvas
    obj.path = path
    obj.here = here
    // obj.__inCanvas__ = inCanvas
    if (parent) {
      if (!name) {
        name = Layer.getUUID('rect')
      }
      parent.drawLayer(name, obj)
      return name
    } else {
      return new Layer(obj)
    }
  },
  LEFT_UP: COMMON.LEFT_UP,
  RIGHT_UP: COMMON.RIGHT_UP,
  LEFT_DOWN: COMMON.LEFT_DOWN,
  RIGHT_DOWN: COMMON.RIGHT_DOWN,
  CENTER: COMMON.CENTER
}

function here(point) {
  const lay = this
  // const translate = lay.$meta.get('$tranlsate') || { x: 0, y: 0 }
  const x = lay.point.x || lay.point[0] || 0
  const y = lay.point.y || lay.point[1] || 0
  const w = lay.width
  const h = lay.height
  let p = {}
  if (lay.position === rectComp.LEFT_UP) {
    p = { x: x + w / 2, y: y + h / 2 }
  } else if (lay.position === rectComp.RIGHT_UP) {
    p = { x: x - w / 2, y: y + h / 2 }
  } else if (lay.position === rectComp.LEFT_DOWN) {
    p = { x: x + w / 2, y: y - h / 2 }
  } else if (lay.position === rectComp.RIGHT_DOWN) {
    p = { x: x - w / 2, y: y - h / 2 }
  } else {
    p = { x, y }
  }
  const trans = lay.$meta.get('$translate') || { x: 0, y: 0 }
  const operax = (point.x || point[0] || 0) - trans.x
  const operay = (point.y || point[1] || 0) - trans.y
  if (operax >= p.x - w / 2 &&
    operax <= p.x + w / 2 &&
    operay >= p.y - h / 2 &&
    operay <= p.y + h / 2) {
    return true
  }
  return false
}

// function inCanvas() {
//   const lay = this
//   const canvas = lay.$canvas
//   const width = canvas.width
//   const height = canvas.height
//   const cp = lay.$meta.get('$translate') || { x: 0, y: 0 }
//   const x = lay.point.x || lay.point[0] || 0
//   const y = lay.point.y || lay.point[1] || 0
//   const w = lay.width
//   const h = lay.height
//   let p = {}
//   if (lay.position === rectComp.LEFT_UP) {
//     p = { x: x + w / 2, y: y + h / 2 }
//   } else if (lay.position === rectComp.RIGHT_UP) {
//     p = { x: x - w / 2, y: y + h / 2 }
//   } else if (lay.position === rectComp.LEFT_DOWN) {
//     p = { x: x + w / 2, y: y - h / 2 }
//   } else if (lay.position === rectComp.RIGHT_DOWN) {
//     p = { x: x - w / 2, y: y - h / 2 }
//   } else {
//     p = { x, y }
//   }
//   if ((p.x + w <= cp.x) && (p.y + h <= cp.y)) {
//     return false
//   }
//   if ((p.x - w) >= (cp.x + width) && (p.y - h) >= (cp.y + height)) {
//     return false
//   }
//   return true
// }

function path() {
  const lay = this
  const x = lay.point.x || lay.point[0] || 0
  const y = lay.point.y || lay.point[1] || 0
  const w = lay.width
  const h = lay.height
  if (lay.position === rectComp.LEFT_UP) {
    lay.point = { x: x + w / 2, y: y + h / 2 }
  } else if (lay.position === rectComp.RIGHT_UP) {
    lay.point = { x: x - w / 2, y: y + h / 2 }
  } else if (lay.position === rectComp.LEFT_DOWN) {
    lay.point = { x: x + w / 2, y: y - h / 2 }
  } else if (lay.position === rectComp.RIGHT_DOWN) {
    lay.point = { x: x - w / 2, y: y - h / 2 }
  }
  rect.call(lay)
}

export function rect() {
  const lay = this
  const basicPath = (ctx) => {
    const x = lay.point.x || lay.point[0] || 0
    const y = lay.point.y || lay.point[1] || 0
    const r = lay.radius
    const w = lay.width
    const h = lay.height
    const p = lay.progress === 0 ? 0 : lay.progress ? lay.progress : 1

    if (p <= 0) {
      return
    }

    let nextX = x - w / 2
    let nextY = y - h / 2 + r
    ctx.moveTo(nextX, nextY)

    let arcX = nextX + r
    let arcY = nextY
    let sAng = 1 * Math.PI
    let eAng = (w * p < r) ? (sAng + Math.acos((r - w * p) / r)) : (1.5 * Math.PI)
    ctx.arc(arcX, arcY, r, sAng, eAng, false)

    if (w * p > r) {
      nextX = (w * p) > (w - r) ? nextX + w - 2 * r : nextX + w * p
      nextY = nextY - r
      ctx.lineTo(nextX, nextY)

      if (w * p > w - r) {
        arcX = nextX
        arcY = nextY + r
        sAng = 1.5 * Math.PI
        eAng = p < 1 ? sAng + Math.acos((r - (w - w * p)) / r) : 0
        ctx.arc(arcX, arcY, r, sAng, eAng, false)

        nextX = nextX + (r - (w - w * p))
        nextY = nextY + h - r - Math.cos(0.5 * Math.PI - Math.acos((r - (w - w * p)) / r)) * r
        ctx.lineTo(nextX, nextY)

        arcY = arcY + h - 2 * r
        sAng = 0
        eAng = p < 1 ? sAng + Math.acos((r - (w - w * p)) / r) : 0.5 * Math.PI
        ctx.arc(arcX, arcY, r, sAng, eAng, false)

        nextX = arcX - w + 3 * r
        nextY = arcY + r
        ctx.lineTo(nextX, nextY)
      } else {
        nextY = nextY + h
        ctx.lineTo(nextX, nextY)

        nextX = nextX - w * p + r
        ctx.lineTo(nextX, nextY)
      }
      arcX = nextX
      arcY = nextY - r
      ctx.arc(arcX, arcY, r, 0.5 * Math.PI, 1 * Math.PI, false)
    } else {
      nextX = nextX + w * p
      nextY = nextY + h - 2 * r + Math.sin(0.5 * Math.PI - Math.acos((r - w * p) / r)) * r
      ctx.lineTo(nextX, nextY)

      arcY = arcY + h - 2 * r
      sAng = 1 * Math.PI - Math.acos((r - w * p) / r)
      eAng = 1 * Math.PI
      ctx.arc(arcX, arcY, r, sAng, eAng, false)
    }

    nextX = x - w / 2
    nextY = y - h / 2 + r
    ctx.lineTo(nextX, nextY)
  }
  Layer.commonDrawing(lay, basicPath)
}

export default rectComp
