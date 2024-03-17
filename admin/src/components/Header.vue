<template>
  <a-layout-header class="header">
    <div class="logo">
      <router-link to="/welcome" style="color: white; font-size: 15px">12306后台管理</router-link>
    </div>
    <div style="float: right; color: white;">
      欢迎使用12306购票系统
    </div>
    <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="dark"
        mode="horizontal"
        :style="{ lineHeight: '64px' }"
    >
      <a-menu-item key="/welcome">
        <router-link to="/welcome">
          <coffee-outlined /> &nbsp; 欢迎
        </router-link>
      </a-menu-item>
      <a-menu-item key="/about">
        <router-link to="/about">
          <coffee-outlined /> &nbsp; 关于
        </router-link>
      </a-menu-item>
    </a-menu>
  </a-layout-header>
</template>

<script>
import {defineComponent, ref, watch} from 'vue';
import store from "@/store";
import router from "@/router";
export default defineComponent({
  name: "HeaderView",
  setup() {
    const selectedKeys = ref([]);
    let member = store.state.member;
    watch(() => router.currentRoute.value.path, (newValue) => {
      console.log("watch", newValue);
      selectedKeys.value=[];
      selectedKeys.value.push(newValue);
    },{immediate: true});
    return {
      selectedKeys,
      member,
    };
  },
});
</script>


<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.logo {
  float: left;
  width: 150px;
  height: 31px;
}
</style>
