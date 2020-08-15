import Vue from 'vue'
import Router from 'vue-router'

/* Layout */
// import Layout from '../views/layout/Layout'
// import marketingRouter from './modules/marketing-manage'

// in development-env not use lazy-loading, because lazy-loading too many pages will cause webpack hot update too slow. so only in production use lazy-loading;
// detail: https://panjiachen.github.io/vue-element-admin-site/#/lazy-loading

Vue.use(Router)

/**
 * hidden: true                   if `hidden:true` will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menu, whatever its child routes length
 *                                if not set alwaysShow, only more than one route under the children
 *                                it will becomes nested mode, otherwise not show the root menu
 * redirect: noredirect           if `redirect:noredirect` will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
    title: 'title'               the name show in subMenu and breadcrumb (recommend set)
    icon: 'svg-name'             the icon show in the sidebar
    breadcrumb: false            if false, the item will hidden in breadcrumb(default is true)
  }
 **/

export const constantRouterMap = [
    { path: '/', redirect: '/home/cluster', hidden: true },
    // { path: '/login', component: () => import('@/views/login/index'), hidden: true },
    // { path: '/regist', component: () => import('@/views/regist/index'), hidden: true },
    // { path: '*', redirect: '/home/homepage', hidden: true },
    // { path: '/home', component: () => import('@/views/home/home') },
    { path: '/404', component: () => import('@/views/404'), hidden: true },
    {
        path: '/home',
        component: () => import('@/views/home/home'),
        name: 'home',
        hidden: true,
        children: [
            {
                name: 'cluster', //
                hidden: true,
                path: 'cluster',
                component: () => import('@/views/home/cluster/index')
            }, {
                name: 'service', //
                hidden: true,
                path: 'service',
                component: () => import('@/views/home/service')
            }, {
                name: 'login', //
                hidden: true,
                path: 'login',
                component: () => import('@/views/home/login')
            }
        ]
    }
]

const router = new Router({
    // mode: 'history', // 后端支持可开
    scrollBehavior: () => ({ y: 0 }),
    routes: constantRouterMap
})

export default router
