<template>
  <!--  <h1>乘客界面</h1>-->
  <p>
    <a-space style="width: 100%">
      <a-date-picker v-model:value="params.date" valueFormat="YYYY-MM-DD" placeholder="请选择日期" />
      <train-select-view v-model="params.code" width="200px"/>
      <a-button type="primary" @click="handleQuery()">查找</a-button>
      <a-button type="primary" @click="OnAdd"><plus-outlined />新增</a-button>
      <a-button type="danger" @click="OnClickGenDaily">手动生成车次信息</a-button>
    </a-space>
  </p>
  <a-table :dataSource="dailyTrains"
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
  <a-modal v-model:visible="visible" title="每日车次" @ok="handleOk"
           ok-text="确认" cancel-text="取消">
    <a-form
        :model="dailyTrain"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }">
      <a-form-item label="日期">
        <a-date-picker v-model:value="dailyTrain.date" valueFormat="YYYY-MM-DD" placeholder="请选择日期" />
      </a-form-item>
      <a-form-item label="车次编号">
        <train-select-view v-model="dailyTrain.code" @change="OnChange"/>
      </a-form-item>
      <a-form-item label="车次类型">
        <a-select v-model:value="dailyTrain.type">
          <a-select-option v-for="item in TRAIN_TYPE_ARRAY" :key="item.code" :value="item.code">
            {{ item.desc }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="始发站">
        <a-input v-model:value="dailyTrain.start"/>
      </a-form-item>
      <a-form-item label="始发站拼音">
        <a-input v-model:value="dailyTrain.startPinyin"/>
      </a-form-item>
      <a-form-item label="出发时间">
        <a-time-picker v-model:value="dailyTrain.startTime" valueFormat="HH:mm:ss" placeholder="请选择时间" />
      </a-form-item>
      <a-form-item label="终点站">
        <a-input v-model:value="dailyTrain.end"/>
      </a-form-item>
      <a-form-item label="终点站拼音">
        <a-input v-model:value="dailyTrain.endPinyin"/>
      </a-form-item>
      <a-form-item label="到站时间">
        <a-time-picker v-model:value="dailyTrain.endTime" valueFormat="HH:mm:ss" placeholder="请选择时间" />
      </a-form-item>
    </a-form>
  </a-modal>
  <a-modal v-model:visible="genDailyVisible" title="生成车次" @ok="handleGenDailyOk"
                     :confirm-loading="genDailyLoading" ok-text="确认" cancel-text="取消">
  <a-form :model="genDaily" :label-col="{span: 4}" :wrapper-col="{ span: 20 }">
    <a-form-item label="日期">
      <a-date-picker v-model:value="genDaily.date" placeholder="请选择日期"/>
    </a-form-item>
  </a-form>
</a-modal>
</template>
<script>
import {defineComponent, onMounted, ref} from "vue";
import axios from "axios";
import {notification} from "ant-design-vue";
import TrainSelectView from "@/components/train-select.vue";
import dayjs from "dayjs";

export default defineComponent({
  components: {TrainSelectView},
  setup() {
    const TRAIN_TYPE_ARRAY = window.TRAIN_TYPE;
    const visible = ref(false);
    let dailyTrain = ref({
      id: undefined,
      date: undefined,
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

    const dailyTrains = ref([]);
    const columns = [
      {
        title: '日期',
        dataIndex: 'date',
        key: 'date',
      },
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
      pageSize: 10,
    })

    let loading = ref(false);

    let params = ref({
      code: null,
      date: null
    });

    const genDaily = ref({
      date: null
    });
    const genDailyVisible = ref(false);
    const genDailyLoading = ref(false);

    const OnAdd = () => {
      dailyTrain.value = {};
      visible.value = true;
    };

    const OnEdit = (record) => {
      dailyTrain.value = window.Tool.copy(record);
      visible.value = true;
    }

    const OnDelete = (record) => {
      axios.delete("/business/admin/daily-train/delete/" + record.id).then((response) => {
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
      axios.post("/business/admin/daily-train/save", dailyTrain.value).then((response) => {
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
      axios.get("/business/admin/daily-train/query-list", {
            params: {
              page: param.page,
              size: param.size,
              code: params.value.code,
              date: params.value.date
            }
          }
      ).then((response) => {
        let data = response.data;
        if (data.success) {
          loading.value = false;
          dailyTrains.value = data.content.list;
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

    const OnChange = (train) => {
      console.log("下拉表单选择车次",train);
      let t = Tool.copy(train);
      delete t.id;
      // dailyTrain.value=t;
      dailyTrain.value = Object.assign(dailyTrain.value, t);
    }

    const OnClickGenDaily = () => {
      genDailyVisible.value = true;
    };

    const handleGenDailyOk = () => {
      let date = dayjs(genDaily.value.date).format("YYYY-MM-DD");
      genDailyLoading.value = true;
      axios.get("/business/admin/daily-train/generate-daily/" + date).then((response) => {
        genDailyLoading.value = false;
        let data = response.data;
        if (data.success) {
          notification.success({description: "生成成功！"});
          genDailyVisible.value = false;
          handleQuery({
            page: pagination.value.current,
            size: pagination.value.pageSize
          });
        } else {
          notification.error({description: data.message});
        }
      });
    };

    onMounted(() => {
      handleQuery({
        page: 1,
        size: pagination.value.pageSize
      });
    });
    return {
      TRAIN_TYPE_ARRAY,
      pagination,
      dailyTrains,
      columns,
      visible,
      dailyTrain,
      loading,
      handleQuery,
      handleTableChange,
      OnAdd,
      OnEdit,
      OnDelete,
      handleOk,
      OnChange,
      params,
      genDaily,
      genDailyVisible,
      genDailyLoading,
      OnClickGenDaily,
      handleGenDailyOk
    };
  },
});
</script>
<style>

</style>