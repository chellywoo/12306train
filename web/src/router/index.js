import { createRouter, createWebHistory } from 'vue-router'
import store from "@/store";
import {notification} from "ant-design-vue";

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import(/* webpackChunkName: "about" */ '../views/Login.vue')
  },

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
      path: 'passenger',
      name: 'passenger',
      component: () => import(/* webpackChunkName: "about" */ '../views/main/passenger.vue'),
    }, {
      path: 'ticket',
      name: 'ticket',
      component: () => import(/* webpackChunkName: "about" */ '../views/main/ticket.vue'),
    }, {
      path: 'order',
      name: 'order',
      component: () => import(/* webpackChunkName: "about" */ '../views/main/order.vue'),
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

// 路由登录拦截
router.beforeEach((to, from, next) => {
  // 要不要对meta.loginRequire属性做监控拦截
  if (to.matched.some(function (item) {
    console.log(item, "是否需要登录校验：", item.meta.loginRequire || false);
    return item.meta.loginRequire
  })) {
    const _member = store.state.member;
    console.log("页面登录校验开始：", _member);
    if (!_member.token) {
      console.log("未登录或连接超时！");
      notification.error({ description: "未登录或连接超时" });
      next('/login');
    } else {
      next();
    }
  } else {
    next();
  }
});


export default router
