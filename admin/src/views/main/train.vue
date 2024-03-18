<template>
  <!--  <h1>乘客界面</h1>-->
  <p>
    <a-space style="width: 100%">
      <a-button type="primary" @click="OnAdd"><plus-outlined />新增</a-button>
      <a-button type="primary" @click="handleQuery()"><sync-outlined/>刷新</a-button>
    </a-space>
  </p>
  <a-table :dataSource="trains"
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
        <span v-for="item in TRAIN_TYPE_ARRAY" :key="item.code">
          <span v-if="item.code === record.type">
            {{ item.desc }}
          </span>
        </span>
      </template>
    </template>
  </a-table>
  <a-modal v-model:visible="visible" title="车次" @ok="handleOk"
           ok-text="确认" cancel-text="取消">
    <a-form
        :model="train"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }">
      <a-form-item label="车次编号">
        <a-input v-model:value="train.code"/>
      </a-form-item>
      <a-form-item label="车次类型">
        <a-select v-model:value="train.type">
          <a-select-option v-for="item in TRAIN_TYPE_ARRAY" :key="item.code" :value="item.code">
            {{ item.desc }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="始发站">
        <station-select-view v-model:value="train.start"/>
      </a-form-item>
      <a-form-item label="始发站拼音">
        <a-input v-model:value="train.startPinyin" disabled/>
      </a-form-item>
      <a-form-item label="出发时间">
        <a-time-picker v-model:value="train.startTime" valueFormat="HH:mm:ss" placeholder="请选择时间" />
      </a-form-item>
      <a-form-item label="终点站">
        <station-select-view v-model:value="train.end"/>
      </a-form-item>
      <a-form-item label="终点站拼音">
        <a-input v-model:value="train.endPinyin" disabled/>
      </a-form-item>
      <a-form-item label="到站时间">
        <a-time-picker v-model:value="train.endTime" valueFormat="HH:mm:ss" placeholder="请选择时间" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script>
import {defineComponent, onMounted, ref, watch} from "vue";
import axios from "axios";
import {notification} from "ant-design-vue";
import {pinyin} from "pinyin-pro";
import StationSelectView from "@/components/station-select.vue";

export default defineComponent({
  components: {StationSelectView},
  setup() {
    const TRAIN_TYPE_ARRAY = window.TRAIN_TYPE;
    const visible = ref(false);
    let train = ref({
      id: undefined,
      code: undefined,
      type: undefined,
      start: undefined,
      startPinyin: undefined,
      startTime: undefined,
      end: undefined,
      endPinyin: undefined,
      endTime: undefined,
      createTime: undefined,
      updateTime: undefined,
    });

    const trains = ref([]);
    const columns = [
      {
        title: '车次编号',
        dataIndex: 'code',
        key: 'code',
      },
      {
        title: '车次类型',
        dataIndex: 'type',
        key: 'type',
      },
      {
        title: '始发站',
        dataIndex: 'start',
        key: 'start',
      },
      {
        title: '始发站拼音',
        dataIndex: 'startPinyin',
        key: 'startPinyin',
      },
      {
        title: '出发时间',
        dataIndex: 'startTime',
        key: 'startTime',
      },
      {
        title: '终点站',
        dataIndex: 'end',
        key: 'end',
      },
      {
        title: '终点站拼音',
        dataIndex: 'endPinyin',
        key: 'endPinyin',
      },
      {
        title: '到站时间',
        dataIndex: 'endTime',
        key: 'endTime',
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

    watch(() => train.value.start, ()=>{
      if (Tool.isNotEmpty(train.value.start)) {
        train.value.startPinyin = pinyin(train.value.start, { toneType: 'none'}).replaceAll(" ", "");
      } else {
        train.value.startPinyin = "";
      }
    }, {immediate: true});

    watch(() => train.value.end, ()=>{
      if (Tool.isNotEmpty(train.value.end)) {
        train.value.endPinyin = pinyin(train.value.end, { toneType: 'none'}).replaceAll(" ", "");
      } else {
        train.value.endPinyin = "";
      }
    }, {immediate: true});

    const OnAdd = () => {
      train.value = {};
      visible.value = true;
    };

    const OnEdit = (record) => {
      train.value = window.Tool.copy(record);
      visible.value = true;
    }

    const OnDelete = (record) => {
      axios.delete("/business/admin/train/delete/" + record.id).then((response) => {
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
      axios.post("/business/admin/train/save", train.value).then((response) => {
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
      axios.get("/business/admin/train/query-list", {
            params: {
              page: param.page,
              size: param.size
            }
          }
      ).then((response) => {
        let data = response.data;
        if (data.success) {
          loading.value = false;
          trains.value = data.content.list;
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
      TRAIN_TYPE_ARRAY,
      pagination,
      trains,
      columns,
      visible,
      train,
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