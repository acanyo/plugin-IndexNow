package cc.lik.indexNow.service;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;

public interface PushIndexNowService {
    /**
     * @return 返回
     * 200
     * Ok
     * URL submitted
     * successfully
     * 400 Bad request
     * Invalid format
     * 403 Forbidden
     * In case of key not valid (e.g. key not found, file found but key not in the file)
     * 422 Unprocessable Entity
     * In case of URLs don't belong to the host or the key is not matching the schema in the protocol
     * 429
     * Too Many Requests
     * Too Many Requests (potential Spam)
     */
    Mono<String> pushIndexNow(Post post);

    /**
     * 清空所有 IndexNow 推送日志
     */
    Mono<Void> clearLogs();
}
