package cc.lik.indexNow.job;

import cc.lik.indexNow.service.PushIndexNowService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@AllArgsConstructor
@Slf4j
public class CleanOldDataJob {
    private PushIndexNowService pushSvc;

    @Scheduled(cron = "0 0 0 ? * SUN")
    public void clean() {
        pushSvc.clearLogs();
    }
}
