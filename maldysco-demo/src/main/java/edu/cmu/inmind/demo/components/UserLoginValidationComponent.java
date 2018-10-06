package edu.cmu.inmind.demo.components;

import com.google.common.eventbus.Subscribe;
import edu.cmu.inmind.demo.common.DemoConstants;
import edu.cmu.inmind.demo.common.Schedule;
import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardSubscription;
import edu.cmu.inmind.multiuser.controller.common.Constants;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import edu.cmu.inmind.multiuser.controller.plugin.StateType;

@StateType(state = Constants.STATELESS)
@BlackboardSubscription(messages= DemoConstants.MSG_CHECK_USER_ID)
public class UserLoginValidationComponent extends PluggableComponent {

    private void checkUserLogin(String username, Blackboard blackboard){
        String validate = Schedule.validate(username);
        if(validate.equals(Schedule.USER_ID_NOT_EXISTS))
            validate = "Wrong MKT id, please try again!";
        else if(validate.equals(Schedule.TOO_EARLY))
            validate = "You have connected too early, please come back at your scheduled time!";
        else if(validate.equals(Schedule.TOO_LATE))
            validate = "Sorry, you have connected too late, please request another time slot through the doodle!";
        else
        {
            validate = "Session "+username + " has been successfully created. Initiate chat to start the user study.";
            blackboard.post(this, DemoConstants.MSG_GROUP_CHAT_READY,
                    validate);
        }
    }

    @Override
    public Blackboard getBlackBoard(String sessionId) {
        return super.getBlackBoard(sessionId);
    }

    @Override
    public void execute() {
        super.execute();
    }

    @Override
    protected void startUp() {
        super.startUp();
    }

    @Override
    public void shutDown() {
        super.shutDown();
    }

    @Override
    public void onEvent(Blackboard blackboard, BlackboardEvent blackboardEvent) throws Throwable {
        switch(blackboardEvent.getId())
        {
            case DemoConstants.MSG_CHECK_USER_ID:
                checkUserLogin(blackboardEvent.getSessionId(),
                        blackboard);
                break;
            default:
                break;
        }
    }
}