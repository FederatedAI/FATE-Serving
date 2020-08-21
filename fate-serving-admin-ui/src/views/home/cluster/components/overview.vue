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
    <ul>
        <li
            v-if="ArrProxy[0] && ArrProxy[0].children.length"
            class="proxy"
            :class="selected === 1 ? 'active' : ''"
            @click="tabNav(1,1)"
        >serving-proxy</li>
        <li v-else class="proxy disabled">serving-proxy</li>
        <li class="admin">admin</li>
        <li
            v-if="ArrServing[0] && ArrServing[0].children.length"
            class="serving"
            :class="selected === 2 ? 'active' : ''"
            @click="tabNav(2,1)"
        >serving-server</li>
        <li v-else class="serving disabled">serving-server</li>
        <li class="caret-l caret">
            <p></p>
            <i class="el-icon-caret-bottom"></i>
        </li>
        <li class="caret-r caret">
            <p></p>
            <i class="el-icon-caret-bottom"></i>
        </li>
        <li class="caret-c caret">
            <i class="el-icon-caret-left" />
            <p></p>
            <i class="el-icon-caret-right" />
        </li>
    </ul>
</template>

<script>
export default {
    name: 'overview',
    props: {
        selected: {
            type: Number,
            default() {
                return 1
            }
        },
        ArrProxy: {
            type: Array,
            default() {
                return []
            }
        },
        ArrServing: {
            type: Array,
            default() {
                return []
            }
        }
    },
    methods: {
        tabNav(index, info) {
            this.$emit('tabNav', index, info)
        }
    }
}
</script>

<style>
</style>
