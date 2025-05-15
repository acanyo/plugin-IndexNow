package cc.lik.indexNow.extension;

import cc.lik.indexNow.service.PushIndexNowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Post;
import run.halo.app.event.post.PostPublishedEvent;
import run.halo.app.extension.ExtensionClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushIndexNowPublisher {
    private final ExtensionClient client;
    private final PushIndexNowService phpSvc;

    @Async
    @EventListener(PostPublishedEvent.class)
    public void onPostPublished(PostPublishedEvent event) {
        client.fetch(Post.class, event.getName())
            .ifPresent(post -> {
                log.info("开始处理文章发布事件: {}", event.getName());
                phpSvc.pushIndexNow(post)
                    .subscribe(
                        response -> log.info("IndexNow推送完成: {}", response),
                        error -> log.error("IndexNow推送失败", error)
                    );
            });
    }
}
