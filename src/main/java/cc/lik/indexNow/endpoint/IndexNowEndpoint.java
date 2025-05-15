package cc.lik.indexNow.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import cc.lik.indexNow.service.PushIndexNowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexNowEndpoint implements CustomEndpoint {
    private final PushIndexNowService pushIndexNowService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "api.indexnow.lik.cc/v1alpha1/indexnow";
        return SpringdocRouteBuilder.route()
            .POST("/indexnow/push", this::pushIndexNow, builder -> {
                builder.operationId("Push IndexNow")
                    .tag(tag)
                    .description("推送文章到 IndexNow")
                    .requestBody(requestBodyBuilder()
                        .implementation(Post.class)
                        .required(true)
                        .description("要推送的文章")
                    )
                    .response(responseBuilder()
                        .responseCode("200")
                        .description("推送成功")
                        .implementation(String.class)
                    )
                    .response(responseBuilder()
                        .responseCode("400")
                        .description("请求格式错误")
                    )
                    .response(responseBuilder()
                        .responseCode("403")
                        .description("key 无效")
                    )
                    .response(responseBuilder()
                        .responseCode("422")
                        .description("URL 不属于该主机或 key 不匹配")
                    )
                    .response(responseBuilder()
                        .responseCode("429")
                        .description("请求过于频繁")
                    );
            })
            .DELETE("/indexnow/clear", this::clearLogs, builder -> {
                builder.operationId("Clear IndexNow Logs")
                    .tag(tag)
                    .description("清空所有 IndexNow 推送日志")
                    .response(responseBuilder()
                        .responseCode("204")
                        .description("清理成功，无内容返回")
                    );
            })
            .build();
    }

    private Mono<ServerResponse> pushIndexNow(ServerRequest request) {
        return request.bodyToMono(Post.class)
            .doOnError(e -> log.error("Failed to parse request body", e))
            .flatMap(post -> {
                if (post == null) {
                    return Mono.error(new IllegalArgumentException("Post cannot be null"));
                }
                return pushIndexNowService.pushIndexNow(post);
            })
            .flatMap(result -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(result))
            .onErrorResume(e -> {
                log.error("Failed to push to IndexNow", e);
                return ServerResponse.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(e.getMessage());
            });
    }

    private Mono<ServerResponse> clearLogs(ServerRequest request) {
        return pushIndexNowService.clearLogs()
            .then(ServerResponse.noContent().build())
            .onErrorResume(e -> {
                log.error("Failed to clear IndexNow logs", e);
                return ServerResponse.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(e.getMessage());
            });
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("api.indexnow.lik.cc/v1alpha1");
    }
}
