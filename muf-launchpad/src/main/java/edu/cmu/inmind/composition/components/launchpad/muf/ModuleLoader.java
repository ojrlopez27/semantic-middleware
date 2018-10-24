package edu.cmu.inmind.composition.components.launchpad.muf;

import edu.cmu.inmind.multiuser.controller.common.CommonUtils;
import edu.cmu.inmind.multiuser.controller.common.Constants;
import edu.cmu.inmind.multiuser.controller.plugin.PluginModule;
import edu.cmu.inmind.multiuser.controller.resources.Config;
import java.util.concurrent.TimeUnit;

import static edu.cmu.inmind.composition.components.launchpad.commons.Constants.MUF_LAUNCHPAD_SERVER_PORT;
import static edu.cmu.inmind.composition.components.launchpad.commons.Constants.MUF_LOGS_EXCEPTION;
import static edu.cmu.inmind.composition.components.launchpad.commons.Constants.MUF_LOGS_REGULAR;

public class ModuleLoader {

    private static final int NUM_POOL_INSTANCES = 10;
    private static final int SESSION_TIMEOUT_THRESHOLD = 5;     // minutes
    private static final String URL_SERVER_ADDRESS = "tcp://127.0.0.1";

    public static PluginModule[] createComponents() {
        return new PluginModule[]{
                new PluginModule.Builder(MUFLaunchpadOrchestrator.class,
                        MUFLaunchpadComponent.class,
                        MUFLaunchpadComponent.class.getSimpleName())
                        .build()
        };
    }

    public static Config createConfig() {
        return new Config.Builder()
                .setServerAddress(URL_SERVER_ADDRESS)
                .setSessionManagerPort(Integer.parseInt(CommonUtils.getProperty(MUF_LAUNCHPAD_SERVER_PORT)))
                .setSessionTimeout(SESSION_TIMEOUT_THRESHOLD, TimeUnit.MINUTES)
                .setDefaultNumOfPoolInstances(NUM_POOL_INSTANCES)
                .setPathLogs(CommonUtils.getProperty(MUF_LOGS_REGULAR))
                .setPathExceptionLogger(CommonUtils.getProperty(MUF_LOGS_EXCEPTION))
                .setExceptionTraceLevel(Constants.SHOW_ALL_EXCEPTIONS)
                .build();
    }

}