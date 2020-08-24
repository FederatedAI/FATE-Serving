<!--
  Copyright 2019 The FATE Authors. All Rights Reserved.

  Licensed under the Apache License, Version 2.0 (the 'License');
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an 'AS IS' BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

 -->
<template>
  <div class='menu-container'>
    <template v-for='v in menuList'>
      <el-submenu
        :index='v.name'
        v-if='v.children&&v.children.length>0'
        :key='v.name'
      >
        <template slot='title'>
          <!-- <i
            class='iconfont'
            :class='v.meta.icon'
          ></i> -->
          <svg-icon v-if="v.meta&&v.meta.icon" :icon-class="v.meta.icon"></svg-icon>
          <span>{{v.meta.name}}</span>
        </template>
        <el-menu-item-group>
          <my-nav :menuList='v.children'></my-nav>
        </el-menu-item-group>
      </el-submenu>
      <el-menu-item
        :key='v.name'
        :index='v.name'
        @click='gotoRoute(v.name)'
        v-else
      >
        <!-- <i
          class='iconfont'
          :class='v.meta.icon'
        ></i> -->
        <svg-icon v-if="v.meta&&v.meta.icon" :icon-class="v.meta.icon"></svg-icon>
        <span slot='title'>{{v.meta.name}}</span>
      </el-menu-item>
    </template>
  </div>
</template>

<script>
export default {
    name: 'my-nav',
    props: {
        menuList: {
            type: Array,
            default: () => {
                return []
            }
        }
    },
    methods: {
        gotoRoute(name) {
            this.$router.push({ name })
        }
    }
}
</script>

<style lang='scss'>
.menu-container {
  .svg-icon{
    margin-right:10px;
  }
}
</style>
