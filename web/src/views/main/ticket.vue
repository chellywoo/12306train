<template>
  <!--  <h1>乘客界面</h1>-->
  <p>
    <a-space style="width: 100%">
      <a-date-picker v-model:value="params.date" valueFormat="YYYY-MM-DD" placeholder="请选择日期" />
      <station-select-view v-model:value="params.start" />
      <station-select-view v-model:value="params.end" />
      <a-button type="primary" @click="handleQuery()">查找</a-button>
    </a-space>
  </p>
  <a-table :dataSource="dailyTrainTickets"
           :columns="columns"
           :pagination="pagination"
           @change="handleTableChange"
           :loading="loading">
    <template #bodyCell="{ column, record }">
      <template v-if="column.key === 'operation'">
        <a-button style="color: blue" @click="toOrder(record)"><ShoppingCartOutlined />购票</a-button>
      </template>
      <template v-else-if="column.dataIndex === 'station'">
        {{record.start}}<br/>
        {{record.end}}
      </template>
      <template v-else-if="column.dataIndex === 'time'">
        {{record.startTime}}<br/>
        {{record.endTime}}
      </template>
      <template v-else-if="column.dataIndex === 'duration'">
        {{calDuration(record.startTime, record.endTime)}}<br/>
        <div v-if="record.startTime.replaceAll(':', '') >= record.endTime.replaceAll(':', '')">
          次日到达
        </div>
        <div v-else>
          当日到达
        </div>
      </template>
      <template v-else-if="column.dataIndex === 'ydz'">
        <div v-if="record.ydz >= 0">
          {{record.ydz}}<br/>
          {{record.ydzPrice}}￥
        </div>
        <div v-else>
          --
        </div>
      </template>
      <template v-else-if="column.dataIndex === 'edz'">
        <div v-if="record.edz >= 0">
          {{record.edz}}<br/>
          {{record.edzPrice}}￥
        </div>
        <div v-else>
          --
        </div>
      </template>
      <template v-else-if="column.dataIndex === 'rw'">
        <div v-if="record.rw >= 0">
          {{record.rw}}<br/>
          {{record.rwPrice}}￥
        </div>
        <div v-else>
          --
        </div>
      </template>
      <template v-else-if="column.dataIndex === 'yw'">
        <div v-if="record.yw >= 0">
          {{record.yw}}<br/>
          {{record.ywPrice}}￥
        </div>
        <div v-else>
          --
        </div>
      </template>
    </template>
  </a-table>
</template>
<script>
import {defineComponent, onMounted, ref} from "vue";
import axios from "axios";
import {notification} from "ant-design-vue";
import StationSelectView from "@/components/station-select.vue";
import dayjs from "dayjs";
import router from "@/router";

