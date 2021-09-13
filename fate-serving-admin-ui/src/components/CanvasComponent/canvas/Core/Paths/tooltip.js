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
 *  point: {x, y},
 *  position: enum,
 *  text: [String],
 *  textPosition: enum
 *  lineBetween: Number
 *  padding: Number,
 *
 *  trangleSize: Number,
 *  radius: Number,
 *
 *  containerStyle: {},
 *  textStyle: {}
 * }
 */

import COMMON from './common'
import Layer from '../Basic'
import Trangle from './trangle'
import Rect from './rect'
import Text, { measureText } from './text'

const LINE_BETWEEN = 5

const tooltipComp = {
  drawTooltip(obj, parent, name) {
    obj.canvas = parent ? parent.$canvas : obj.canvas
    obj.struct = struct
    obj.here = here
    if (parent) {
      if (!name) {
        name = Layer.getUUID('tooltip')
      }
      parent.drawLayer(name, obj)
      return name
    } else {
      return new Layer(obj)
    }
  },
  here,
  LEFT: COMMON.LEFT,
  RIGHT: COMMON.RIGHT,
  UP: COMMON.CENTER,
  BOTTOM: COMMON.BOTTOM,
  TEXT: {
    LEFT: Text.LEFT,
    RIGHT: Text.RIGHT,
    CENTER: Text.CENTER
  },
  events: {
    scale: function(time, point = { x: 0, y: 0 }) {
      const lay = this
      lay.setCus('scale', () => {
        lay.point = Layer.scaleDistanceForPoint(lay.point, point, time)
        if (lay.width) lay.width = Layer.toFixed(lay.width * time)
        if (lay.height) lay.height = Layer.toFixed(lay.height * time)
        if (lay.padding) lay.padding = Layer.toFixed(lay.padding * time)
        if (lay.trangleSize) lay.trangleSize = Layer.toFixed(lay.trangleSize * time)
        if (lay.textStyle.font) {
          const n = parseFloat(lay.textStyle.font)
          lay.textStyle.font = lay.textStyle.font.replace(
            n, Layer.toFixed(n * time))
        }
        lay.linebetween = Layer.toFixed(lay.linebetween * time)
        lay.radius = Layer.toFixed((lay.radius || 4) * time)
      })
    }
  }
}

function here(point) {
  return false
}

function struct() {
  const lay = this
  lay.linebetween = lay.linebetween || LINE_BETWEEN
  const x = lay.point.x || lay.point[0] || 0
  const y = lay.point.y || lay.point[1] || 0
  const tranglePoint = { x, y }
  const contentPoint = {}
  let widthest = 0
  lay.text = Array.isArray(lay.text) ? lay.text : [lay.text]
  for (const item of lay.text) {
    const info = measureText(lay.$ctx, item, lay.textStyle)
    if (Math.ceil(info.width) > widthest) {
      widthest = Math.ceil(info.width)
    }
  }
  const contentHeight = parseInt(lay.textStyle.font) * lay.text.length + lay.linebetween * (lay.text.length - 1) + lay.padding * 1.5
  const contentWidth = widthest + lay.padding * 2
  if (lay.position === tooltipComp.RIGHT) {
    contentPoint.x = x - lay.trangleSize - contentWidth / 2
    contentPoint.y = y
  } else if (lay.position === tooltipComp.UP) {
    contentPoint.x = x
    contentPoint.y = y + lay.trangleSize + contentHeight / 2
  } else if (lay.position === tooltipComp.BOTTOM) {
    contentPoint.x = x
    contentPoint.y = y - lay.trangleSize - contentHeight / 2
  } else {
    contentPoint.x = x + lay.trangleSize + contentWidth / 2
    contentPoint.y = y
  }
  Trangle.drawTrangle({
    props: {
      point: tranglePoint,
      height: lay.trangleSize,
      style: lay.containerStyle,
      position: lay.position || COMMON.LEFT,
      fill: true
    }
  }, this, 'trangle')
  Rect.drawRect({
    props: {
      point: contentPoint,
      radius: lay.radius || 4,
      width: contentWidth,
      height: contentHeight,
      position: Rect.CENTER,
      style: lay.containerStyle,
      fill: true
    }
  }, this, 'rect')
  for (let i = 0; i < lay.text.length; i++) {
    let x = contentPoint.x
    let y = contentPoint.y
    if (lay.textPosition === tooltipComp.TEXT.LEFT) {
      x = x - widthest / 2
    } else if (lay.textPosition === tooltipComp.TEXT.RIGHT) {
      x = x + widthest / 2
    }
    y = y - (((lay.text.length - 1) / 2 - i) * (parseInt(lay.textStyle.font) + lay.linebetween))
    Text.drawText({
      props: {
        text: lay.text[i],
        point: { x, y },
        position: lay.textPosition || tooltipComp.TEXT.CENTER,
        style: lay.textStyle
      }
    }, this, 'text' + i)
  }
  lay.$meta.set('clear', { width: contentWidth, height: contentWidth + lay.trangleSize })
}

export default tooltipComp
