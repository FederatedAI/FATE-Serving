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
<img src="@/assets/welcomepage.svg" alt="" />
  <div class="login">
    <div class="title">Welcome</div>
    <div class="warning" v-if="passwordWarn">
        <em class="el-icon-warning"></em>
        <span>
            Incorrect username or password. <br>
            The default username is admin, the default password is admin.
        </span>
    </div>
    <div class="form">
      <div class="label">
        <span>User name</span>
      </div>
      <div class="username" :class="{ name:true,'name-warn': passwordWarn,nameIn:nameInputStatus }">
        <el-autocomplete
            popper-class="autopopper"
            v-model="form.username"
            :fetch-suggestions="querySearchAsync"
            @keyup.enter.native="submit"
            @focus="nameInputStatus = true"
            @blur="nameInputStatus = false"
            @select="handleSelect"
            >
            <template slot-scope="{ item }">
                <em class="el-icon-s-custom"></em>
                <div>
                    <div class="name">{{ item.value }}</div>
                    <span class="addr">******</span>
                </div>
                <span  class="default" v-if="item.value === 'admin'">(default)</span>
            </template>
        </el-autocomplete>
      </div>
    </div>
    <div class="form">
      <div class="label">
        <span>Password</span>
      </div>
      <div :class="{ name:true,'name-warn': passwordWarn,nameIn:pwdInputStatus}">
        <el-input
          :type="pwdType"
          v-model.trim="form.password"
          :class="{ 'active': passwordInput }"
          placeholder=""
          @keyup.enter.native="submit"
          @focus="pwdInputStatus = true"
          @blur="pwdInputStatus = false"
          ref='pwdInput'>
        <em slot="suffix" class="el-icon-view view" @click="showPwd"/></el-input>
      </div>
    </div>
    <div>
        <el-checkbox v-model="checked">Remember me</el-checkbox>
    </div>
    <div class="btn">
      <el-button class="OK-btn" :type="type" :disabled="disabledbtn" @click.native.prevent="submit" >Login</el-button>
    </div>
  </div>
  </div>
</template>

<script>

export default {
    name: 'home',
    components: {},
    data() {
        return {
            pwdType: 'password',
            nameInputStatus: false,
            pwdInputStatus: false,
            passwordInput: false, // 是否显示输入框样式
            passwordWarn: false, // 显示警告样式
            disabledbtn: true, // 按钮可点击
            type: 'primary',
            checked: true,
            form: {
                username: '',
                password: ''
            }
        }
    },
    watch: {
        form: {
            handler: function(val) {
                if (val.username && val.password) {
                    this.disabledbtn = false
                } else {
                    this.disabledbtn = true
                }
            },
            immediate: true,
            deep: true
        }
    },
    computed: {},
    mounted() {
        this.restaurants = this.loadAll()
    },
    methods: {
        submit() {
            this.$store.dispatch('Login', this.form).then(res => {
                this.$router.push({ path: '/home/cluster' })
            }).catch(() => {
                this.passwordWarn = true
                this.$refs.pwdInput.focus()
            })
        },
        showPwd() {
            if (this.pwdType === 'password') {
                this.pwdType = ''
            } else {
                this.pwdType = 'password'
            }
        },
        querySearchAsync(queryString, cb) {
            var results = queryString ? this.restaurants.filter(this.createStateFilter(queryString)) : this.restaurants
            cb(results)
        },
        createStateFilter(queryString) {
            return (state) => {
                return (state.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0)
            }
        },
        handleSelect(item) {
            this.form.username = item.value
            this.form.password = item.address
            this.$refs.pwdInput.focus()
        },
        loadAll() {
            return [
                { 'value': 'admin', 'address': 'admin' }
            ]
        }
    }
}
</script>

<style rel="stylesheet/scss" lang="scss" >
.autopopper .popper__arrow {
    display: none;
     border-bottom-color: #fff !important;
     top: 0 !important;
}
.autopopper {
    margin: 0 auto;
    width: 27vmax !important;
    left: 36.5vmax !important;
    margin-top: 9px !important;
    .el-scrollbar__wrap {
        padding: 10px 0 !important;
        li {
            line-height: 20px !important;
            padding-top: 3px;
            display: flex;
            justify-content:flex-start;
            .default {
                float: right;
                margin-left: 17vw;
                margin-top: 8px;
                color:rgba(132,140,153,1);
            }
            .el-icon-s-custom {
                margin-top: 6px;
                margin-right: 20px;
                font-size: 24px;
                color:rgba(230,235,240,1);
                opacity:1;
            }
        }
    }
}
</style>
