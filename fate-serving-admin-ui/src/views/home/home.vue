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
            <span class="name" slot="reference">{{ username }} <i class="el-icon-caret-bottom"/> </span>
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
            // this.visible = false
            // setCookie('sessionToken', '', -1)
            this.$store.dispatch('Logout').then(res => {
                this.$router.push({ path: '/home/login' })
                this.visible = false
            }).catch(err => {
            })
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
