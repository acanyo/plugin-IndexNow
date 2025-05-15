package cc.lik.indexNow.service.impl;

import cc.lik.indexNow.entity.HandsomeIndexNowLogs;
import cc.lik.indexNow.service.PushIndexNowService;
import cc.lik.indexNow.service.SettingConfigGetter;
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

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushIndexNowServiceImpl implements PushIndexNowService {

    private final SettingConfigGetter settingConfigGetter;
    private final WebClient webClient;
    private final ExtensionClient extensionClient;
    private static final Path staticDir = Paths.get(System.getProperty("user.home"), "/.halo2/");
    private final String indexNowUrl = "https://api.indexnow.org/IndexNow";

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
                    if (!Files.exists(keyFile)) {
                        Files.createDirectories(staticDir);
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
                        .flatMap(response -> saveLog(postUrl, "推送成功: " + response)
                            .thenReturn(response))
                        .onErrorResume(error -> saveLog(postUrl, "推送失败: " + error.getMessage())
                            .then(Mono.error(error)));
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
        .flatMap(logs -> Mono.fromRunnable(() -> extensionClient.create(logs)))
        .then();
    }

    private record IndexNowRequest(
        String host,
        String key,
        List<String> urlList
    ) {}
}
