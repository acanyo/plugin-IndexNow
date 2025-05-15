import { definePlugin } from "@halo-dev/console-shared";
import HomeView from "./views/HomeView.vue";
import {Dialog, IconPlug, Toast, VDropdownDivider, VDropdownItem} from "@halo-dev/components";
import { markRaw } from "vue";
import type { ListedPost } from "@halo-dev/api-client";
import axios, {AxiosError} from "axios";

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: "Root",
      route: {
        path: "/example",
        name: "Example",
        component: HomeView,
        meta: {
          title: "示例页面",
          searchable: true,
          menu: {
            name: "示例页面",
            group: "示例分组",
            icon: markRaw(IconPlug),
            priority: 0,
          },
        },
      },
    },
  ],
  extensionPoints: {
    'post:list-item:operation:create': () => {
      return [
        {
          priority: 21,
          component: markRaw(VDropdownItem),
          label: '同步IndexNow索引',
          visible: true,
          action: async (item?: ListedPost) => {
            if (!item) return;
            Dialog.warning({
              title: '同步IndexNow索引',
              description:
                '确定同步此文章吗?',
              onConfirm: async () => {
                try {
                  await axios.post(
                    `/apis/api.indexnow.lik.cc/v1alpha1/push`,
                    item.post,
                    {
                      headers: {
                        'Content-Type': 'application/json',
                      },
                    }
                  );
                  Toast.success('同步IndexNow索引成功！');
                } catch (error) {
                  if (error instanceof AxiosError) {
                    const data = error.response?.data;
                    const msg = typeof data === 'string' ? data : '同步失败，请重试';
                    Toast.error(msg);
                  }
                }
              },
            });
          },
        },
      ];
    },
  },
});
