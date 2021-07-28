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
import { toRGBA } from './utils'

export default function InitTranslate(Layer) {
  Layer.prototype.InitTranslate = function({ translate }) {
    if (translate) {
      for (const key in translate) {
        if (Array.isArray(translate[key])) {
          this.registerChainTranslate(false, translate[key])
        } else if (typeof translate[key] === 'object') {
          if (typeof translate[key].repeat === 'boolean' && translate[key].steps) {
            this.registerChainTranslate(translate[key].repeat, ...translate[key].steps)
          } else {
            this.registerChainTranslate(false, translate[key])
          }
        }
      }
    }
  }

  Layer.prototype.registerChainTranslate = function(name, repeat, ...obj) {
    const trans = new Chain(name, repeat, this, obj)
    beats.set(name + '_' + this.$uuid, trans)
    process()
  }

  Layer.prototype.deleteChainTranslate = function(name) {
    beats.delete(name + '_' + this.$uuid)
  }

  Layer.prototype.deleteAllAboutChain = function() {
    const lay = this
    if (beats.size > 0) {
      for (const item of beats) {
        if (item[0].match(lay.$uuid)) {
          beats.delete(item[0])
          continue
        }
      }
    }
    if (lay.$children.size > 0) {
      for (const val of lay.$children) {
        val[1].deleteAllAboutChain()
      }
    }
  }

  Layer.prototype.settingRGBA = function(from, to, setting, costTime, betweenTime) {
    const final = {
      interval: betweenTime || intervalTime,
      lay: this,
      target: to,
      origin: from,
      process() {
        const origin = toRGBA(this.origin).replace('rgba(', '').replace(')$', '').split(',')
        const target = toRGBA(this.target).replace('rgba(', '').replace(')$', '').split(',')
        const current = toRGBA(this.current).replace('rgba(', '').replace(')$', '').split(',')
        for (let i = 0; i < origin.length; i++) {
          const once = (parseFloat(target[i]) - parseFloat(origin[i])) / Math.floor((costTime || intervalTime) / (betweenTime || intervalTime))
          current[i] = parseFloat(current[i]) + once
          if ((once < 0 && current[i] < parseFloat(target[i])) || (once > 0 && current[i] > parseFloat(target[i]))) {
            current[i] = parseFloat(target[i])
          }
        }
        setting.call(this.lay, 'rgba(' + current.join(',') + ')')
        return 'rgba(' + current.join(',') + ')' === 'rgba(' + target.join(',') ? true : 'rgba(' + current.join(',') + ')'
      }
    }
    return final
  }

  Layer.prototype.settingNum = function(from, to, setting, costTime, betweenTime) {
    const final = {
      interval: betweenTime || intervalTime,
      lay: this,
      target: to,
      origin: from,
      process() {
        const once = (this.target - this.origin) / Math.floor((costTime || intervalTime) / (betweenTime || intervalTime))
        let current = this.current + once
        if ((once < 0 && current < this.target) || (once > 0 && current > this.target)) {
          current = this.target
        }
        this.current = current
        setting.call(this.lay, current)
        return current === this.target ? true : current
      }
    }
    return final
  }

  Layer.prototype.setting = function(to, setting, costTime) {
    const final = {
      interval: costTime || intervalTime,
      lay: this,
      target: to,
      origin: null,
      process() {
        setting.call(this.lay, to)
        return Transform.FINISH
      }
    }
    return final
  }

  Layer.prototype.settingCus = function(process, costTime) {
    const final = {
      interval: costTime || intervalTime,
      lay: this,
      target: null,
      origin: null,
      process: function() {
        process.call(this)
        return true
      }
    }
    return final
  }

  Layer.prototype.setCus = function(cusname, process, costTime) {
    const name = cusname || Layer.getUUID('trasnlate')
    this.registerChainTranslate(name, false, this.settingCus(process, costTime))
    return name
  }

  Layer.prototype.set = function(cusname, to, setting, costTime) {
    const name = cusname || Layer.getUUID('trasnlate')
    const final = {
      interval: costTime || intervalTime,
      lay: this,
      target: to,
      origin: null,
      process() {
        setting.call(this.lay, to)
        return Transform.FINISH
      }
    }
    this.registerChainTranslate(name, false, final)
    return name
  }
}

const beats = new Map()
let processing = null
const intervalTime = 10

function process() {
  if (beats.size > 0 && processing === null) {
    processing = setTimeout(eachStep, intervalTime)
  }
}

function eachStep() {
  const redrawList = []
  for (const val of beats) {
    const result = val[1].getOnce()
    if (result === null) {
      beats.delete(val[0])
    } else if (result) {
      restruct(val[1].lay)
      const top = val[1].lay.getTopNode()
      let index = 0
      for (const vall of redrawList) {
        if (top.$uuid === vall.$uuid) {
          break
        }
        index++
      }
      if (index >= redrawList.length) {
        redrawList.push(top)
      }
      if (val[1].chainFinish) {
        beats.delete(val[0])
      }
    }
  }
  for (const val of redrawList) {
    val.drawing()
  }
  if (beats.size > 0) {
    processing = setTimeout(eachStep, intervalTime)
  } else {
    clearTimeout(processing)
    processing = null
  }
}

function restruct(lay) {
  if (lay.$struct) {
    lay.$struct()
  }
}

class Chain {
  constructor(name, repeat, lay, translates) {
    this.name = name
    this.repeat = repeat
    this.chainList = []
    this.chainFinish = false
    this.lay = lay
    this.hasCalledOnce = false
    for (const val of translates) {
      val.lay = lay
      this.chainList.push(new Transform(val))
    }
  }
  getOnce() {
    let final = null
    for (const val of this.chainList) {
      if (val.finish) {
        continue
      } else {
        final = val.getOnce()
        break
      }
    }
    if (this.complete()) {
      if (this.repeat) {
        this.restart()
      } else {
        this.chainFinish = true
      }
    }
    this.hasCalledOnce = true
    return final
  }
  complete() {
    for (const val of this.chainList) {
      if (!val.finish) {
        return false
      }
    }
    return true
  }
  restart() {
    for (const val of this.chainList) {
      val.restart()
    }
  }
}

class Transform {
  constructor({ target, process, interval, origin, lay, compare }) {
    this.target = target // for what target
    this.process = process
    this.interval = interval
    this.origin = origin
    this.lay = lay
    this.finish = false
    this.current = this.origin
    this.round = 0
    this.compare = compare
  }
  getOnce() {
    this.round++
    let final = false
    if (this.round * intervalTime >= this.interval) {
      this.current = this.process()
      this.complete()
      this.round = 0
      final = true
    }
    return final
  }
  getCurrent(space) {
    const times = Math.floor(space / this.interval)
    let final = this.origin
    for (let i = 0; i < times; i++) {
      final = this.process.call(this.lay, final)
    }
    return final
  }
  complete(check) {
    const c = check || this.current
    if (this.compare) {
      this.finish = this.compare()
    } else {
      this.finish = (c === this.target) || (c === true) || (c === Transform.FINISH)
    }
    return this.finish
  }
  restart() {
    this.finish = false
    this.round = 0
    this.current = this.origin
  }
}

Transform.FINISH = 'FINISH'
