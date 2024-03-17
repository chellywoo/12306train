<template>
  <HeaderView></HeaderView>
  <a-layout>
    <SiderView></SiderView>
    <a-layout-content
        :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
    >
      <router-view></router-view>
    </a-layout-content>
  </a-layout>
</template>
<script>
import {defineComponent} from 'vue';
import HeaderView from "@/components/Header.vue";
import SiderView from "@/components/Sider.vue";
export default defineComponent({
  components: {
    SiderView,
    HeaderView,
  },
  setup() {
    return {};
  },
});
</script>
<style>

</style>