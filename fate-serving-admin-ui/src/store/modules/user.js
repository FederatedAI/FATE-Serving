/**
*  Copyright 2019 The FATE Authors. All Rights Reserved.
*
*  Licensed under the Apache License, Version 2.0 (the 'License');
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an 'AS IS' BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
**/
import { login, logout } from '@/api/user'
import { getToken, setToken, removeToken } from '@/utils/auth'

const user = {
    state: {
        token: getToken()
    },
    mutations: {
        SET_TOKEN: (state, token) => {
            state.token = token
        }
    },

    actions: {
        Login({ commit }, info) {
            return new Promise((resolve, reject) => {
                login(info).then(response => {
                    const data = response.data
                    const token = data.sessionToken
                    localStorage.name = info.username
                    setToken(token)
                    commit('SET_TOKEN', token)
                    resolve()
                }).catch(error => {
                    reject(error)
                })
            })
        },
        Logout({ commit }) {
            return new Promise((resolve, reject) => {
                logout().then(response => {
                    removeToken()
                    commit('SET_TOKEN', '')
                    resolve()
                }).catch(error => {
                    reject(error)
                })
            })
        }
    }
}

export default user
