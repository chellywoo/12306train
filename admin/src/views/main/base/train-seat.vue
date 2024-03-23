<template>
  <!--  <h1>乘客界面</h1>-->
  <p>
    <a-space style="width: 100%">
      <train-select-view v-model="params.trainCode" width="200px"/>
      <a-button type="primary" @click="handleQuery()">查找</a-button>
    </a-space>
  </p>
  <a-table :dataSource="trainSeats"
           :columns="columns"
           :pagination="pagination"
           @change="handleTableChange"
           :loading="loading">
    <template #bodyCell="{ column,record }">
      <template v-if="column.key === 'operation'">
      </template>
      <template v-else-if="column.key === 'col'">
        <span v-for="item in SEAT_COL_ARRAY" :key="item.code">
          <span v-if="item.code === record.col && item.type === record.seatType">
            {{ item.desc }}
          </span>
        </span>
      </template>
      <template v-else-if="column.key === 'seatType'">
        <span v-for="item in SEAT_TYPE_ARRAY" :key="item.code">
          <span v-if="item.code === record.seatType">
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
import TrainSelectView from "@/components/train-select.vue";

export default defineComponent({
  components: {TrainSelectView},
  setup() {
    const SEAT_COL_ARRAY = window.SEAT_COL;
    const SEAT_TYPE_ARRAY = window.SEAT_TYPE;
    const visible = ref(false);
    let trainSeat = ref({
      id: undefined,
      trainCode: undefined,
      carriageIndex: undefined,
      row: undefined,
      col: undefined,
      seatType: undefined,
      carriageSeatIndex: undefined,
      createTime: undefined,
      updateTime: undefined,
    });

    const trainSeats = ref([]);
    const columns = [
      {
        title: '车次编号',
        dataIndex: 'trainCode',
        key: 'trainCode',
      },
      {
        title: '厢号',
        dataIndex: 'carriageIndex',
        key: 'carriageIndex',
      },
      {
        title: '行号',
        dataIndex: 'row',
        key: 'row',
      },
      {
        title: '列号',
        dataIndex: 'col',
        key: 'col',
      },
      {
        title: '座位类型',
        dataIndex: 'seatType',
        key: 'seatType',
      },
      {
        title: '同车厢座序',
        dataIndex: 'carriageSeatIndex',
        key: 'carriageSeatIndex',
      },
    ];
    const pagination = ref({
      total: 0,
      current: 1,
      pageSize: 10,
    })

    let loading = ref(false);

    let params = ref({
      trainCode: null
    })

    const handleQuery = (param) => {
      if (!param) {
        param = {
          page: 1,
          size: pagination.value.pageSize
        }
      }
      loading.value = true;
      axios.get("/business/admin/train-seat/query-list", {
            params: {
              page: param.page,
              size: param.size,
              trainCode: params.value.trainCode
            }
          }
      ).then((response) => {
        let data = response.data;
        if (data.success) {
          loading.value = false;
          trainSeats.value = data.content.list;
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
      SEAT_COL_ARRAY,
      SEAT_TYPE_ARRAY,
      pagination,
      trainSeats,
      columns,
      visible,
      trainSeat,
      loading,
      handleQuery,
      handleTableChange,
      params
    };
  },
});
</script>
<style>

</style>