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
 * features: [string]
 * correlations: [[string]]
 * width: Number,
 * point: {x: y},
 * max: Number,
 * min: Number,
 */

import Layer from '../Core'
import square from './square'
import { measureText } from '../Core/Paths/text'

const TEXT_ANGLE = -Math.PI / 4
const TEXT_FONT = 12
const TEXT_FAMILY = 'Lato'
const TEXT_COLOR = '#000'

const PRECOLORS = [
  '#3145A6',
  '#DEECFC',
  '#0EC7A5'
]

const START = 1
const END = -1
const BETWEEN = 0.25
const FEATURE_DISTANCE = 0.1

const CorrelationComp = {
  drawCorrelation(obj, parent, name) {
    obj.canvas = parent ? parent.$canvas : obj.canvas
    obj.struct = struct
    if (parent) {
      if (!name) {
        name = Layer.getUUID('text')
      }
      parent.drawLayer(name, obj)
      return name
    } else {
      return new Layer(obj)
    }
  },
  events: {
    newFeatures(features, showContent = false) {
      const lay = this
      lay.setCus('newFeatures', () => {
        lay.features = features
        if (showContent) {
          lay.setCus('showTextAfterFeaturesChange', () => {
            lay.emit('showContent', true)
          })
        }
      })
    },
    scale(time, point = { x: 0, y: 0 }, after) {
      const lay = this
      const trans = lay.$meta.get('$translate') || { x: 0, y: 0 }
      point = { x: point.x - trans.x, y: point.y - trans.y }
      lay.setCus('scale', () => {
        lay.point = Layer.scaleDistanceForPoint(lay.point, point, time)
        lay.width = Layer.toFixed(lay.width * time)
        lay.textFont = Layer.toFixed(lay.textFont * time)
        lay.eachWidth = Layer.toFixed(lay.eachWidth * time)
        lay.contentFont = Layer.toFixed(lay.contentFont * time)
        if (after) {
          after.call(lay)
        }
      })
    },
    filter(max, min, showText) {
      const lay = this
      lay.max = max || lay.max || 1
      lay.min = min || lay.min || -1
      for (const item of lay.$children) {
        if (!item[0].match(/_[xy]_/i)) {
          const num = parseFloat(item[1].content)
          item[1].emit('setDisable', (num > lay.max || num < lay.min), showText)
        }
      }
    }
  }
}

function struct() {
  const lay = this
  lay.textFont = lay.textFont || TEXT_FONT
  lay.featureDistance = lay.featureDistance || FEATURE_DISTANCE
  lay.max = lay.max || 1
  lay.min = lay.min || -1
  lay.drawInstance = lay.drawInstance || new Correlation(lay.features, lay.correlations, lay)
  lay.drawInstance.checkShowing(lay.features)
}

export default CorrelationComp

