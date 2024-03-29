<template>
  <!--  <h1>乘客界面</h1>-->
  <p>
    <a-space style="width: 100%">
      
      <a-button type="primary" @click="handleQuery()"><sync-outlined/>刷新</a-button>
    </a-space>
  </p>
  <a-table :dataSource="confirmOrders"
           :columns="columns"
           :pagination="pagination"
           @change="handleTableChange"
           :loading="loading">
    <template #bodyCell="{ column,record }">
      <template v-if="column.key === 'operation'">
      </template>
      <template v-else-if="column.key === 'status'">
        <span v-for="item in CONFIRM_ORDER_STATUS_ARRAY" :key="item.code">
          <span v-if="item.code === record.status">
            {{ item.desc }}
          </span>
        </span>
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
    const CONFIRM_ORDER_STATUS_ARRAY = window.CONFIRM_ORDER_STATUS;
    const visible = ref(false);
    let confirmOrder = ref({
      id: undefined,
      memberId: undefined,
      date: undefined,
      trainCode: undefined,
      start: undefined,
      end: undefined,
      dailyTrainTicketId: undefined,
      tickets: undefined,
      status: undefined,
      createTime: undefined,
      updateTime: undefined,
    });

    const confirmOrders = ref([]);
    const columns = [
      {
        title: '用户id',
        dataIndex: 'memberId',
        key: 'memberId',
      },
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
        title: '到达站',
        dataIndex: 'end',
        key: 'end',
      },
      {
        title: '余票ID',
        dataIndex: 'dailyTrainTicketId',
        key: 'dailyTrainTicketId',
      },
      {
        title: '车票',
        dataIndex: 'tickets',
        key: 'tickets',
      },
      {
        title: '订单状态',
        dataIndex: 'status',
        key: 'status',
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
      axios.get("/business/admin/confirm-order/query-list", {
            params: {
              page: param.page,
              size: param.size
            }
          }
      ).then((response) => {
        let data = response.data;
        if (data.success) {
          loading.value = false;
          confirmOrders.value = data.content.list;
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
      CONFIRM_ORDER_STATUS_ARRAY,
      pagination,
      confirmOrders,
      columns,
      visible,
      confirmOrder,
      loading,
      handleQuery,
      handleTableChange,
    };
  },
});
</script>
<style>

</style>