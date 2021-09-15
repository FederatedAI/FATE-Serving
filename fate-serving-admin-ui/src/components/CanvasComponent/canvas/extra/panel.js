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

import Layer from '../Core'

const CON_TO_DEGREE = 10
const DEGREE_DISTANCE = 5
const DEGREE_TO_NUM = 10
const NUM_TO_BOTTOM = 25
const LINE_TO_BOTTOM = 10
const LINE_STYLE = '#e8e8ef'
const FAMILY_FONT = 'Arial'

const panelComp = {
  drawPanel(obj, parent, name) {
    obj.canvas = parent ? parent.$canvas : obj.canvas
    obj.path = path
    if (parent) {
      if (!name) {
        name = Layer.getUUID('trangle')
      }
      parent.drawLayer(name, obj)
      return name
    } else {
      return new Layer(obj)
    }
  },
  events: {
    progress(progress) {
      const lay = this
      lay.registerChainTranslate('progress', false, lay.settingNum(lay.progress, progress, (to) => { lay.progress = to }, 200))
    },
    setTime(time) {
      const lay = this
      lay.setCus('setTime', () => {
        lay.time = _exchangeTime(time)
      })
    }
  }
}

export default panelComp

function _exchangeTime(ms) {
  if (ms.toString().match(':')) {
    return ms
  }
  if (!ms) {
    return '00:00:00'
  }
  const t = Math.round(ms / 1000)
  let s = t % 60
  const tm = (t - s) / 60
  let m = tm % 60
  let h = (tm - m) / 60
  if (s < 10) s = '0' + s
  if (m < 10) m = '0' + m
  if (h < 10) h = '0' + h
  return '' + h + ':' + m + ':' + s
}

function createGradient(ctx, sn, en, colors) {
  const gradient = ctx.createLinearGradient(sn.x, sn.y, en.x, en.y)
  for (const val of colors) {
    gradient.addColorStop(val.pos, val.color)
  }
  return gradient
}

function drawContainer(ctx, start, radius, width, gradient = true) {
  if (gradient) {
    ctx.strokeStyle = createGradient(ctx, { x: start.x, y: start.y }, { x: start.x + width, y: start.y }, [{ pos: 0, color: 'rgba(73, 78, 206, 0.6)' }, { pos: 1, color: '#24b68b' }])
    ctx.lineWidth = 10
    ctx.lineCap = 'butt'
  } else {
    ctx.lineWidth = 2
    ctx.lineCap = 'butt'
    ctx.strokeStyle = LINE_STYLE
  }
  const s = Math.sin(Math.PI / 4)
  const c = Math.cos(Math.PI / 4)
  const x = start.x + s * radius
  const y = start.y + c * radius
  ctx.beginPath()
  ctx.moveTo(start.x, start.y)
  ctx.arc(x, y, radius, 1.25 * Math.PI, 1.75 * Math.PI)
  ctx.stroke()
  return radius
}

function getPosition(sx, sy, degree, distance) {
  return {
    x: sx + distance * Math.sin(2 * Math.PI * (degree) / 360),
    y: sy + distance * Math.cos(2 * Math.PI * (degree) / 360)
  }
}

