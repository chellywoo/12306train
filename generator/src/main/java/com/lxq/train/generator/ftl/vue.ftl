<template>
  <p>
    <a-space>
      <#if !readOnly><a-button type="primary" @click="OnAdd"><user-add-outlined/>新增乘客</a-button></#if>
      <a-button type="primary" @click="handleQuery()"><sync-outlined/>刷新</a-button>
    </a-space>
  </p>
  <a-table :dataSource="${domain}s"
           :columns="columns"
           :pagination="pagination"
           @change="handleTableChange"
           :loading="loading">
    <template #bodyCell="{ column,record }">
      <template v-if="column.key === 'operation'">
        <#if !readOnly>
        <a-space>
          <a style="color: blueviolet" @click="OnEdit(record)"><edit-outlined/>编辑</a>
          <a-popconfirm title="删除后不可恢复，确定删除?" ok-text="确认" cancel-text="取消" @confirm="OnDelete(record)">
            <delete-outlined style="color: red"/>
            <a style="color: red">删除</a>
          </a-popconfirm>
        </a-space>
        </#if>
      </template>
      <#list fieldList as field>
        <#if field.enums>
      <template v-else-if="column.key === '${field.nameHump}'">
        <span v-for="item in ${field.enumsConst}_ARRAY" :key="item.key">
          <span v-if="item.key === record.${field.nameHump}">
            {{ item.value }}
          </span>
        </span>
      </template>
        </#if>
      </#list>
    </template>
  </a-table>
  <#if !readOnly>
  <a-modal v-model:visible="visible" title="${tableNameCn}" @ok="handleOk"
           ok-text="确认" cancel-text="取消">
    <a-form :model="${domain}" :label-col="{span: 4}" :wrapper-col="{ span: 20 }">
      <#list fieldList as field>
        <#if field.name!="id" && field.nameHump!="createTime" && field.nameHump!="updateTime">
      <a-form-item label="${field.nameCn}">
        <#if field.enums>
        <a-select v-model:value="${domain}.${field.nameHump}">
          <a-select-option v-for="item in ${field.enumsConst}_ARRAY" :key="item.key" :value="item.key">
            {{item.value}}
          </a-select-option>
        </a-select>
        <#elseif field.javaType=='Date'>
          <#if field.type=='time'>
        <a-time-picker v-model:value="${domain}.${field.nameHump}" valueFormat="HH:mm:ss" placeholder="请选择时间" />
          <#elseif field.type=='date'>
        <a-date-picker v-model:value="${domain}.${field.nameHump}" valueFormat="YYYY-MM-DD" placeholder="请选择日期" />
          <#else>
        <a-date-picker v-model:value="${domain}.${field.nameHump}" valueFormat="YYYY-MM-DD HH:mm:ss" show-time placeholder="请选择日期" />
          </#if>
        <#else>
        <a-input v-model:value="${domain}.${field.nameHump}" />
        </#if>
      </a-form-item>

        </#if>
      </#list>
    </a-form>
  </a-modal>
  </#if>
</template>

<script>
import { defineComponent, ref, onMounted } from 'vue';
import {notification} from "ant-design-vue";
import axios from "axios";

export default defineComponent({
  name: "${do_main}-view",
  setup() {
    <#list fieldList as field>
    <#if field.enums>
    const ${field.enumsConst}_ARRAY = window.${field.enumsConst};
    </#if>
    </#list>
    const visible = ref(false);
    let ${domain} = ref({
      <#list fieldList as field>
      ${field.nameHump}: undefined,
      </#list>
    });
    const ${domain}s = ref([]);
    let loading = ref(false);
    const columns = [
    <#list fieldList as field>
      <#if field.name!="id" && field.nameHump!="createTime" && field.nameHump!="updateTime">
      {
      title: '${field.nameCn}',
      dataIndex: '${field.nameHump}',
      key: '${field.nameHump}',
    },
      </#if>
    </#list>
    <#if !readOnly>
    {
      title: '操作',
      dataIndex: 'operation',
      key: 'operation'
    }
    </#if>
    ];

    const pagination = ref({
      total: 0,
      current: 1,
      pageSize: 10,
    });

    <#if !readOnly>
    const OnAdd = () => {
      ${domain}.value = {};
      visible.value = true;
    };

    const OnEdit = (record) => {
      ${domain}.value = window.Tool.copy(record);
      visible.value = true;
    };

    const OnDelete = (record) => {
      axios.delete("/${module}/${do_main}/delete/" + record.id).then((response) => {
        const data = response.data;
        if (data.success) {
          notification.success({description: "删除成功！"});
          handleQuery({
            page: pagination.value.current,
            size: pagination.value.pageSize,
          });
        } else {
          notification.error({description: data.message});
        }
      });
    };

    const handleOk = () => {
      axios.post("/${module}/${do_main}/save", ${domain}.value).then((response) => {
        let data = response.data;
        if (data.success) {
          notification.success({description: "保存成功！"});
          visible.value = false;
          handleQuery({
            page: pagination.value.current,
            size: pagination.value.pageSize
          });
        } else {
          notification.error({description: data.message});
        }
      });
    };
    </#if>

    const handleQuery = (param) => {
      if (!param) {
        param = {
          page: 1,
          size: pagination.value.pageSize
        };
      }
      loading.value = true;
      axios.get("/${module}/${do_main}/queryList", {
        params: {
          page: param.page,
          size: param.size
        }
      }).then((response) => {
        loading.value = false;
        let data = response.data;
        if (data.success) {
          ${domain}s.value = data.content.list;
          // 设置分页控件的值
          pagination.value.current = param.page;
          pagination.value.total = data.content.total;
        } else {
          notification.error({description: data.message});
        }
      });
    };

    const handleTableChange = (pagination) => {
      // console.log(pagination);
      handleQuery({
        page: pagination.value.current,
        size: pagination.value.pageSize
      })
    };

    onMounted(() => {
      handleQuery({
        page: 1,
        size: pagination.value.pageSize
      });
    });

    return {
      <#list fieldList as field>
      <#if field.enums>
      ${field.enumsConst}_ARRAY,
      </#if>
      </#list>
      pagination,
      ${domain}s,
      columns,
      visible,
      ${domain},
      handleTableChange,
      handleQuery,
      loading,
      <#if !readOnly>
      OnAdd,
      handleOk,
      OnEdit,
      OnDelete
      </#if>
    };
  },
});
</script>
