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
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

export const constantRouterMap = [
    { path: '/', redirect: '/home/cluster', hidden: true },
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
    // mode: 'history',
    scrollBehavior: () => ({ y: 0 }),
    routes: constantRouterMap
})

export default router
