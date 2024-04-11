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
  <a-divider/>
  <b>选择乘客: </b>
  <a-checkbox-group v-model:value="passengerChecks" :options="passengerOptions"/>
<!--  <br/>-->
<!--  选中的乘客:{{ passengerChecks }}-->
<!--  <br/>-->
<!--  购票列表:{{ tickets }}-->
  <div class="order-tickets">
    <a-row class="order-tickets-header" v-if="tickets.length > 0">
      <a-col :span="2">乘客</a-col>
      <a-col :span="6">身份证</a-col>
      <a-col :span="4">票种</a-col>
      <a-col :span="4">座位类型</a-col>
    </a-row>
    <a-row class="order-tickets-row" v-for="ticket in tickets" :key="ticket.passengerId">
      <a-col :span="2">{{ ticket.passengerName }}</a-col>
      <a-col :span="6">{{ ticket.passengerIdCard }}</a-col>
      <a-col :span="4">
        <a-select v-model:value="ticket.passengerType" style="width: 100%">
          <a-select-option v-for="item in PASSENGER_TYPE" :key="item.code" :value="item.code">
            {{ item.desc }}
          </a-select-option>
        </a-select>
      </a-col>
      <a-col :span="4">
        <a-select v-model:value="ticket.seatTypeCode" style="width: 100%">
          <a-select-option v-for="item in seatTypes" :key="item.code" :value="item.code">
            {{ item.desc }}
          </a-select-option>
        </a-select>
      </a-col>
    </a-row>
  </div>
  <div v-if="tickets.length > 0">
    <a-button type="primary" size="large" @click="finishCheckPassenger">提交订单</a-button>
  </div>
  <a-modal v-model:visible="visible" title="请核对信息" style="top: 50px; width: 800px" ok-text="确认"
           cancel-text="取消" @ok="showImageCodeModal()">
    <div class="order-tickets">
      <a-row class="order-tickets-header" v-if="tickets.length > 0">
        <a-col :span="5">乘客</a-col>
        <a-col :span="8">身份证</a-col>
        <a-col :span="5">票种</a-col>
        <a-col :span="4">座位类型</a-col>
      </a-row>
      <a-row class="order-tickets-row" v-for="ticket in tickets" :key="ticket.passengerId">
        <a-col :span="5">{{ ticket.passengerName }}</a-col>
        <a-col :span="8">{{ ticket.passengerIdCard }}</a-col>
        <a-col :span="5">
          <span v-for="item in PASSENGER_TYPE" :key="item.code">
            <span v-if="item.code === ticket.passengerType">
              {{ item.desc }}
            </span>
          </span>
        </a-col>
        <a-col :span="4">
          <span v-for="item in seatTypes" :key="item.code">
            <span v-if="item.code === ticket.seatTypeCode">
              {{ item.desc }}
            </span>
          </span>
        </a-col>
      </a-row>
<!--      <br/>-->
<!--      是否支持选座：{{ chooseSeatType }}-->
<!--      <br/>-->
<!--      选座初始化：{{ chooseSeatObj }}-->
<!--      <br/>-->
<!--      所选座位类型：{{ SEAT_COL }}-->
      <div v-if="chooseSeatType === 0" style="color: red;">
        您购买的车票不支持选座
        <div>12306规则：只有全部是一等座或全部是二等座才支持选座</div>
        <div>12306规则：余票小于一定数量时，不允许选座（本项目以20为例）</div>
      </div>
      <div v-else style="text-align: center">
        <a-switch class="choose-seat-item" v-for="item in SEAT_COL" :key="item.code"
                  v-model:checked="chooseSeatObj[item.code + '1']" :checked-children="item.desc"
                  :un-checked-children="item.desc"/>
        <div v-if="tickets.length > 1">
          <a-switch class="choose-seat-item" v-for="item in SEAT_COL" :key="item.code"
                    v-model:checked="chooseSeatObj[item.code + '2']" :checked-children="item.desc"
                    :un-checked-children="item.desc"/>
        </div>
        <div style="color: #999999">提示：您可以选择{{ tickets.length }}个座位</div>
      </div>
    </div>
    <br/>
    {{ tickets }}
  </a-modal>

  <a-modal v-model:visible="imageCodeModalVisible" :title="null" :footer="null" :closable="false"
           105 style="top: 50px; width: 400px">
    <p style="text-align: center; font-weight: bold; font-size: 18px">使用服务端验证码削弱瞬时高峰<br/>防止机器人刷票
    </p>
    <p>
      <a-input v-model:value="imageCode" placeholder="图片验证码">
        <template #suffix>
          <img v-show="!!imageCodeSrc" :src="imageCodeSrc" alt="验证码" v-on:click="loadImageCode()"/>
        </template>
      </a-input>
    </p>
    <a-button type="danger" block @click="handleOk">输入验证码后开始购票</a-button>
  </a-modal>
