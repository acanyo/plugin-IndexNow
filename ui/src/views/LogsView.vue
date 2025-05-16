<script lang="ts" setup>
import {
  VCard,
  IconRefreshLine,
  VButton,
  VEmpty,
  VLoading,
  VPagination,
  VPageHeader,
  Toast,
  VSpace,
  VStatusDot, Dialog,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import { computed, ref, watch } from "vue";
import { formatDatetime } from "@/utils/date";
import { indexNowApiClient } from "@/api";
import type { HandsomeIndexNowLogs } from "@/api/generated/models/handsome-index-now-logs";
import type { HandsomeIndexNowLogsList } from "@/api/generated/models/handsome-index-now-logs-list";
import IconUpload from "~icons/ri/upload-cloud-line";
import IconParkHelp from '~icons/icon-park-outline/help';
import {axiosInstance} from "@halo-dev/api-client";

defineOptions({
  name: "LogsView",
});

const page = useRouteQuery<number>("page", 1, {
  transform: Number,
});
const size = useRouteQuery<number>("size", 20, {
  transform: Number,
});
const total = ref(0);

const {
  data: logs,
  isLoading,
  isFetching,
  refetch,
} = useQuery<HandsomeIndexNowLogsList>({
  queryKey: ["indexnow-logs", page, size],
  queryFn: async () => {
    const { data } = await indexNowApiClient.logs.listHandsomeIndexNowLogs({
      page: page.value,
      size: size.value,
      sort: ["metadata.creationTimestamp,desc"],
    });
    total.value = data.total;
    return data;
  },
  keepPreviousData: true,
  onSuccess: (data) => {
    size.value = data.size ?? 20;
    total.value = data.total ?? 0;
  }
});

const formattedLogs = computed(() => {
  if (!logs.value?.items) return [];
  return logs.value.items.map((item: HandsomeIndexNowLogs) => ({
    ...item,
    metadata: {
      ...item.metadata,
      timestamp: formatDatetime(item.metadata.creationTimestamp || "")
    }
  }));
});

function handleRefresh() {
  refetch();
}

const state = (message: string) => {
  if (message.includes('成功')) {
    return "success";
  }
  if (message.includes('失败')) {
    return "error";
  }
  return "warning";
};

const statusText = (message: string) => {
  if (message.includes('成功')) {
    return "成功";
  }
  if (message.includes('失败')) {
    return "失败";
  }
  return "未知";
};
function handleClear() {
  Dialog.warning({
    title: "清空记录",
    description: "确定要清空所有推送记录吗？此操作不可恢复。",
    async onConfirm() {
      try {
        await axiosInstance.delete("/apis/api.indexnow.lik.cc/v1alpha1/indexnow/clear")
          .then((res: any) => {
            Toast.success("清空成功");
          });
      } catch (e) {
        console.error("", e);
      }
      refetch();
    },
  });
}
function copyMessage(msg: string) {
  if (!msg) return;
  navigator.clipboard.writeText(msg).then(() => {
    Toast.success("消息已复制");
  }).catch(() => {
    Toast.error("复制失败");
  });
}
</script>

<template>
  <VPageHeader title="indexNow推送日志">
    <template #icon>
      <IconUpload class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton
          type="default"
          size="sm"
          class="!bg-gray-100 hover:!bg-gray-200"
          @click="handleRefresh"
        >
          <template #icon>
            <IconRefreshLine class="mr-2" />
          </template>
          刷新
        </VButton>
        <VButton type="danger" @click="handleClear()"> 清空 </VButton>
      </VSpace>
    </template>
  </VPageHeader>
  <div class="m-0 md:m-4"
       v-permission="['plugin:indexnow:manage']"
  >
    <VCard :body-class="['!p-0']">
      <div class="relative w-full">
        <VLoading v-if="isLoading" />
        <Transition v-else-if="!logs?.items?.length" appear name="fade">
          <VEmpty
            message="当前还没有推送记录，请确保已经正确配置了搜索的信息"
            title="暂无推送记录"
          >
            <template #actions>
              <VSpace>
                <VButton :loading="isFetching" @click="refetch()"> 刷新 </VButton>
                <VButton
                  v-permission="['plugin:sitepush:manage']"
                  type="secondary"
                  @click="$router.push('/plugins/indexNow?tab=basic')"
                >
                  去配置
                </VButton>
              </VSpace>
            </template>
          </VEmpty>
        </Transition>
        <div v-else class="relative w-full">
          <table class="w-full">
            <thead>
              <tr>
                <th class="px-4 py-3 text-left">
                  <div class="w-max flex items-center">URL</div>
                </th>
                <th class="px-4 py-3 text-right">
                  <div class="w-max flex items-center gap-1 justify-end">
                    状态/时间
                    <IconParkHelp
                      v-tooltip="'悬浮状态可查看返回信息，点击可复制返回信息'"
                      class="Handsome-indexnow-logs-help h-4 w-4 text-gray-400 cursor-help"
                    />
                  </div>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="log in formattedLogs"
                :key="log.metadata.name"
                class="group cursor-pointer hover:bg-gray-50"
              >
                <td class="px-4 py-3">
                  <a
                    :href="log.logsSpec?.pushUrl"
                    target="_blank"
                    class="text-blue-500 hover:text-blue-600"
                  >
                    {{ log.logsSpec?.pushUrl || '无URL' }}
                  </a>
                </td>
                <td class="px-4 py-3 text-right w-56">
                  <div class="Handsome-indexnow-logs-status-time flex items-center justify-end gap-2 min-w-[120px] max-w-[200px]">
                    <span
                      v-tooltip="log.logsSpec?.message || ''"
                      class="Handsome-indexnow-logs-status inline-flex items-center justify-center min-w-[60px] max-w-[120px] truncate mr-1"
                      @click="copyMessage(log.logsSpec?.message || '')"
                      style="cursor: pointer;"
                    >
                      <VStatusDot
                        :state="state(log.logsSpec?.message || '')"
                        :text="statusText(log.logsSpec?.message || '')"
                        :animate="state(log.logsSpec?.message || '') === 'success'"
                      />
                    </span>
                    <span class="Handsome-indexnow-logs-time text-gray-500 text-sm select-text whitespace-nowrap">
                      {{ log.metadata.timestamp }}
                    </span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="flex flex-wrap items-center justify-between gap-4 p-4">
        <VPagination
          v-model:page="page"
          v-model:size="size"
          page-label="页"
          size-label="条 / 页"
          :total-label="`共 ${total} 项数据`"
          :total="total"
          :size-options="[10, 20, 30, 50, 100]"
        />
      </div>
    </VCard>
  </div>
</template>

<style scoped>
.text-green-500 {
  color: #10b981;
}
.text-red-500 {
  color: #ef4444;
}
</style> 