export default defineComponent({
  components: {StationSelectView},
  setup() {
    const visible = ref(false);
    let dailyTrainTicket = ref({
      id: undefined,
      date: undefined,
      trainCode: undefined,
      start: undefined,
      startPinyin: undefined,
      startTime: undefined,
      startIndex: undefined,
      end: undefined,
      endPinyin: undefined,
      endTime: undefined,
      endIndex: undefined,
      ydz: undefined,
      ydzPrice: undefined,
      edz: undefined,
      edzPrice: undefined,
      rw: undefined,
      rwPrice: undefined,
      yw: undefined,
      ywPrice: undefined,
      createTime: undefined,
      updateTime: undefined,
    });

    const dailyTrainTickets = ref([]);
    const columns = [
      {
        title: '日期',
        dataIndex: 'date',
        key: 'date',
      },
      {
        title: '车次编号',
        dataIndex: 'trainCode',
        key: 'trainCode',
      },
      {
        title: '车站',
        dataIndex: 'station',
      },
      {
        title: '时间',
        dataIndex: 'time',
      },
      {
        title: '历时',
        dataIndex: 'duration',
      },

      // {
      //   title: '出发站',
      //   dataIndex: 'start',
      //   key: 'start',
      // },
      // {
      //   title: '出发站拼音',
      //   dataIndex: 'startPinyin',
      //   key: 'startPinyin',
      // },
      // {
      //   title: '出发时间',
      //   dataIndex: 'startTime',
      //   key: 'startTime',
      // },
      // {
      //   title: '出发站序',
      //   dataIndex: 'startIndex',
      //   key: 'startIndex',
      // },
      // {
      //   title: '到达站',
      //   dataIndex: 'end',
      //   key: 'end',
      // },
      // {
      //   title: '到达站拼音',
      //   dataIndex: 'endPinyin',
      //   key: 'endPinyin',
      // },
      // {
      //   title: '到站时间',
      //   dataIndex: 'endTime',
      //   key: 'endTime',
      // },
      // {
      //   title: '到站站序',
      //   dataIndex: 'endIndex',
      //   key: 'endIndex',
      // },
      {
        title: '一等座余票',
        dataIndex: 'ydz',
        key: 'ydz',
      },
      // {
      //   title: '一等座票价',
      //   dataIndex: 'ydzPrice',
      //   key: 'ydzPrice',
      // },
      {
        title: '二等座余票',
        dataIndex: 'edz',
        key: 'edz',
      },
      // {
      //   title: '二等座票价',
      //   dataIndex: 'edzPrice',
      //   key: 'edzPrice',
      // },
      {
        title: '软卧余票',
        dataIndex: 'rw',
        key: 'rw',
      },
      // {
      //   title: '软卧票价',
      //   dataIndex: 'rwPrice',
      //   key: 'rwPrice',
      // },
      {
        title: '硬卧余票',
        dataIndex: 'yw',
        key: 'yw',
      },
      // {
      //   title: '硬卧票价',
      //   dataIndex: 'ywPrice',
      //   key: 'ywPrice',
      // },
      {
        title: '操作',
        dataIndex: 'operation',
        key: 'operation',
      },
    ];
    const pagination = ref({
      total: 0,
      current: 1,
      pageSize: 10,
    })

    let loading = ref(false);

    const params = ref({
      date: null,
      // trainCode: null,
      start: null,
      end: null
    });
    const handleQuery = (param) => {
      if(Tool.isEmpty(params.value.date)){
        notification.error({description: "请输入日期"});
        return;
      }
      if(Tool.isEmpty(params.value.start)){
        notification.error({description: "请输入始发站"});
        return;
      }
      if(Tool.isEmpty(params.value.end)){
        notification.error({description: "请输入终点站"});
        return;
      }

      if (!param) {
        param = {
          page: 1,
          size: pagination.value.pageSize
        }
      }
      loading.value = true;
      axios.get("/business/daily-train-ticket/query-list", {
            params: {
              page: param.page,
              size: param.size,
              date: params.value.date,
              // trainCode: params.value.trainCode,
              start: params.value.start,
              end: params.value.end
            }
          }
      ).then((response) => {
        let data = response.data;
        if (data.success) {
          loading.value = false;
          dailyTrainTickets.value = data.content.list;
          pagination.value.current = param.page;//如果不加这一行，点击第二页之后，虽然列表修改了但是页码还在第一页
          pagination.value.total = data.content.total;
        } else {
          notification.error({description: data.message});
        }
      })
    }

    const handleTableChange = (page) => {
      // console.log(pagination);
      pagination.value.pageSize = page.pageSize;
      handleQuery({
        page: page.current,
        size: page.pageSize
      })
    }

    const calDuration = (startTime, endTime) => {
      let diff = dayjs(endTime, 'HH:mm:ss').diff(dayjs(startTime, 'HH:mm:ss'), 'seconds');
      return dayjs('00:00:00', 'HH:mm:ss').second(diff).format('HH:mm:ss');
    };

    const toOrder = (record) => {
      dailyTrainTicket.value = Tool.copy(record);
      SessionStorage.set("dailyTrainTicket", dailyTrainTicket.value);
      router.push("/order");
    }

    onMounted(() => {
      // handleQuery({
      //   page: 1,
      //   size: pagination.value.pageSize
      // });
    });
    return {
      pagination,
      dailyTrainTickets,
      columns,
      visible,
      dailyTrainTicket,
      loading,
      handleQuery,
      handleTableChange,
      params,
      calDuration,
      toOrder
    };
  },
});
</script>
<style>

</style>