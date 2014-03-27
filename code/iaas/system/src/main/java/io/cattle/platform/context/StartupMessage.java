package io.cattle.platform.context;

import io.cattle.platform.iaas.config.ScopedConfig;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupMessage implements Runnable {

    private static final Logger consoleLog = LoggerFactory.getLogger("ConsoleStatus");

    ScopedConfig scopedConfig;

    @Override
    public void run() {
        String apiUrl = scopedConfig.getApiUrl(null);
        consoleLog.info("[URL  ] API               : {}", apiUrl);
        consoleLog.info("[URL  ] SSH Authorization : {}/authorized_keys", apiUrl);
    }

    public ScopedConfig getScopedConfig() {
        return scopedConfig;
    }

    @Inject
    public void setScopedConfig(ScopedConfig scopedConfig) {
        this.scopedConfig = scopedConfig;
    }

}
