package cc.lik.indexNow.service;

import lombok.Data;
import reactor.core.publisher.Mono;

public interface SettingConfigGetter {
    Mono<BasicConfig> getBasicConfig();

    @Data
    class BasicConfig {
        public static final String GROUP = "basic";
        private Boolean enablePush;
        private String siteUrl;
        private String indexNowKey;
    }
}
