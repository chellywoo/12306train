import { createRouter, createWebHistory } from 'vue-router'

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
    },{
      path: 'station',
      name: 'station',
      component: () => import(/* webpackChunkName: "about" */ '../views/main/station.vue'),
    },{
      path: 'train',
      name: 'train',
      component: () => import(/* webpackChunkName: "about" */ '../views/main/train.vue'),
    },{
      path: 'train-station',
      name: 'train-station',
      component: () => import(/* webpackChunkName: "about" */ '../views/main/train-station.vue'),
    },{
      path: 'train_carriage',
      name: 'train_carriage',
      component: () => import(/* webpackChunkName: "about" */ '../views/main/train-carriage.vue'),
    }]
  },{
    path: '',
    redirect: '/welcome'
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
