
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
 * Manangement component`s status
 * @param {Object} layer Constructe function
 */

import { mergeObjTo, getUUID } from './utils'

export default function InitStatus(layer) {
  layer.prototype._InitStatus = function(obj) {
    const lay = this
    lay._inited = false
    lay.$children = new Map()
    lay.$childrenName = []
    lay.$parent = null // Parent componentfor current layer-object
    lay.$meta = new Map()
    inited(lay, obj)
  }

  // get toppest component of current node
  layer.prototype.getTopNode = function() {
    const lay = this
    let topParent = lay
    while (topParent.$parent) {
      topParent = topParent.$parent
    }
    return topParent
  }

  // Has need updated current picture
  layer.prototype.destory = function() {
    const lay = this
    destory(lay)
  }
}

// Initing basic information and attrs for component
/**
 * props: drawing attribute, need to be object
 * struct: funcion for getting children component,
 * path: need drawing,
 * here: checking mouse position do in area of component,
 * clear: how to clear picture of current component,
 * zindex: current component will drawing in which level
 */
function inited(lay, { canvas, props, struct, path, here, clear, zindex, visiable = true, _const = false, __parent__, __inCanvas__ }) {
  const helper = {}
  if (!lay.$uuid) helper.$uuid = getUUID('layer')
  if (canvas) {
    helper.$canvas = canvas
    helper.$ctx = canvas.getContext('2d')
  }
  if (props) { // Initing props
    for (const key in props) {
      helper[key] = props[key]
    }
  }
  if (path) helper.$path = path
  if (here) helper.$here = here
  else {
    if (struct) {
      helper.$here = function(point) {
        let final = false
        for (const val of lay.$children) {
          final = val[1].$here(point)
          if (final) break
        }
        return final
      }
    } else {
      helper.$here = (point) => { return false }
    }
  }
  if (clear) helper.$clear = clear
  if (zindex) helper.$zindex = zindex
  if (struct) {
    helper.$struct = function() {
      lay.$childrenName = []
      struct.call(lay)
      for (const val of lay.$children) {
        if (lay.$childrenName.indexOf(val[0]) < 0 && !val[1].$const) {
          lay.$children.delete(val[0])
        }
      }
    }
  }
  if (__parent__) {
    helper.$parent = __parent__
    if (_const) helper.$const = _const
  }
  if (__inCanvas__) {
    helper.$inCanvas = __inCanvas__
  }
  helper.$visiable = visiable
  mergeObjTo(lay, helper)
  if (struct) lay.$struct() // Getting children for current component
  return lay
}

function destory(lay) {
  const childs = lay.$children
  lay.$children = null
  lay.$parent = null
  lay.$events = {}
  childs.forEach((val) => {
    val.destory(val)
  })
}