</template>
<script>
import {computed, defineComponent, onMounted, ref, watch} from "vue";
import axios from "axios";
import {notification} from "ant-design-vue";

export default defineComponent({
  setup() {
    const passengers = ref([]);
    const passengerOptions = ref([]);
    const passengerChecks = ref([]);
    const tickets = ref([]);
    const seatTypes = [];
    const PASSENGER_TYPE = window.PASSENGER_TYPE;
    const visible = ref(false);

    const dailyTrainTicket = SessionStorage.get(SESSION_ORDER) || {}; // 防止空指针异常
    console.log("下单的车次信息：", dailyTrainTicket);

    const SEAT_TYPE = window.SEAT_TYPE_ARRAY;
    console.log("车座类型", SEAT_TYPE);

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

    // 购票列表，用于界面展示，并传递到后端接口，用来描述：哪个乘客购买什么座位的票
    // {
    //   passengerId: 123,
    //   passengerType: "1",
    //   passengerName: "张三",
    //   passengerIdCard: "12323132132",
    //   seatTypeCode: "1",
    //   seat: "C1"
    // }
    // 勾选或去掉某个乘客时，在购票列表中加上或去掉一张表
    watch(() => passengerChecks.value, (newVal, oldVal) => {
      console.log("勾选乘客时发生变化", newVal, oldVal);
      tickets.value = [];
      passengerChecks.value.forEach((item) => tickets.value.push({
        passengerId: item.id,
        passengerType: item.type,
        seatTypeCode: seatTypes[0].code,
        passengerName: item.name,
        passengerIdCard: item.idCard

      }))
    },{immediate: true});

    // 0：不支持选座；1：选一等座；2：选二等座
    const chooseSeatType = ref(0);
    // 根据选择的座位类型，计算出对应的列，比如要选的是一等座，就筛选出ACDF，要选的是二等座，就筛选出ABCDF
    const SEAT_COL = computed(() => {
      return window.SEAT_COL.filter(item => item.type === chooseSeatType.value);
    });
    // 选择的座位
    // {
    //   A1: false, C1: true，D1: false, F1: false，
    //   A2: false, C2: false，D2: true, F2: false
    // }
    const chooseSeatObj = ref({});
    watch(() => SEAT_COL.value, () => {
      chooseSeatObj.value = {};
      for (let i = 1; i <= 2; i++) {
        SEAT_COL.value.forEach((item) => {
          chooseSeatObj.value[item.code + i] = false;
        })
      }
      console.log("初始化两排座位，都是未选中：", chooseSeatObj.value);
    }, {immediate: true});

    const handlePassenger = () => {
      axios.get("/member/passenger/query-mine").then((response) => {
        let data = response.data;
        if (data.success) {
          passengers.value = data.content;
          passengers.value.forEach((item) => passengerOptions.value.push({
            label: item.name,
            value: item
          }))
        } else {
          notification.error({description: data.message});
        }
      })
    };

    const finishCheckPassenger = () =>{
      console.log("购票列表:", tickets.value);
      if(tickets.value.length > 5){
        notification.error({description:"最多只能购买五张票"});
        return;
      }
      // 校验余票是否充足，购票中的每个座位类型，都去车次余票列表中查看余票是否充足
      let seatTypeTemp = Tool.copy(seatTypes);
      for (let i = 0; i < tickets.value.length; i++) {
        let ticket = tickets.value[i];
        for(let j = 0; j < seatTypeTemp.length; j++){
          let seatType = seatTypeTemp[j];
          if(ticket.seatTypeCode === seatType.code){
            seatType.count--;
            if(seatType.count < 0 ) {
              notification.error({description: seatType.desc + "余票不足"});
              return;
            }
          }
        }
      }
      console.log("前端余票校验通过");

      // 判断是否支持选座，只有纯一等座和纯二等座支持选座
      // 先筛选出购票列表中的所有座位类型，比如四张表：[1, 1, 2, 2]
      let ticketSeatTypeCodes = [];
      for (let i = 0; i < tickets.value.length; i++) {
        let ticket = tickets.value[i];
        ticketSeatTypeCodes.push(ticket.seatTypeCode);
      }
      // 为购票列表中的所有座位类型去重：[1, 2]
      const ticketSeatTypeCodesSet = Array.from(new Set(ticketSeatTypeCodes));
      console.log("选好的座位类型：", ticketSeatTypeCodesSet);
      if (ticketSeatTypeCodesSet.length !== 1) {
        console.log("选了多种座位，不支持选座");
        chooseSeatType.value = 0;
      } else {
        // ticketSeatTypeCodesSet.length === 1，即只选择了一种座位（不是一个座位，是一种座位）
        if (ticketSeatTypeCodesSet[0] === SEAT_TYPE.YDZ.code) {
          console.log("一等座选座");
          chooseSeatType.value = SEAT_TYPE.YDZ.code;
        } else if (ticketSeatTypeCodesSet[0] === SEAT_TYPE.EDZ.code) {
          console.log("二等座选座");
          chooseSeatType.value = SEAT_TYPE.EDZ.code;
        } else {
          console.log("不是一等座或二等座，不支持选座");
          chooseSeatType.value = 0;
        }
      }

      // 查询余票是否大于20，若小于20就不允许选座
      if(chooseSeatType.value !== 0){
        for(let i = 0; i < seatTypes.length; i++){
          let seatType = seatTypes[i];
          if(seatType.code === ticketSeatTypeCodesSet[0]){
            if(seatType.count < 20){
              console.log("余票不足20，无法选座");
              chooseSeatType.value = 0;
              break;
            }
          }
        }
      }

      visible.value = true;
    }

    const handleOk = () => {
      if(Tool.isEmpty(imageCode.value)){
        notification.error({description:'验证码不能为空'});
        return;
      }

      console.log("选好的座位: "+ chooseSeatObj.value);
      // 增加乘客数与选座数的校验，给乘客购票时返回的数据添加票的类型
      // 清空已存在的列表
      for(let count = 0; count < tickets.value.length; count++){
        tickets.value[count].seat = null;
      }
      // 设置每张票的座位
      let i=-1;
      for(let key in chooseSeatObj.value){
        if(chooseSeatObj.value[key]){
          i++;
          if( i > tickets.value.length - 1){
            notification.error({description: "选取的座位大于乘客数"});
            return;
          }
          tickets.value[i].seat = key;
        }
      }
      if(i > -1 && i < (tickets.value.length - 1)){
        notification.error({description: "选取的座位小于乘客数"});
        return;
      }
      console.log("最终购票:" + tickets.value);

      axios.post("/business/confirm-order/accept", {
        dailyTrainTicketId: dailyTrainTicket.id,
        date: dailyTrainTicket.date,
        trainCode: dailyTrainTicket.trainCode,
        start: dailyTrainTicket.start,
        end: dailyTrainTicket.end,
        tickets: tickets.value
      }).then((response) => {
        let data = response.data;
        if (data.success) {
          notification.success({description: "下单成功！"});
        } else {
          notification.error({description: data.message});
        }
      });
    }

    /*-----------------验证码------------------*/
    const imageCodeModalVisible = ref();
    const imageCodeToken = ref();
    const imageCodeSrc = ref();
    const imageCode = ref();
    /**
     * 加载图形验证码
     */
    const loadImageCode = () => {
      imageCodeToken.value = Tool.uuid(8);
      imageCodeSrc.value = process.env.VUE_APP_SERVER + '/business/kaptcha/image-code/' + imageCodeToken.value;
    };
    const showImageCodeModal = () => {
      loadImageCode();
      imageCodeModalVisible.value = true;
    };

    onMounted(() => {
      handlePassenger();
    })

    return {
      dailyTrainTicket,
      seatTypes,
      passengers,
      handlePassenger,
      passengerOptions,
      passengerChecks,
      tickets,
      PASSENGER_TYPE,
      visible,
      finishCheckPassenger,
      SEAT_COL,
      chooseSeatObj,
      chooseSeatType,
      handleOk,
      imageCode,
      imageCodeSrc,
      imageCodeToken,
      imageCodeModalVisible,
      loadImageCode,
      showImageCodeModal
    };
  },
});
</script>

<style>
#app {
  text-align: left;
}

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
.order-tickets {
  margin: 10px 0;
}
.order-tickets .ant-col {
  padding: 5px 10px;
}
.order-tickets .order-tickets-header {
  background-color: #9bbdf6;
  border: solid 1px #9bbdf6;
  color: darkblue;
  font-size: 16px;
  padding: 5px 0;
}
.order-tickets .order-tickets-row {
  border: solid 1px cornflowerblue;
  border-top: none;
  vertical-align: middle;
  line-height: 30px;
}
.order-tickets .choose-seat-item {
  margin: 5px 5px;
}
</style>