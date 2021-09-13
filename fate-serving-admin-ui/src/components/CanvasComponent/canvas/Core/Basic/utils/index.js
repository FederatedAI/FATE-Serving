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

import CanvasUtil from './canvasUtils'

export function mergeObj(...obj) {
  let final = {}
  if (obj.length === 1) {
    final = obj[0]
  } else {
    for (const val of obj) {
      for (const key in val) {
        final[key] = val[key]
      }
    }
  }
  return final
}

export function mergeObjTo(origin, ...obj) {
  const final = origin
  for (const val of obj) {
    for (const key in val) {
      final[key] = val[key]
    }
  }
  return final
}

export function getUUID(name, sub_now = 7, sub_ran = 3) {
  const now = new Date().getTime().toString().substr(-sub_now)
  let ran = Math.ceil(Math.random() * Math.pow(10, sub_ran)).toString()
  ran = ((sub_ran > ran.length) ? new Array(sub_ran - ran.length).fill(0).join('') : '') + ran
  return name + '_' + now + '_' + ran
}

export function toRGBA(color, opacity) {
  let sColor = color.toLowerCase()
  const reg = /^#([0-9a-fA-f]{3}|[0-9a-fA-f]{6})$/
  if (sColor && reg.test(sColor)) {
    if (sColor.length === 4) {
      let sColorNew = '#'
      for (let i = 1; i < 4; i += 1) {
        sColorNew += sColor.slice(i, i + 1).concat(sColor.slice(i, i + 1))
      }
      sColor = sColorNew
    }
    const sColorChange = []
    for (let i = 1; i < 7; i += 2) {
      sColorChange.push(parseInt('0x' + sColor.slice(i, i + 2)))
    }
    return 'rgba(' + sColorChange.join(',') + ',' + (opacity !== undefined ? opacity : 1) + ')'
  }
  return sColor
}

export function stroke(ctx, style, drawing, justPath = false) {
  (!justPath && ctx.beginPath())
  drawing(ctx)
  if (!justPath) {
    ctx.save()
    for (const key in style) {
      ctx[key] = style[key]
    }
    ctx.stroke()
    ctx.restore()
    ctx.closePath()
  }
}

export function fill(ctx, style, drawing, justPath = false) {
  (!justPath && ctx.beginPath())
  drawing(ctx)
  if (!justPath) {
    ctx.save()
    for (const key in style) {
      ctx[key] = style[key]
    }
    ctx.fill()
    ctx.restore()
    ctx.closePath()
  }
}

export function commonDrawing(lay, drawing) {
  const ctx = lay.$ctx
  if (lay.stroke) {
    stroke(ctx, lay.style, drawing)
  }
  if (lay.fill) {
    fill(ctx, lay.style, drawing)
  }
  if (lay.justPath) {
    drawing(ctx)
  }
}

export function rotate(ctx, center, angle) {
  const x = center.x || center[0]
  const y = center.y || center[1]
  ctx.translate(x, y)
  ctx.rotate(angle)
  ctx.translate(-x, -y)
}

function scaleDistanceForPoint(origin, accord, scaleTime) {
  const ox = origin.x || origin[0] || 0
  const oy = origin.y || origin[1] || 0
  const ax = accord.x || accord[0] || 0
  const ay = accord.y || accord[1] || 0
  const dx = toFixed((ox - ax) * scaleTime)
  const dy = toFixed((oy - ay) * scaleTime)
  return { x: ax + dx, y: ay + dy }
}

function toFixed(num, pos = 6) {
  return parseFloat(parseFloat(num).toFixed(pos))
}

export function exchangeTime(time) {
  if (!time) {
    return '00:00:00'
  }
  if (time.toString().match(':')) {
    return time
  }
  time = new Date().getTime() - time
  const t = Math.round(time / 1000)
  let s = t % 60
  const tm = (t - s) / 60
  let m = tm % 60
  let h = (tm - m) / 60
  s = s < 10 ? '0' + s : s
  m = m < 10 ? '0' + m : m
  h = h < 10 ? '0' + h : h
  return h + ':' + m + ':' + s
}

export function exportTool(Layer) {
  const toolList = { mergeObj, mergeObjTo, getUUID, toRGBA, stroke, fill, commonDrawing, rotate, scaleDistanceForPoint, toFixed, CanvasUtil, exchangeTime }
  for (const key in toolList) {
    Layer[key] = toolList[key]
  }
}
