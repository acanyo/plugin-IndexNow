package cc.lik.indexNow;

import cc.lik.indexNow.entity.HandsomeIndexNowLogs;
import org.springframework.stereotype.Component;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

/**
 * <p>Plugin main class to manage the lifecycle of the plugin.</p>
 * <p>This class must be public and have a public constructor.</p>
 * <p>Only one main class extending {@link BasePlugin} is allowed per plugin.</p>
 *
 * @author guqing
 * @since 1.0.0
 */
@Component
public class IndexNowPlugin extends BasePlugin {

    private final SchemeManager schemeManager;

    public IndexNowPlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        schemeManager.register(HandsomeIndexNowLogs.class);
    }

    @Override
    public void stop() {
        Scheme scheme = schemeManager.get(HandsomeIndexNowLogs.class);
        schemeManager.unregister(scheme);
    }
}
