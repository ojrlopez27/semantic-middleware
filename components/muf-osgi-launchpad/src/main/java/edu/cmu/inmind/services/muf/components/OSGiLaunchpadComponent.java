package edu.cmu.inmind.services.muf.components;

import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.common.Constants;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;
import edu.cmu.inmind.services.muf.components.osgi.LaunchpadStarterController;
import edu.cmu.inmind.services.muf.inputs.LaunchpadInput;

import static edu.cmu.inmind.services.muf.commons.Constants.MSG_LP_GET_ALL_SERVICES;
import static edu.cmu.inmind.services.muf.commons.Constants.MSG_LP_GET_SERVICE_IMPL;
import static edu.cmu.inmind.services.muf.commons.Constants.MSG_LP_INPUT_CMD;
import static edu.cmu.inmind.services.muf.commons.Constants.MSG_LP_LIST_SERVICES;
import static edu.cmu.inmind.services.muf.commons.Constants.MSG_LP_OUTPUT_CMD;
import static edu.cmu.inmind.services.muf.commons.Constants.MSG_LP_START_SERVICE;

@StateType(state = Constants.STATEFULL)
@BlackboardSubscription(messages = {MSG_LP_INPUT_CMD})
public class OSGiLaunchpadComponent extends PluggableComponent {

    LaunchpadStarterController launchpadStarterController;

    @Override
    public void startUp(){
        super.startUp();
        Log4J.info(this, "Inside OSGiLaunchpadComponent.startUp");
        launchpadStarterController = new LaunchpadStarterController();
        launchpadStarterController.initFramework();
    }

    @Override
    public void shutDown() {
        super.shutDown();
        Log4J.info(this, "Inside OSGiLaunchpadComponent.shutDwon");
        launchpadStarterController.stopFramework();
    }

    /*
    @Override
    public void execute() {
        Log4J.info(this, "Inside OSGiLaunchpadComponent.execute: " + hashCode());
        handleService(getBlackBoard(getSessionId()));
    }
    */

    @Override
    public void onEvent(Blackboard blackboard, BlackboardEvent blackboardEvent) throws Throwable {
        Log4J.info(this, "Inside OSGiLaunchpadComponent.onEvent");
        try {
            // read the input command
            LaunchpadInput launchpadInput = (LaunchpadInput) blackboardEvent.getElement();
            String launchpadInputCommand = launchpadInput.getCommand();

            // process the input command
            switch (launchpadInputCommand) {
                case MSG_LP_START_SERVICE:
                    launchpadStarterController.startService(launchpadInput.getOSGiService());
                    break;
                case MSG_LP_LIST_SERVICES:
                    launchpadStarterController.listServices();
                    break;
                case MSG_LP_GET_ALL_SERVICES:
                    launchpadStarterController.getAllServices();
                    break;
                case MSG_LP_GET_SERVICE_IMPL:
                    launchpadStarterController.getImplementation(launchpadInput.getServiceReference());
                default:
                    Log4J.info(this, "Inside OSGiLaunchpadComponent.handleService " + " -- not sure what to do.");
            }

            // identify the output:
            // May be, a custom LaunchpadOutput object?
            // ... do something interesting here ...
            Object launchpadOutput = new Object();

            // update the blackboard
            if (launchpadInput.getOSGiService() != null) {
                blackboard.post(this, MSG_LP_OUTPUT_CMD, launchpadInput.getOSGiService()); // launchpadOutput);
            }

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

}