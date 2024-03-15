<template>
<!--  <h1>乘客界面</h1>-->
  <p> <a-button type="primary" @click="showModal">新增乘客</a-button></p>
  <a-table :dataSource="passengers" :columns="columns" :pagination="pagination" />
  <a-modal v-model:visible="visible" title="乘客" @ok="handleOk"
           ok-text="确认" cancel-text="取消">
    <a-form
        :model="passenger"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }">
      <a-form-item
          label="姓名"
          :rules="[{ required: true, message: '请输入姓名!' }]"
      >
        <a-input v-model:value="passenger.name" />
      </a-form-item>

      <a-form-item
          label="身份证号"
          :rules="[{ required: true, message: '请输入身份证号!' }]"
      >
        <a-input v-model:value="passenger.idCard" />
      </a-form-item>

      <a-form-item
          label="乘客类型"
          :rules="[{ required: true, message: '请选择乘客类型!' }]"
      >
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
    const showModal = () => {
      visible.value = true;
    };
    const handleOk = () => {
      axios.post("/member/passenger/save", passenger.value).then((response) => {
        let data = response.data;
        if(data.success){
          notification.success({description: "添加成功!"});
          visible.value = false;
        }else{
          notification.error({description: data.message});
        }
      })
    };
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
      pageSize: 2,
    })

    const handleQuery = (param) => {
      axios.get("/member/passenger/queryList", {
            params: {
              page: param.page,
              size: param.size
            }
          }
      ).then((response) => {
        let data = response.data;
        if (data.success) {
          passengers.value = data.content.list;
          pagination.total = data.content.total;
        } else {
          notification.error({description: data.message});
        }
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
    };
  },
});
</script>
<style>

</style>