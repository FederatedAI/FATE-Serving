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
 *  radius: Number,
 *  position: enum [LEFT_UP, LEFT_DOWN, RIGHT_UP, RIGHT_DOWN],
 *  sAngle: Number,
 *  eAngle: Number,
 *  wise: Boolean,
 *  style: Object,
 *  stroke: Boolean,
 *  fill: Boolean,
 *  justPath: Boolean
 * }
 */
import COMMON from './common'
import Layer from '../Basic'

const arcComp = {
  drawArc(obj, parent, name) {
    obj.canvas = parent ? parent.$canvas : obj.canvas
    obj.path = path
    // obj.__inCanvas__ = inCanvas
    if (parent) {
      if (!name) {
        name = Layer.getUUID('circle')
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
  RIGHT_DOWM: COMMON.RIGHT_DOWM
}

// function inCanvas() {
//   const lay = this
//   const canvas = lay.$canvas
//   const width = canvas.width
//   const height = canvas.height
//   const cp = lay.$meta.get('$translate') || { x: 0, y: 0 }
//   const x = lay.point.x || lay.point[0] || 0
//   const y = lay.point.y || lay.point[1] || 0
//   const r = lay.radius
//   if ((x + r <= cp.x) || (y + r <= cp.y)) {
//     return false
//   }
//   if ((x - r) >= (cp.x + width) || (y - r) >= (cp.y + height)) {
//     return false
//   }
//   return true
// }

function path() {
  const lay = this
  const x = lay.point.x || lay.point[0] || 0
  const y = lay.point.y || lay.point[1] || 0
  const r = lay.radius
  if (lay.position === arcComp.LEFT_UP) {
    lay.point = { x: x + r, y: y + r }
  } else if (lay.position === arcComp.RIGHT_UP) {
    lay.point = { x, y: y + r }
  } else if (lay.position === arcComp.LEFT_DOWN) {
    lay.point = { x: x + r, y }
  }
  arc.call(lay)
}

export function arc(lay) {
  const basicPath = (ctx) => {
    const x = lay.point.x || lay.point[0] || 0
    const y = lay.point.y || lay.point[1] || 0
    const r = lay.radius
    const sAngle = lay.sAngle || COMMON._SANGLE
    const eAngle = lay.eAngle || COMMON._EANGLE
    const wise = !!lay.wise
    ctx.arc(x, y, r, sAngle, eAngle, wise)
  }
  Layer.commonDrawing(lay, basicPath)
}

export default arcComp

