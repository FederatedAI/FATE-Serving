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
  <div class="home">
    <el-header>
      <div class="logo">
        <span class="logo-t" @click="toHome">FATE Serving</span>
        <div v-if="token">
            <el-popover
            placement="top"
            width="160"
            v-model="visible">
            <p class="pname" style="text-align: center;">{{ username }}</p>
            <div style="text-align: center; margin: 0">
                <el-button type="primary" class="exitbut" size="mini" @click="exit">Exit <img class="exits" src="@/assets/exits.png" alt="" srcset=""></el-button>
            </div>
            <span class="name" slot="reference">{{ username }} <em class="el-icon-caret-bottom"/> </span>
            </el-popover>
        </div>
      </div>
    </el-header>
    <el-main>
      <navbar v-if="showNav"/>
      <router-view />
    </el-main>
  </div>
</template>

<script>
import navbar from './components/navbar'
import { mapState, mapGetters } from 'vuex'
export default {
    name: 'home',
    components: {
        navbar
    },
    data() {
        return {
            showNav: false,
            username: localStorage.name,
            visible: false
        }
    },
    watch: {
        $route: {
            handler: function(val) {
                if (val.name === 'login') {
                    this.showNav = false
                } else {
                    this.showNav = true
                    this.username = localStorage.name
                }
            },
            immediate: true
        }
    },
    computed: {
        ...mapState(['name']),
        ...mapGetters([
            'token'
        ])
    },

    methods: {
        toHome() {
            this.$router.push({
                name: 'cluster',
                path: '/home/cluster',
                query: {}
            })
        },
        exit() {
            this.$store.dispatch('Logout').then(res => {
                this.$router.push({ path: '/home/login' })
                this.visible = false
            }).catch()
        }
    }
}
</script>

<style rel="stylesheet/scss" lang="scss" >

@media screen and (max-width: 1400px) {
    .home {
        font-size: 16px;
    }
}
@media screen and (max-width: 1250px) {
    .home {
        font-size: 14px;
    }
}
@media screen and (max-width: 1100px) {
    .home {
        font-size: 12px;
    }
}
@media screen and (max-width: 950px) {
    .home {
        font-size: 10px;
    }
}
@media screen and (max-width: 800px) {
    .home {
        font-size: 8px;
    }
}

@import 'src/styles/home.scss';
</style>