function drawDegree(ctx, bet = CON_TO_DEGREE, long = DEGREE_DISTANCE, toWord = DEGREE_TO_NUM, r, start, font) {
  const deg = 45
  let checkX = Math.sin(45 * 2 * Math.PI / 360) * r
  let checkY = Math.cos(45 * 2 * Math.PI / 360) * r
  let lx = start.x
  let ly = start.y
  for (let ind = 0; ind < 11; ind++) {
    (function() {
      const i = ind
      if (i !== 0) {
        const newCheckX = Math.sin((45 - i * 9) * 2 * Math.PI / 360) * r
        const newCheckY = Math.cos((45 - i * 9) * 2 * Math.PI / 360) * r
        lx += (checkX - newCheckX)
        ly += (checkY - newCheckY)
        checkX = newCheckX
        checkY = newCheckY
      }
      const cs = getPosition(lx, ly, deg - (i * 9), bet)
      const es = getPosition(cs.x, cs.y, deg - (i * 9), long)
      const ts = getPosition(es.x, es.y, deg - (i * 9), toWord)
      ctx.beginPath()
      ctx.moveTo(cs.x, cs.y)
      ctx.lineTo(es.x, es.y)
      ctx.strokeStyle = LINE_STYLE
      ctx.lineWidth = 1
      ctx.stroke()

      ctx.beginPath()
      ctx.textAlign = 'center'
      ctx.textBaseLine = 'middle'
      ctx.fillStyle = '#494ece'
      ctx.font = font + 'px ' + FAMILY_FONT
      ctx.fillText(i * 10, ts.x, ts.y)
    })()
  }
}

const path = function() {
  const lay = this
  const ctx = lay.$ctx
  lay.width = lay.width
  lay.progress = lay.progress || 0
  lay.content = lay.content || 'elapsad'
  lay.time = _exchangeTime(lay.time) || '00:00:00'
  lay.x = lay.x || 0
  lay.r = (lay.width - lay.x * 2) / 2 / Math.sin(Math.PI / 4)
  lay.y = lay.y || lay.r - (lay.width - lay.x * 2) / 2 + 6
  drawContainer(ctx, { x: lay.x, y: lay.y }, lay.r, lay.width)
  drawDegree(ctx, CON_TO_DEGREE, DEGREE_DISTANCE, DEGREE_TO_NUM, lay.r, { x: lay.x, y: lay.y }, 10)
  const distance = CON_TO_DEGREE + DEGREE_DISTANCE + DEGREE_TO_NUM + NUM_TO_BOTTOM
  drawContainer(ctx, { x: lay.x + Math.sin(Math.PI / 4) * distance, y: lay.y + Math.cos(Math.PI / 4) * distance }, lay.r - distance, lay.width - Math.sin(Math.PI / 4) * distance * 2, false)
  const degree = 90 * lay.progress / 100
  const checkX = Math.sin(45 * 2 * Math.PI / 360) * lay.r
  const checkY = Math.cos(45 * 2 * Math.PI / 360) * lay.r
  let fx = lay.x
  let fy = lay.y
  const newCheckX = Math.sin((45 - degree) * 2 * Math.PI / 360) * lay.r
  const newCheckY = Math.cos((45 - degree) * 2 * Math.PI / 360) * lay.r
  fx += checkX - newCheckX
  fy += checkY - newCheckY
  const slp = getPosition(fx, fy, (45 - degree), distance - LINE_TO_BOTTOM)
  const elp = getPosition(slp.x, slp.y, (45 - degree), 30)
  ctx.beginPath()
  ctx.moveTo(slp.x, slp.y)
  ctx.lineTo(elp.x, elp.y)
  ctx.strokeStyle = '#494ece'
  ctx.lineWidth = 1
  ctx.stroke()

  const contentX = lay.x + Math.sin(Math.PI / 4) * lay.r
  const contentY = lay.y + Math.cos(Math.PI / 4) * distance - 14
  ctx.fillStyle = '#bbbbc8'
  ctx.font = '13px ' + FAMILY_FONT
  ctx.textAlign = 'center'
  ctx.textBaseLine = 'middle'
  ctx.fillText(lay.content, contentX, contentY)

  const timeX = contentX
  const timeY = contentY + 14 + 4
  ctx.fillStyle = '#494ece'
  ctx.font = '16px ' + FAMILY_FONT
  ctx.fillText(lay.time, timeX, timeY)

  const progressX = contentX
  const progressY = timeY + 16 + 16 + 18
  const progresses = Math.round(lay.progress) + '%'
  ctx.fillStyle = '#494ece'
  ctx.font = 'bold 36px ' + FAMILY_FONT
  ctx.fillText(progresses, progressX, progressY)
}

