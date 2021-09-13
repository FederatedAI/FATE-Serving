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
export default function InitEvents(Layer) {
  Layer.prototype._InitEvents = function({ events }) {
    const lay = this
    lay.$events = events || {}
    lay.$events.$showing = function() {
      const layer = this
      layer.$visiable = true
      if (layer.$children.size > 0) {
        for (const val of layer.$children) {
          val[1].emit('$showing')
        }
      }
    }
    lay.$events.$hide = function() {
      const layer = this
      layer.$visiable = false
      if (layer.$children.size > 0) {
        for (const val of layer.$children) {
          val[1].emit('$hide')
        }
      }
    }
    lay.$events.$translate = function(x, y) {
      const layer = this
      if (!layer.$meta) layer.$meta = new Map()
      layer.$meta.set('$translate', { x: x || 0, y: y || 0 })
    }
  }

  Layer.prototype.$showing = function() {
    this.emit('$showing')
  }

  Layer.prototype.$hide = function() {
    this.emit('$hide')
  }

  Layer.prototype.emit = function(type, ...props) {
    const lay = this
    let finish = ''
    for (const val of lay.$children) {
      finish = val[1].emit(type, ...props)
      if (finish === 'finish') {
        return 'finish'
      }
    }
    if (lay.$events[type]) {
      if (!Array.isArray(lay.$events[type])) {
        lay.$events[type] = [lay.$events[type]]
      }
      for (const val of lay.$events[type]) {
        finish = finish || val.call(lay, ...props)
      }
    }
    return finish
  }

  Layer.prototype.addEvent = function(type, operation) {
    const lay = this
    if (!lay.$events[type]) lay.$events = []
    lay.$events.push(operation)
  }

  Layer.prototype.removeEvent = function(type) {
    const lay = this
    lay.$events[type] = []
  }
}
