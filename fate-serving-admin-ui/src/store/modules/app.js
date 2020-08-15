
const app = {
    state: {
        sidebar: []
    },
    mutations: {
        TOGGLE_SIDEBAR: (state, sidebarArr) => {
            state.sidebar = sidebarArr
        }
    },
    actions: {
        ToggleSideBar: ({ commit }, keyPath) => {
            commit('TOGGLE_SIDEBAR', keyPath)
        }
    }
}

export default app
