<template>
  <a-layout-sider width="200" style="background: #fff">
    <a-menu
        v-model:selectedKeys="selectedKeys"
        :open-keys="['batch','base','business']"
        mode="inline"
        :style="{ height: '100%', borderRight: 0 }"
    >
      <a-menu-item key="/welcome">
        <router-link to="/welcome">
          <coffee-outlined/> &nbsp; 欢迎
        </router-link>
      </a-menu-item>
      <a-menu-item key="/about">
        <router-link to="/about">
          <coffee-outlined/> &nbsp; 关于
        </router-link>
      </a-menu-item>
      <a-sub-menu key="base">
        <template #title>
          <span>
            <UnorderedListOutlined/>
            用户管理
          </span>
        </template>
        <a-menu-item key="/member/ticket">
          <router-link to="/member/ticket">
            <user-outlined /> &nbsp; 车票信息
          </router-link>
        </a-menu-item>
      </a-sub-menu>
      <a-sub-menu key="base">
        <template #title>
          <span>
            <UnorderedListOutlined/>
            业务管理
          </span>
        </template>
        <a-menu-item key="/business/confirm-order">
          <router-link to="/business/confirm-order">
            <user-outlined /> &nbsp; 订单状态管理
          </router-link>
        </a-menu-item>
        <a-menu-item key="/business/daily-train-ticket">
          <router-link to="/business/daily-train-ticket">
            <user-outlined /> &nbsp; 余票管理
          </router-link>
        </a-menu-item>
        <a-menu-item key="/business/daily-train">
          <router-link to="/business/daily-train">
            <car-outlined /> &nbsp; 每日车次
          </router-link>
        </a-menu-item>
        <a-menu-item key="/business/daily-train-station">
          <router-link to="/business/daily-train-station">
            <star-outlined /> &nbsp; 每日车站
          </router-link>
        </a-menu-item>
        <a-menu-item key="/business/daily-train-carriage">
          <router-link to="/business/daily-train-carriage">
            <database-outlined /> &nbsp; 每日车厢
          </router-link>
        </a-menu-item>
        <a-menu-item key="/business/daily-train-seat">
          <router-link to="/business/daily-train-seat">
            <user-outlined /> &nbsp; 每日车座
          </router-link>
        </a-menu-item>
      </a-sub-menu>
      <a-sub-menu key="base">
        <template #title>
          <span>
            <UnorderedListOutlined/>
            基础数据
          </span>
        </template>
        <a-menu-item key="/base/station">
          <router-link to="/base/station">
            <home-outlined /> &nbsp; 车站管理
          </router-link>
        </a-menu-item>
        <a-menu-item key="/base/train">
          <router-link to="/base/train">
            <car-outlined /> &nbsp; 车次管理
          </router-link>
        </a-menu-item>
        <a-menu-item key="/base/train-station">
          <router-link to="/base/train-station">
            <star-outlined /> &nbsp; 车站大屏
          </router-link>
        </a-menu-item>
        <a-menu-item key="/base/train_carriage">
          <router-link to="/base/train_carriage">
            <database-outlined /> &nbsp; 车厢管理
          </router-link>
        </a-menu-item>
        <a-menu-item key="/base/train_seat">
          <router-link to="/base/train_seat">
            <user-outlined /> &nbsp; 车座管理
          </router-link>
        </a-menu-item>
      </a-sub-menu>
      <a-sub-menu key="batch">
        <template #title>
          <span>
            <UnorderedListOutlined/>
            跑批管理
          </span>
        </template>
        <a-menu-item key="/batch/job">
          <router-link to="/batch/job">
            <carry-out-outlined/> &nbsp; 任务管理
          </router-link>
        </a-menu-item>
      </a-sub-menu>
    </a-menu>
  </a-layout-sider>
</template>

<script>
import {defineComponent, ref, watch} from 'vue';
import store from "@/store";
import router from "@/router";
export default defineComponent({
  name: "SiderView",
  setup() {
    const selectedKeys = ref([]);
    let member = store.state.member;
    watch(() => router.currentRoute.value.path, (newValue) => {
      console.log("watch", newValue);
      selectedKeys.value = [];
      selectedKeys.value.push(newValue);
    }, {immediate: true});
    return {
      selectedKeys,
      member,
    };
  },
});
</script>


<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
