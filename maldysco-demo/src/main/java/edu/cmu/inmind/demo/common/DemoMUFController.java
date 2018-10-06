package edu.cmu.inmind.demo.common;

import edu.cmu.inmind.demo.components.NERComponent;
import edu.cmu.inmind.demo.components.S2VComponent;
import edu.cmu.inmind.demo.components.UserLoginValidationComponent;
import edu.cmu.inmind.demo.orchestrator.DemoOrchestrator;
import edu.cmu.inmind.multiuser.controller.common.CommonUtils;
import edu.cmu.inmind.multiuser.controller.common.Constants;
import edu.cmu.inmind.multiuser.controller.plugin.PluginModule;
import edu.cmu.inmind.multiuser.controller.resources.Config;

import java.util.concurrent.TimeUnit;
/**
 * Created for demo : sakoju 10/4/2018
 */
public class DemoMUFController {
    public static Config createConfig()
    {
        String serverIpAddress = Utils.getSystemIPAddress();
        return new Config.Builder()
                .setExceptionTraceLevel(Constants.SHOW_ALL_EXCEPTIONS)
                .setSessionManagerPort
                        (Integer.parseInt(CommonUtils.
                                getProperty("server.composition")))
                .setPathLogs(
                        CommonUtils.getProperty("logs.mkt.regular.path"))
                .setDefaultNumOfPoolInstances(10)
                .setSessionTimeout(5, TimeUnit.MINUTES)
                .setServerAddress(serverIpAddress)
                .build();
    }

    public static PluginModule[] createModules()
    {
        return new PluginModule[]{
                new PluginModule.Builder(DemoOrchestrator.class,
                        NERComponent.class, DemoConstants.ID_NER)
                .addPlugin(S2VComponent.class, DemoConstants.ID_S2V)
                .addPlugin(UserLoginValidationComponent.class, DemoConstants.ID_USERLOGIN)
                .build()
        };
    }
}