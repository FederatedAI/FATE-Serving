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
    <div class="navbar">
        <ul>
            <li :class="selected === 0 ? 'active' : ''" @click="tabNav(0)">Cluster</li>
            <li :class="selected === 1 ? 'active' : ''" @click="tabNav(1)">Service</li>
        </ul>
    </div>
</template>

<script>
export default {
    name: 'navbar',
    components: {},
    data() {
        return {
            selected: 0
        }
    },
    watch: {
        '$route.name'(val) {
            if (val === 'service') {
                this.selected = 1
            } else if (val === 'cluster') {
                this.selected = 0
            }
        }
    },
    computed: {},
    created() {
        if (this.$route.name === 'service') {
            this.selected = 1
        } else if (this.$route.name === 'cluster') {
            this.selected = 0
        }
    },
    methods: {
        tabNav(index) {
            let path
            this.selected = +index
            if (+index === 1) {
                path = '/home/service'
            } else {
                path = '/home/cluster'
            }
            this.$router.push({
                path: path
            })
        }
    }
}
</script>

<style rel="stylesheet/scss" lang="scss">
.navbar{
    overflow: hidden;
    margin-bottom: 10px;
    ul {
        li {
            list-style: none;
            float: left;
            width: 100px;
            padding: 8px 0;
            text-align: center;
            background-color: #E6EBF0;
            color: #217ad9;
            cursor: pointer;
        }
    }
    .active{
        background-color: #217ad9;
        color: #fff;
    }
}
</style>
