<template>
<!--  <h1>乘客界面</h1>-->
  <p>
    <a-space style="width: 100%">
    <a-button type="primary" @click="showModal">
      <user-add-outlined/>
      新增乘客
    </a-button>
    <a-button type="primary" @click="handleQuery()">
      <sync-outlined/>
      刷新
    </a-button>
    </a-space>
  </p>
  <a-table :dataSource="passengers" :columns="columns" :pagination="pagination" @change="handleTableChange"
           :loading="loading"/>
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
          <a-select-option value="1">成人</a-select-option>
          <a-select-option value="2">儿童</a-select-option>
          <a-select-option value="3">学生</a-select-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script>
import {defineComponent, onMounted, reactive, ref} from "vue";
import axios from "axios";
import {notification} from "ant-design-vue";

export default defineComponent({
  setup() {
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

    const passengers= ref([]);
    const columns= [
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
      },
    ];
    const pagination = reactive({
      total: 0,
      current: 1,
      pageSize: 6,
    })

    let loading=ref(false);

    const showModal = () => {
      visible.value = true;
    };
    const handleOk = () => {
      axios.post("/member/passenger/save", passenger.value).then((response) => {
        let data = response.data;
        if(data.success){
          notification.success({description: "添加成功!"});
          visible.value = false;
          passenger.value="";
          handleQuery({
            page: pagination.current,
            size: pagination.pageSize
          })
        }else{
          notification.error({description: data.message});
        }
      })
    };

    const handleQuery = (param) => {
      if(!param){
        param={
          page: 1,
          size: pagination.pageSize
        }
      }
      loading.value=true;
      axios.get("/member/passenger/queryList", {
            params: {
              page: param.page,
              size: param.size
            }
          }
      ).then((response) => {
        let data = response.data;
        if (data.success) {
          loading.value=false;
          passengers.value = data.content.list;
          pagination.current = param.page;//如果不加这一行，点击第二页之后，虽然列表修改了但是页码还在第一页
          pagination.total = data.content.total;
        } else {
          notification.error({description: data.message});
        }
      })
    }

    const handleTableChange = (pagination) => {
      // console.log(pagination);
      handleQuery({
        page: pagination.current,
        size: pagination.pageSize
      })
    }

    onMounted(() => {
      handleQuery({
        page: 1,
        size: pagination.pageSize
      });
    });
    return {
      pagination,
      passengers,
      columns,
      visible,
      showModal,
      handleOk,
      passenger,
      handleTableChange,
      handleQuery,
      loading,
    };
  },
});
</script>
<style>

</style>