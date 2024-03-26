<template>
  <div>{{ dailyTrainTicket }}</div>
</template>
<script>
import {defineComponent} from "vue";

export default defineComponent({
  setup() {
    const dailyTrainTicket = SessionStorage.get("dailyTrainTicket") || {}; // 防止空指针异常
    console.log("下单的车次信息：",dailyTrainTicket);
    return {
      dailyTrainTicket
    };
  },
});
</script>

<style>

</style>