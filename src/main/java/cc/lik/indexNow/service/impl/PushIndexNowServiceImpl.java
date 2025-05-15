package cc.lik.indexNow.service.impl;

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
    private static final Path staticDir = Paths.get(System.getProperty("user.home"), "/.halo2/");
    private final String indexNowUrl = "https://api.indexnow.org/IndexNow";

    @Override
    public Mono<String> pushIndexNow(Post post) {
        return settingConfigGetter.getBasicConfig()
            .switchIfEmpty(Mono.error(new RuntimeException("无法获取基本配置"))).flatMap(config->{
                String permalink = post.getStatus().getPermalink();
                String url = indexNowUrl + "?url=" + permalink;
                String indexNowKey = config.getIndexNowKey();
                
                // 创建文件
                return Mono.fromCallable(() -> {
                    try {
                        Path keyFile = staticDir.resolve(indexNowKey + ".txt");
                        // 检查文件是否存在
                        if (Files.exists(keyFile)) {
                            log.info("IndexNow key 文件已存在: {}", keyFile);
                            return null;
                        }
                        Files.createDirectories(staticDir);
                        Files.writeString(keyFile, indexNowKey);
                        log.info("成功创建 IndexNow key 文件: {}", keyFile);
                        return null;
                    } catch (IOException e) {
                        log.error("创建 IndexNow key 文件失败", e);
                        throw new RuntimeException("创建 IndexNow key 文件失败: " + e.getMessage());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.defer(() -> {
                    // 从 siteUrl 中提取主机名
                    String host = URI.create(config.getSiteUrl()).getHost();
                    
                    // 构建请求体
                    IndexNowRequest request = new IndexNowRequest(
                        host,
                        indexNowKey,
                        List.of(permalink)
                    );

                    // 调用 IndexNow API
                    return webClient.post()
                        .uri(indexNowUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(String.class)
                        .doOnSuccess(response -> log.info("IndexNow API 调用成功: {}", response))
                        .doOnError(error -> log.error("IndexNow API 调用失败", error));
                }));
            });
    }

    private record IndexNowRequest(
        String host,
        String key,
        List<String> urlList
    ) {}
}
