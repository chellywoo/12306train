import {createRouter, createWebHistory} from 'vue-router'

const routes = [
    {
        path: '/',
        name: 'window',
        component: () => import(/* webpackChunkName: "about" */ '../views/Window.vue'),
        meta: {
            loginRequire: true
        },
        children: [{
            path: 'welcome',
            name: 'welcome',
            component: () => import(/* webpackChunkName: "about" */ '../views/main/welcome.vue'),
        }, {
            path: 'about',
            name: 'about',
            component: () => import(/* webpackChunkName: "about" */ '../views/main/about.vue'),
        }, {
            path: 'batch/',
            name: 'batch/',
            children: [{
                path: 'job',
                name: 'job',
                component: () => import(/* webpackChunkName: "about" */ '../views/main/batch/job.vue'),
            }]
        }, {
            path: 'business/',
            name: 'business/',
            children: [{
                path: 'daily-train',
                name: 'daily-train',
                component: () => import(/* webpackChunkName: "about" */ '../views/main/business/daily-train.vue'),
            },{
                path: 'daily-train-station',
                name: 'daily-train-station',
                component: () => import(/* webpackChunkName: "about" */ '../views/main/business/daily-train-station.vue'),
            },{
                path: 'daily-train-carriage',
                name: 'daily-train-carriage',
                component: () => import(/* webpackChunkName: "about" */ '../views/main/business/daily-train-carriage.vue'),
            },{
                path: 'daily-train-seat',
                name: 'daily-train-seat',
                component: () => import(/* webpackChunkName: "about" */ '../views/main/business/daily-train-seat.vue'),
            }]
        }, {
            path: 'base/',
            name: 'base/',
            children: [{
                path: 'station',
                name: 'station',
                component: () => import(/* webpackChunkName: "about" */ '../views/main/base/station.vue'),
            }, {
                path: 'train',
                name: 'train',
                component: () => import(/* webpackChunkName: "about" */ '../views/main/base/train.vue'),
            }, {
                path: 'train-station',
                name: 'train-station',
                component: () => import(/* webpackChunkName: "about" */ '../views/main/base/train-station.vue'),
            }, {
                path: 'train_carriage',
                name: 'train_carriage',
                component: () => import(/* webpackChunkName: "about" */ '../views/main/base/train-carriage.vue'),
            }, {
                path: 'train_seat',
                name: 'train_seat',
                component: () => import(/* webpackChunkName: "about" */ '@/views/main/base/train-seat.vue'),
            }]
        }

        ]
    }, {
        path: '',
        redirect: '/welcome'
    }
]

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes
})

export default router
