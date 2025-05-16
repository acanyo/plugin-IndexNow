package cc.lik.indexNow.job;

import cc.lik.indexNow.service.PushIndexNowService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import reactor.core.publisher.Mono;

@Component
@EnableScheduling
@AllArgsConstructor
@Slf4j
public class NotPushedDataJob {
    private PushIndexNowService pushSvc;
    private final ReactiveExtensionClient client;
    //按照indexNow文档说明 未更新数据的url文章不需要推送 故此暂时去掉 定时任务 如果有失败 建议手动尝试
    // @Scheduled(cron = "0 0 0 * * ?")
   /* public void clean() {
        log.info("开始执行未同步文章推送任务");
        ListOptions listOptions = ListOptions.builder()
            .labelSelector()
            .notExists("indexnow/lik.cc/indexNowSuccessful")
            .end()
            .build();

        client.listAll(Post.class, listOptions, null)
            .flatMap(post -> {
                log.info("开始推送文章: {}", post.getMetadata().getName());
                return pushSvc.pushIndexNow(post)
                    .doOnSuccess(result -> log.info("文章推送成功: {}", post.getMetadata().getName()))
                    .doOnError(error -> log.error("文章推送失败: {}, 原因: {}", post.getMetadata().getName(), error.getMessage()))
                    .thenReturn(post);
            })
            .doOnComplete(() -> log.info("未同步文章推送任务完成"))
            .doOnError(error -> log.error("未同步文章推送任务异常: {}", error.getMessage()))
            .subscribe();
    }*/
}
