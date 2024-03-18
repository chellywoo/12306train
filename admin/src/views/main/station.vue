<template>
  <!--  <h1>乘客界面</h1>-->
  <p>
    <a-space style="width: 100%">
      <a-button type="primary" @click="OnAdd"><plus-outlined />新增车站</a-button>
      <a-button type="primary" @click="handleQuery()"><sync-outlined/>刷新</a-button>
    </a-space>
  </p>
  <a-table :dataSource="stations"
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
    </template>
  </a-table>
  <a-modal v-model:visible="visible" title="车站" @ok="handleOk"
           ok-text="确认" cancel-text="取消">
    <a-form
        :model="station"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }">
      <a-form-item label="站名">
        <a-input v-model:value="station.name"/>
      </a-form-item>
      <a-form-item label="站名拼音">
        <a-input v-model:value="station.namePinyin"/>
      </a-form-item>
      <a-form-item label="站名拼音首字母">
        <a-input v-model:value="station.namePy"/>
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
    const visible = ref(false);
    let station = ref({
      id: undefined,
      name: undefined,
      namePinyin: undefined,
      namePy: undefined,
      createTime: undefined,
      updateTime: undefined,
    });

    const stations = ref([]);
    const columns = [
      {
        title: '站名',
        dataIndex: 'name',
        key: 'name',
      },
      {
        title: '站名拼音',
        dataIndex: 'namePinyin',
        key: 'namePinyin',
      },
      {
        title: '站名拼音首字母',
        dataIndex: 'namePy',
        key: 'namePy',
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
      station.value = {};
      visible.value = true;
    };

    const OnEdit = (record) => {
      station.value = window.Tool.copy(record);
      visible.value = true;
    }

    const OnDelete = (record) => {
      axios.delete("/business/admin/station/delete/" + record.id).then((response) => {
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
      axios.post("/business/admin/station/save", station.value).then((response) => {
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
      axios.get("/business/admin/station/query-list", {
            params: {
              page: param.page,
              size: param.size
            }
          }
      ).then((response) => {
        let data = response.data;
        if (data.success) {
          loading.value = false;
          stations.value = data.content.list;
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
      pagination,
      stations,
      columns,
      visible,
      station,
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