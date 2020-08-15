import router from './router'
// import store from '@/store'
import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css' // progress bar style
import { getToken } from '@/utils/auth'
NProgress.configure({ showSpinner: false })// NProgress configuration

// 不重定向白名单
const whiteList =
  [
      '/home/login'
  ]
router.beforeEach((to, from, next) => {
    NProgress.start()
    if (getToken()) {
        if (to.path === '/home/login') {
            next({ path: '/home/cluster' })
            NProgress.done()
        } else {
            next()
        }
    } else {
        if (whiteList.indexOf(to.path) !== -1) {
            next()
        } else {
            next({ path: '/home/login' })
            NProgress.done()
        }
    }
})

router.afterEach(() => {
    NProgress.done() // 结束Progress
})