class Correlation {
  constructor(features, contents, lay) {
    this.features = features
    this.contents = contents
    this.newFeatures = features
    this.lay = lay
    this.betweenFont = 0.1
    this.squareList = new Map()
    this.getInstance()
  }
  getLongestFeatureStyle(textFontSize, longest) {
    const lay = this.lay
    if (!longest) {
      longest = ''
      for (const item of this.newFeatures) {
        if (item.length > longest.length) {
          longest = item
        }
      }
    }
    const stylus = measureText(this.lay.$ctx, longest, {
      font: textFontSize + 'px ' + TEXT_FAMILY
    }, TEXT_ANGLE)
    const eachWidth = (this.lay.width - stylus.xlength - stylus.xlength * lay.featureDistance - textFontSize) / this.newFeatures.length
    const fontSize = square.getSuitableFont(this.lay.$ctx, '-0.000000', eachWidth, TEXT_FAMILY)
    if (fontSize + this.betweenFont < textFontSize || fontSize - this.betweenFont > textFontSize) {
      const change = (fontSize - textFontSize) / 2
      const finalChange = Math.abs(change) < this.betweenFont ? (change < 0 ? -this.betweenFont : this.betweenFont) : change
      return this.getLongestFeatureStyle(textFontSize + finalChange, longest)
    } else {
      this.lay.eachWidth = eachWidth
      this.lay.distance = stylus
      this.lay.textFont = textFontSize
      this.lay.contentFont = fontSize
      return textFontSize
    }
  }
  getInstance() {
    const lay = this.lay
    const rever = [...this.features].reverse()
    for (let i = 0; i < rever.length; i++) {
      for (let j = 0; j < this.features.length; j++) {
        const num = parseFloat(Layer.toFixed(this.contents[i][j], 6))
        const sq = new SquareInfo({
          width: 0,
          content: num,
          x: this.features[j],
          y: rever[i],
          contentFont: lay.contentFont
        })
        this.squareList.set(this.features[j] + '_' + rever[i], sq)
      }
    }
  }
  checkpos() {
    const lay = this.lay
    const point = this.lay.point
    const distance = this.lay.distance
    const eachWidth = this.lay.eachWidth
    const x = point.x
    const y = point.y
    const textFont = lay.textFont
    const rever = [...this.newFeatures].reverse()
    for (let i = 0; i < rever.length; i++) {
      const finalPosY = y + (i + 0.5) * eachWidth
      for (let j = 0; j < this.newFeatures.length; j++) {
        const finalPosX = x + distance.xlength + distance.xlength * lay.featureDistance + (j + 0.5) * eachWidth + textFont
        const comp = this.squareList.get(this.newFeatures[j] + '_' + rever[i])
        comp.width = eachWidth
        comp.point = { x: finalPosX, y: finalPosY }
        comp.getInstance(this.lay)
      }
    }
  }
  checkTextPos() {
    const lay = this.lay
    const point = this.lay.point
    const distance = this.lay.distance
    const eachWidth = this.lay.eachWidth
    const x = point.x
    const y = point.y
    const textFont = this.lay.textFont
    const rever = [...this.newFeatures].reverse()
    const yForFeaturex = y + this.newFeatures.length * eachWidth + distance.ylength * lay.featureDistance / 2 + textFont
    for (let i = 0; i < this.newFeatures.length; i++) {
      const xForFeaturex = x + distance.xlength + distance.ylength * lay.featureDistance + (i + 0.5) * eachWidth + textFont
      Layer.component.text.drawText({
        props: {
          point: { x: xForFeaturex, y: yForFeaturex },
          text: this.newFeatures[i],
          style: {
            font: this.lay.textFont + 'px ' + TEXT_FAMILY,
            fillStyle: TEXT_COLOR
          },
          position: Layer.component.text.RIGHT,
          angle: TEXT_ANGLE
        }
      }, this.lay, '_x_' + this.newFeatures[i])
    }
    const xForFeatureY = x + distance.xlength * (1 + lay.featureDistance / 2)
    for (let i = 0; i < rever.length; i++) {
      const yForFeatureY = y + (i + 0.5) * eachWidth
      Layer.component.text.drawText({
        props: {
          point: { x: xForFeatureY, y: yForFeatureY },
          text: rever[i],
          style: {
            font: this.lay.textFont + 'px ' + TEXT_FAMILY,
            fillStyle: TEXT_COLOR
          },
          position: Layer.component.text.RIGHT,
          angle: TEXT_ANGLE
        }
      }, this.lay, '_y_' + rever[i])
    }
  }
  checkShowing(newFeatures) {
    const lay = this.lay
    const showCheckLongest = (newFeatures.length !== this.newFeatures.length)
    this.newFeatures = newFeatures
    if (!this.lay.eachWidth || showCheckLongest) {
      this.getLongestFeatureStyle(this.lay.textFont)
    } else {
      let longest = ''
      for (const item of this.newFeatures) {
        if (item.length > longest.length) {
          longest = item
        }
      }
      this.lay.distance = measureText(this.lay.$ctx, longest, {
        font: this.lay.textFont + 'px ' + TEXT_FAMILY
      }, TEXT_ANGLE)
    }
    this.newFeatures = newFeatures
    this.checkpos()
    this.checkTextPos()
    const width = lay.distance.xlength * (1 + lay.featureDistance) + this.newFeatures.length * lay.eachWidth
    const height = lay.distance.ylength * (1 + lay.featureDistance) + this.newFeatures.length * lay.eachWidth
    const point = lay.point
    lay.$meta.set('clear', { width, height, point })
  }
}

class SquareInfo {
  constructor(obj) {
    this.point = obj.point || {}
    this.width = obj.width || 0
    this.content = obj.content || '-'
    this.featureX = obj.x || ''
    this.featureY = obj.y || ''
    this.contentFont = obj.contentFont
    this.color = obj.color || this.getColorForNum(this.content)
  }
  RangeAxis() {
    const final = []
    let s = START
    let e = END
    if (s < e) {
      const m = s
      s = e
      e = m
    }
    let val = s
    while (val >= e) {
      final.push(val)
      val -= BETWEEN
    }
    return final
  }
  getColorForNum(num) {
    if (num === '-') return '#F8F8FA'
    const range = parseFloat(Math.floor((START - num) / BETWEEN * 100) / 100)
    const eachChange = parseFloat(
      Math.floor((PRECOLORS.length - 1) / (this.RangeAxis().length - 1) * 100) / 100
    )
    const poinExchangeToColor = eachChange * range
    const startColor = Layer.toRGBA(
      PRECOLORS[Math.floor(poinExchangeToColor)]
    )
    const endColor = Layer.toRGBA(PRECOLORS[Math.ceil(poinExchangeToColor)])
    const change = poinExchangeToColor - Math.floor(poinExchangeToColor)
    const colorS = startColor
      .replace('rgba(', '')
      .replace(')', '')
      .split(',')
    const colorE = endColor
      .replace('rgba(', '')
      .replace(')', '')
      .split(',')
    const re = -(parseInt(colorS[0]) - parseInt(colorE[0])) * change
    const ge = -(parseInt(colorS[1]) - parseInt(colorE[1])) * change
    const be = -(parseInt(colorS[2]) - parseInt(colorE[2])) * change
    const final = [
      parseInt(colorS[0]) + re,
      parseInt(colorS[1]) + ge,
      parseInt(colorS[2]) + be
    ]
    return 'rgb(' + final.join(',') + ')'
  }
  getInstance(lay) {
    square.drawSquare({
      props: {
        point: this.point,
        width: this.width,
        color: this.color,
        content: this.content,
        featureX: this.featureX,
        featureY: this.featureY,
        disable: (parseFloat(this.content) > lay.max || parseFloat(this.content) < lay.min),
        fontSize: lay.contentFont
      },
      events: square.events
    }, lay, this.featureX + '_' + this.featureY)
  }
}
