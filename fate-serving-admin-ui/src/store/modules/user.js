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
