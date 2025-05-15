package cc.lik.indexNow.service.impl;

import cc.lik.indexNow.service.PushIndexNowService;
import cc.lik.indexNow.service.SettingConfigGetter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushIndexNowServiceImpl implements PushIndexNowService {

    private final SettingConfigGetter settingConfigGetter;

    private final String indexNowUrl = "https://api.indexnow.org/IndexNow";
    @Override
    public Mono<String> pushIndexNow(Post post) {
        return settingConfigGetter.getBasicConfig()
            .switchIfEmpty(Mono.error(new RuntimeException("无法获取基本配置"))).flatMap(config->{
                String permalink = post.getStatus().getPermalink();
                String url = indexNowUrl + "?url=" + permalink;
                return null;
            });
    }
}
