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
    <div>
        <div
            class="ip-list-li"
            v-for="(item,index) in ipData.children"
            :key="index"
        >
            <span style="cursor: pointer;" @click="selectIP(item,index)" :class="activeip === index ? 'activeip' : ''">{{ item && item.name }}</span>
            <span
                class="iptime"
                :class="activeip === index ? 'activetime' : ''"
            >{{ item.timestamp | dateform}}</span>
        </div>
    </div>
</template>

<script>
export default {
    name: 'iplist',
    props: {
        activeip: {
            type: Number,
            default() {
                return 0
            }
        },
        ipData: {
            type: Object,
            default() {
                return {}
            }
        }
    },
    methods: {
        selectIP(item, index) {
            this.$emit('selectIP', item, index)
        }
    }
}
</script>

<style>
</style>
