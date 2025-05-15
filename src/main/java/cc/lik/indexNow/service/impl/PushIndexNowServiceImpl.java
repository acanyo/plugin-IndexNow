package cc.lik.indexNow.service.impl;

import cc.lik.indexNow.service.PushIndexNowService;
import cc.lik.indexNow.service.SettingConfigGetter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.content.Post;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushIndexNowServiceImpl implements PushIndexNowService {

    private final SettingConfigGetter settingConfigGetter;
    private static Path filePath = Paths.get(System.getProperty("user.home"), "/.halo2/");
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
                        // 检查文件是否存在
                        if (Files.exists(filePath)) {
                            log.info("IndexNow key 文件已存在: {}", filePath);
                            return "success";
                        }
                        Files.write(filePath, indexNowKey.getBytes());
                        log.info("成功创建 IndexNow key 文件: {}", filePath);
                        return "success";
                    } catch (IOException e) {
                        log.error("创建 IndexNow key 文件失败", e);
                        throw new RuntimeException("创建 IndexNow key 文件失败: " + e.getMessage());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::just);
            });
    }
}
