<template>
<!--  <h1>乘客界面</h1>-->
  <a-button type="primary" @click="showModal">新增乘客</a-button>
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
import {defineComponent, ref} from "vue";
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
    return {
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