<template>
  <!--  <h1>乘客界面</h1>-->
  <p>
    <a-space style="width: 100%">
      <a-button type="primary" @click="OnAdd"><plus-outlined />新增</a-button>
      <a-button type="primary" @click="handleQuery()"><sync-outlined/>刷新</a-button>
    </a-space>
  </p>
  <a-table :dataSource="trainSeats"
           :columns="columns"
           :pagination="pagination"
           @change="handleTableChange"
           :loading="loading">
    <template #bodyCell="{ column,record }">
      <template v-if="column.key === 'operation'">
        <a-space>
          <a style="color: blueviolet" @click="OnEdit(record)"><edit-outlined/>编辑</a>
          <a-popconfirm title="删除后不可恢复，确定删除?" ok-text="确认" cancel-text="取消" @confirm="OnDelete(record)">
            <delete-outlined style="color: red"/>
            <a style="color: red">删除</a>
          </a-popconfirm>
        </a-space>
      </template>
      <template v-else-if="column.key === 'col'">
        <span v-for="item in SEAT_COL_ARRAY" :key="item.code">
          <span v-if="item.code === record.col">
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
  <a-modal v-model:visible="visible" title="座位" @ok="handleOk"
           ok-text="确认" cancel-text="取消">
    <a-form
        :model="trainSeat"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }">
      <a-form-item label="车次编号">
        <train-select-view v-model="trainSeat.trainCode"/>
      </a-form-item>
      <a-form-item label="厢序">
        <a-input v-model:value="trainSeat.carriageIndex"/>
      </a-form-item>
      <a-form-item label="排号">
        <a-input v-model:value="trainSeat.row"/>
      </a-form-item>
      <a-form-item label="列号">
        <a-select v-model:value="trainSeat.col">
          <a-select-option v-for="item in SEAT_COL_ARRAY" :key="item.code" :value="item.code">
            {{ item.desc }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="座位类型">
        <a-select v-model:value="trainSeat.seatType">
          <a-select-option v-for="item in SEAT_TYPE_ARRAY" :key="item.code" :value="item.code">
            {{ item.desc }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="同车厢座序">
        <a-input v-model:value="trainSeat.carriageSeatIndex"/>
      </a-form-item>
    </a-form>
  </a-modal>
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
        title: '厢序',
        dataIndex: 'carriageIndex',
        key: 'carriageIndex',
      },
      {
        title: '排号',
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
      {
        title: "操作",
        dataIndex: 'operation',
        key: 'operation'
      }
    ];
    const pagination = ref({
      total: 0,
      current: 1,
      pageSize: 6,
    })

    let loading = ref(false);

    const OnAdd = () => {
      trainSeat.value = {};
      visible.value = true;
    };

    const OnEdit = (record) => {
      trainSeat.value = window.Tool.copy(record);
      visible.value = true;
    }

    const OnDelete = (record) => {
      axios.delete("/business/admin/train-seat/delete/" + record.id).then((response) => {
        let data = response.data;
        if (data.success) {
          notification.success({description: "删除成功!"});
          handleQuery({
            page: pagination.value.current,
            size: pagination.value.pageSize
          })
        } else {
          notification.error({description: data.message});
        }
      })
    }
    const handleOk = () => {
      axios.post("/business/admin/train-seat/save", trainSeat.value).then((response) => {
        let data = response.data;
        if (data.success) {
          notification.success({description: "保存成功!"});
          visible.value = false;
          handleQuery({
            page: pagination.value.current,
            size: pagination.value.pageSize
          })
        } else {
          notification.error({description: data.message});
        }
      })
    };

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
              size: param.size
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

    const handleTableChange = (clickPage) => {
      // console.log(pagination);
      handleQuery({
        page: clickPage.current,
        size: clickPage.pageSize
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
      OnAdd,
      OnEdit,
      OnDelete,
      handleOk,
    };
  },
});
</script>
<style>

</style>