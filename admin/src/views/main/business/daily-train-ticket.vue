<template>
  <!--  <h1>乘客界面</h1>-->
  <p>
    <a-space style="width: 100%">
      
      <a-button type="primary" @click="handleQuery()"><sync-outlined/>刷新</a-button>
    </a-space>
  </p>
  <a-table :dataSource="dailyTrainTickets"
           :columns="columns"
           :pagination="pagination"
           @change="handleTableChange"
           :loading="loading">
    <template #bodyCell="{ column }">
      <template v-if="column.key === 'operation'">
      </template>
    </template>
  </a-table>
</template>
<script>
import {defineComponent, onMounted, ref} from "vue";
import axios from "axios";
import {notification} from "ant-design-vue";

export default defineComponent({
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
        title: '出发站',
        dataIndex: 'start',
        key: 'start',
      },
      {
        title: '出发站拼音',
        dataIndex: 'startPinyin',
        key: 'startPinyin',
      },
      {
        title: '出发时间',
        dataIndex: 'startTime',
        key: 'startTime',
      },
      {
        title: '出发站序',
        dataIndex: 'startIndex',
        key: 'startIndex',
      },
      {
        title: '到达站',
        dataIndex: 'end',
        key: 'end',
      },
      {
        title: '到达站拼音',
        dataIndex: 'endPinyin',
        key: 'endPinyin',
      },
      {
        title: '到站时间',
        dataIndex: 'endTime',
        key: 'endTime',
      },
      {
        title: '到站站序',
        dataIndex: 'endIndex',
        key: 'endIndex',
      },
      {
        title: '一等座余票',
        dataIndex: 'ydz',
        key: 'ydz',
      },
      {
        title: '一等座票价',
        dataIndex: 'ydzPrice',
        key: 'ydzPrice',
      },
      {
        title: '二等座余票',
        dataIndex: 'edz',
        key: 'edz',
      },
      {
        title: '二等座票价',
        dataIndex: 'edzPrice',
        key: 'edzPrice',
      },
      {
        title: '软卧余票',
        dataIndex: 'rw',
        key: 'rw',
      },
      {
        title: '软卧票价',
        dataIndex: 'rwPrice',
        key: 'rwPrice',
      },
      {
        title: '硬卧余票',
        dataIndex: 'yw',
        key: 'yw',
      },
      {
        title: '硬卧票价',
        dataIndex: 'ywPrice',
        key: 'ywPrice',
      },
    ];
    const pagination = ref({
      total: 0,
      current: 1,
      pageSize: 10,
    })

    let loading = ref(false);


    const handleQuery = (param) => {
      if (!param) {
        param = {
          page: 1,
          size: pagination.value.pageSize
        }
      }
      loading.value = true;
      axios.get("/business/admin/daily-train-ticket/query-list", {
            params: {
              page: param.page,
              size: param.size
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

    onMounted(() => {
      handleQuery({
        page: 1,
        size: pagination.value.pageSize
      });
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
    };
  },
});
</script>
<style>

</style>