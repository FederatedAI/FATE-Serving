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
 *  height: Number,
 *  img: Object,
 * }
 */
import Layer from '../Basic'

const iconComp = {
  drawIcon(obj, parent, name) {
    obj.canvas = parent ? parent.$canvas : obj.canvas
    obj.path = path
    obj.here = here
    // obj.__inCanvas__ = inCanvas
    if (parent) {
      if (!name) {
        name = Layer.getUUID('icon')
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
//   const x = lay.point.x || lay.point[0] || 0
//   const y = lay.point.y || lay.point[1] || 0
//   const w = lay.width
//   const h = lay.height || w
//   if ((x + w <= cp.x) && (y + h <= cp.y)) {
//     return false
//   }
//   if ((x - w) >= (cp.x + width) && (y - h) >= (cp.y + height)) {
//     return false
//   }
//   return true
// }

function here(point) {
  const lay = this
  const x = lay.point.x || lay.point[0] || 0
  const y = lay.point.y || lay.point[1] || 0
  const w = lay.width
  const h = lay.height || w
  const trans = lay.$meta.get('$translate') || { x: 0, y: 0 }
  const px = (point.x || point[0]) - trans.x
  const py = (point.y || point[1]) - trans.y
  if (px < x + w / 2 && px > x - w / 2) {
    if (py < y + h / 2 && py > y - h / 2) {
      return true
    }
  }
  return false
}

function path() {
  image(this)
}

export function image(lay) {
  const ctx = lay.$ctx
  ctx.beginPath()
  const x = lay.point.x || lay.point[0] || 0
  const y = lay.point.y || lay.point[1] || 0
  const w = lay.width
  const h = lay.height || w
  ctx.drawImage(lay.img, x - w / 2, y - h / 2, w, h)
  ctx.closePath()
}

export default iconComp

