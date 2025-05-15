package cc.lik.indexNow.service.impl;

import cc.lik.indexNow.entity.HandsomeIndexNowLogs;
import cc.lik.indexNow.service.PushIndexNowService;
import cc.lik.indexNow.service.SettingConfigGetter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushIndexNowServiceImpl implements PushIndexNowService {

    private final SettingConfigGetter settingConfigGetter;
    private final WebClient webClient;
    private final ReactiveExtensionClient client;
    private static final Path staticDir = Paths.get(System.getProperty("user.home"), "/.halo2/static");
    private final String indexNowUrl = "https://api.indexnow.org/IndexNow";

    private Mono<Void> handleSuccess(Post post, String postUrl, String response) {
        log.info("IndexNow推送成功: {}", response);
        log.info("开始更新文章标签");
        return client.fetch(Post.class, post.getMetadata().getName())
            .switchIfEmpty(Mono.error(new RuntimeException("文章不存在")))
            .flatMap(latestPost -> {
                log.info("获取到最新文章数据");
                latestPost.getMetadata().getLabels().put("indexnow/lik.cc/indexNowSuccessful", "true");
                return client.update(latestPost)
                    .retry(3)
                    .doOnNext(v -> log.info("文章标签更新中..."))
                    .doOnSuccess(v -> log.info("文章标签更新成功"))
                    .doOnError(e -> log.error("文章标签更新失败", e));
            })
            .then(Mono.defer(() -> {
                log.info("准备记录成功日志");
                return saveLog(postUrl, "推送成功: " + response);
            }))
            .doOnSuccess(v -> log.info("成功日志记录完成"))
            .doOnError(e -> {
                log.error("记录成功日志失败", e);
            })
            .then();
    }

    @Override
    public Mono<String> pushIndexNow(Post post) {
        return settingConfigGetter.getBasicConfig()
            .switchIfEmpty(Mono.error(new RuntimeException("无法获取基本配置")))
            .flatMap(config -> {
                String permalink = post.getStatus().getPermalink();
                String postUrl = config.getSiteUrl() + permalink;
                String indexNowKey = config.getIndexNowKey();
                
                return Mono.fromCallable(() -> {
                    Path keyFile = staticDir.resolve(indexNowKey + ".txt");
                    if (!Files.exists(staticDir)) {
                        Files.createDirectories(staticDir);
                    }
                    if (!Files.exists(keyFile)) {
                        Files.writeString(keyFile, indexNowKey);
                    }
                    return null;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> Mono.error(new RuntimeException("创建 IndexNow key 文件失败: " + e.getMessage())))
                .then(Mono.defer(() -> {
                    String host = URI.create(config.getSiteUrl()).getHost();
                    // 构建请求体
                    IndexNowRequest request = new IndexNowRequest(
                        host,
                        indexNowKey,
                        List.of(postUrl)
                    );
                    return webClient.post()
                        .uri(indexNowUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(String.class)
                        .defaultIfEmpty("200")
                        .doOnNext(response -> log.info("收到IndexNow响应: {}", response))
                        .flatMap(response -> {
                            log.info("开始处理成功响应");
                            return handleSuccess(post, postUrl, response)
                                .subscribeOn(Schedulers.boundedElastic())
                                .doOnSuccess(v -> log.info("处理成功响应完成"))
                                .doOnError(error -> log.error("处理成功响应时发生错误", error))
                                .thenReturn(response);
                        })
                        .onErrorResume(error -> {
                            log.error("IndexNow推送失败", error);
                            return saveLog(postUrl, "推送失败: " + error.getMessage())
                                .subscribeOn(Schedulers.boundedElastic())
                                .then(Mono.error(error));
                        });
                }));
            });
    }

    private Mono<Void> saveLog(String pushUrl, String message) {
        return Mono.fromSupplier(() -> {
            HandsomeIndexNowLogs logs = new HandsomeIndexNowLogs();
            HandsomeIndexNowLogs.indexNowLogsSpec spec = new HandsomeIndexNowLogs.indexNowLogsSpec();
            spec.setPushUrl(pushUrl);
            spec.setPushTime(System.currentTimeMillis());
            spec.setMessage(message);
            logs.setMetadata(new Metadata());
            logs.getMetadata().setGenerateName("indexnow-logs-");
            logs.setLogsSpec(spec);
            return logs;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(client::create)
        .then();
    }

    private record IndexNowRequest(
        String host,
        String key,
        List<String> urlList
    ) {}
}
