<template>
  <div class="order-train">
    <span class="order-train-main">{{ dailyTrainTicket.date }}</span>&nbsp;
    <span class="order-train-main">{{ dailyTrainTicket.trainCode }}</span>次&nbsp;
    <span class="order-train-main">{{ dailyTrainTicket.start }}</span>站
    <span class="order-train-main">({{ dailyTrainTicket.startTime }})</span>&nbsp;
    <span class="order-train-main">——</span>&nbsp;
    <span class="order-train-main">{{ dailyTrainTicket.end }}</span>站
    <span class="order-train-main">({{ dailyTrainTicket.endTime }})</span>&nbsp;


    <div class="order-train-ticket">
    <span v-for="item in seatTypes" :key="item.type">
      <span>{{ item.desc }}</span>&nbsp;
      <span class="order-train-ticket-main"> ¥{{ item.price }}</span>
      <span> </span>&nbsp;
      <span class="order-train-ticket-main"> {{ item.count }}</span> 张票
    </span>
    </div>
  </div>
  <div>
    {{ passengers }}
  </div>
</template>
<script>
import {defineComponent, onMounted, ref} from "vue";
import axios from "axios";
import {notification} from "ant-design-vue";

export default defineComponent({
  setup() {
    const dailyTrainTicket = SessionStorage.get(SESSION_ORDER) || {}; // 防止空指针异常
    console.log("下单的车次信息：", dailyTrainTicket);

    const SEAT_TYPE = window.SEAT_TYPE_ARRAY;
    console.log("车座类型", SEAT_TYPE);

    const seatTypes = [];
    for (let KEY in SEAT_TYPE) {
      let key = KEY.toLowerCase();
      if (dailyTrainTicket[key] >= 0) {
        seatTypes.push({
          type: KEY,
          code: SEAT_TYPE[KEY].code,
          desc: SEAT_TYPE[KEY].desc,
          count: dailyTrainTicket[key],
          price: dailyTrainTicket[key + 'Price']
        });
      }
    }
    console.log("本车提供的车次类型：", seatTypes);

    const passengers = ref([]);
    const handlePassenger = () => {
      axios.get("/member/passenger/query-mine").then((response) => {
        let data = response.data;
        if (data.success) {
          passengers.value = data.content;
        } else {
          notification.error({description: data.message});
        }
      })
    };

    onMounted(() =>{
      handlePassenger();
    })

    return {
      dailyTrainTicket,
      seatTypes,
      passengers,
      handlePassenger
    };
  },
});
</script>

<style>
.order-train .order-train-main {
  font-size: 18px;
  font-weight: bold;
}

.order-train .order-train-ticket {
  margin-top: 15px;
}

.order-train .order-train-ticket .order-train-ticket-main {
  color: crimson;
  font-size: 18px;
}
</style>