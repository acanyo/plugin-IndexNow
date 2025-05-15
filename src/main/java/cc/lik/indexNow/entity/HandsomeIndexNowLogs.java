package cc.lik.indexNow.entity;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@GVK(kind = "HandsomeIndexNowLogs", group = "indexnow.lik.cc",
    version = "v1alpha1", singular = "handsomeindexnowlogs", plural = "handsomeindexnowlogs")
public class HandsomeIndexNowLogs extends AbstractExtension {
    @Schema(requiredMode = REQUIRED)
    private indexNowLogsSpec logsSpec;
    @Data
    public static class indexNowLogsSpec {
        private String pushUrl;
        private Long pushTime;
        private String message;
    }
}
