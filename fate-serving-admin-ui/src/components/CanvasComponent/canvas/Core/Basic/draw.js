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
export default function InitDrawing(Layer) {
  Layer.prototype.drawing = function() {
    drawing.call(this)
  }

  // Setting children layer
  Layer.prototype.drawLayer = function(name, operation, close) {
    const lay = this
    let child = lay.$children.get(name)
    let newLayer = true
    if (child) {
      newLayer = false
      if (!close) {
        if (operation.props) {
          for (const prop in operation.props) {
            child[prop] = operation.props[prop]
          }
        }
      }
      if (operation.canvas) {
        if (child.$canvas !== operation.canvas) {
          child.$canvas = operation.canvas
          child.$ctx = child.$canvas.getContext('2d')
        }
      }
      if (child.$struct) child.$struct()
    } else {
      operation.__parent__ = lay
      if (operation.visiable === undefined) {
        operation.visiable = lay.$visiable
      }
      child = new Layer(operation)
      lay.$children.set(name, child)
    }
    lay.$childrenName.push(name)
    return {
      node: child,
      old: !newLayer
    }
  }

  // Setting Multi Children layer
  Layer.prototype.drawLayers = function(options) {
    const lay = this
    for (const key in options) {
      lay.drawLayer(key, options[key])
    }
  }
}

function drawing() {
  const lay = this
  const drawingList = []
  const ergodic = function(node, level = 0) {
    if (node.$children.size > 0) {
      node.$children.forEach((val, key) => {
        if (val.$children.size > 0 && val.$visiable) {
          ergodic(val, level + (val.$zindex || 0))
        } else {
          const index = level + (val.$zindex || 0)
          if (!drawingList[index]) drawingList[index] = []
          drawingList[index].push(val)
        }
      })
    } else {
      if (!drawingList[0]) drawingList[0] = []
      drawingList[0].push(node)
    }
  }
  clear.call(lay)
  const node = lay.getTopNode()
  ergodic(node, 0)
  for (const val of drawingList) {
    if (val) {
      for (const item of val) {
        if (item.$visiable) {
          if (!item.$inCanvas || item.$inCanvas()) {
            item.$path()
          }
        }
      }
    }
  }
}

// Clear Current Stuff
function clear() {
  const lay = this
  if (lay.$parent) return
  if (lay.$clear) {
    lay.$clear()
  } else {
    const width = lay.$canvas.width
    const height = lay.$canvas.height
    lay.$ctx.clearRect(-10 * width, -10 * height, width * 20, height * 20)
  }
}
