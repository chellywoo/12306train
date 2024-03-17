<template>
  <!--  <h1>乘客界面</h1>-->
  <p>
    <a-space style="width: 100%">
      <a-button type="primary" @click="OnAdd"><user-add-outlined/>新增乘客</a-button>
      <a-button type="primary" @click="handleQuery()"><sync-outlined/>刷新</a-button>
    </a-space>
  </p>
  <a-table :dataSource="passengers"
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
      <template v-else-if="column.key === 'type'">
        <span v-for="item in PASSENGER_TYPE_ARRAY" :key="item.key">
          <span v-if="item.key === record.type">
            {{ item.value }}
          </span>
        </span>
      </template>
    </template>
  </a-table>

  <a-modal v-model:visible="visible" title="乘客" @ok="handleOk"
           ok-text="确认" cancel-text="取消">
    <a-form
        :model="passenger"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }">
      <a-form-item label="姓名">
        <a-input v-model:value="passenger.name"/>
      </a-form-item>

      <a-form-item label="身份证号">
        <a-input v-model:value="passenger.idCard"/>
      </a-form-item>

      <a-form-item label="乘客类型">
        <a-select v-model:value="passenger.type">
          <a-select-option v-for="item in PASSENGER_TYPE_ARRAY" :key="item.key" :value="item.key">
            {{ item.value }}
          </a-select-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script>
import {defineComponent, onMounted, ref} from "vue";
import axios from "axios";
import {notification} from "ant-design-vue";

export default defineComponent({
  setup() {
    const PASSENGER_TYPE_ARRAY = window.PASSENGER_TYPE;
    const visible = ref(false);
    let passenger = ref({
      id: undefined,
      memberId: undefined,
      name: undefined,
      idCard: undefined,
      type: undefined,
      createTime: undefined,
      updateTime: undefined,
    });

    const passengers = ref([]);
    const columns = [
      {
        title: '姓名',
        dataIndex: 'name',
        key: 'name',
      },
      {
        title: '身份证',
        dataIndex: 'idCard',
        key: 'idCard',
      },
      {
        title: '乘客类型',
        dataIndex: 'type',
        key: 'type',
      }, {
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
      passenger.value = {};
      visible.value = true;
    };

    const OnEdit = (record) => {
      passenger.value = window.Tool.copy(record);
      visible.value = true;
    }

    const OnDelete = (record) => {
      axios.delete("/member/passenger/delete/" + record.id).then((response) => {
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
      axios.post("/member/passenger/save", passenger.value).then((response) => {
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
      axios.get("/member/passenger/queryList", {
            params: {
              page: param.page,
              size: param.size
            }
          }
      ).then((response) => {
        let data = response.data;
        if (data.success) {
          loading.value = false;
          passengers.value = data.content.list;
          pagination.value.current = param.page;//如果不加这一行，点击第二页之后，虽然列表修改了但是页码还在第一页
          pagination.value.total = data.content.total;
        } else {
          notification.error({description: data.message});
        }
      })
    }

    const handleTableChange = (pagination) => {
      // console.log(pagination);
      handleQuery({
        page: pagination.value.current,
        size: pagination.value.pageSize
      })
    }

    onMounted(() => {
      handleQuery({
        page: 1,
        size: pagination.value.pageSize
      });
    });
    return {
      PASSENGER_TYPE_ARRAY,
      pagination,
      passengers,
      columns,
      visible,
      OnAdd,
      handleOk,
      passenger,
      handleTableChange,
      handleQuery,
      loading,
      OnEdit,
      OnDelete,

    };
  },
});
</script>
<style>

</style>