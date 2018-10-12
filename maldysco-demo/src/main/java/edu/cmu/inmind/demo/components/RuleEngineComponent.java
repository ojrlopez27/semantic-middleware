package edu.cmu.inmind.demo.components;

import edu.cmu.inmind.demo.common.DemoConstants;
import edu.cmu.inmind.demo.common.Node;
import edu.cmu.inmind.demo.common.Utils;
import edu.cmu.inmind.demo.model.WorkingMemory;
import edu.cmu.inmind.multiuser.controller.blackboard.Blackboard;
import edu.cmu.inmind.multiuser.controller.blackboard.BlackboardEvent;
import edu.cmu.inmind.multiuser.controller.plugin.PluggableComponent;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.jeasy.rules.mvel.MVELRule;

import java.util.ArrayList;
import java.util.List;

public class RuleEngineComponent extends PluggableComponent {
    // AS: Abstract Services, GS: Grounded Services
    private WorkingMemory wm;
    private Rules rulesAS, rulesGS;
    private RulesEngine rulesEngineAS, rulesEngineGS;
    private Facts factsAS;
    private List<Rule> ruleListAS, ruleListGS;


    public RuleEngineComponent() {
        wm = new WorkingMemory();
        rulesAS = new Rules();
        rulesGS = new Rules();
        factsAS = new Facts();
        rulesEngineAS = new DefaultRulesEngine();
        rulesEngineGS = new DefaultRulesEngine();
        ruleListAS = new ArrayList<>();
        ruleListGS = new ArrayList<>();
    }
    public void addGoal(String goal) {
        // create some facts
        wm.setGoal(goal);
        factsAS.put("wm", wm);
        // we do this only the first time
        wm.setLastRuleName(goal);
        wm.setCommand(goal);
    }

    /**
     * This is just a simple chaining of rules.
     * @param step
     */
    public MVELRule addStep(String step, String abstracServiceCandidates) {
        String ruleName = Utils.getRuleName(step);
        List<String> candidates = new ArrayList<>();
        candidates.add(abstracServiceCandidates);
        wm.getCandidates().add(candidates);
        MVELRule rule = new MVELRule()
                .name(ruleName)
                .description(step)
                .priority(1)
                .when( "wm.command == wm.lastRuleName")
                .then("wm.print(\"Triggering '" + ruleName + "'...\"); ")
                .then(String.format("wm.command = \"%s\"; ", ruleName))
                .then(String.format("wm.abstractService = \"%s\";", step))
                .then(String.format("wm.lastRuleName = \"%s\"; ", ruleName));
        ruleListAS.add(rule);
        return rule;
    }

    /**
     * This method will put in WM the list of abastract services
     */
    public void fireRulesAS(){
        //create a default rules engine and fire rules on known facts
        rulesEngineAS.fire(rulesAS, factsAS);
    }


    /**
     * We can also simulate here some changing conditions like gradual battery draining, or
     * weak/strong wifi access changes.
     */
    public void addPhoneStatusToWM(){
        wm.setBattery(DemoConstants.PHONE_HIGH_BATTERY);
        wm.setWifi(DemoConstants.WIFI_ON);
        //wm.setExecutor(serviceExecutor);
        wm.setCommand(DemoConstants.CHECK_BATTERY);
    }

    public void addStepAndRegister(String step, String abstracServiceCandidates) {
        rulesAS.register( addStep(step, abstracServiceCandidates) );
    }

    public CompositeService generateCompositeServiceRequest() {
        CompositeServiceBuilder builder = null;
        // create a rule set
        for(Rule rule : ruleListAS){
            rulesAS.register(rule);
            if(builder == null) builder = new CompositeServiceBuilder(rule.getDescription());
            else builder.and(rule.getDescription());
        }
        return builder.build();
    }
    /**
     * Heuristic rules for grounding services (given a set of abstract services) based on
     * non-functional QoS features (i.e., battery consumption, connectivity, availability, etc.)
     */
    public void createRulesForGroundingServices() {
        ruleListGS.add(
                new MVELRule()
                        .name("if-battery-low-then-low-consumption-rule")
                        .description("if battery is low, then look for a service method with low battery consumption")
                        .priority(1)
                        .when( String.format("wm.battery == \"%s\" && wm.command == \"%s\"",
                                DemoConstants.PHONE_LOW_BATTERY, DemoConstants.CHECK_BATTERY) )
                        .then( String.format("wm.executor.pick(\"%s\", wm.service); ", DemoConstants.LOW_BATTERY_SERVICE) )
        );

        ruleListGS.add(
                new MVELRule()
                        .name("if-battery-high-then-check-wifi-rule")
                        .description("if battery is high, then check connectivity")
                        .priority(1)
                        .when( String.format("wm.battery == \"%s\" && wm.command == \"%s\"",
                                DemoConstants.PHONE_HIGH_BATTERY, DemoConstants.CHECK_BATTERY) )
                        .then( String.format("wm.command = \"%s\"", DemoConstants.CHECK_WIFI) )
                        .then( String.format("wm.executor.setCandidate(\"%s\", wm.service); ", DemoConstants.PHONE_HIGH_BATTERY) )
        );

        ruleListGS.add(
                new MVELRule()
                        .name("if-wifi-on-then-pick-remote-service-rule")
                        .description("if wifi is ON then pick a remote service")
                        .priority(1)
                        .when( String.format("wm.wifi == \"%s\" && wm.command == \"%s\"",
                                DemoConstants.WIFI_ON, DemoConstants.CHECK_WIFI) )
                        .then( String.format("wm.executor.pick(\"%s\", wm.service); ", DemoConstants.PICK_REMOTE_SERVICE) )
        );

        ruleListGS.add(
                new MVELRule()
                        .name("if-wifi-off-then-pick-local-service-rule")
                        .description("if wifi is OF then pick a local service")
                        .priority(1)
                        .when( String.format("wm.wifi == \"%s\" && wm.command == \"%s\"",
                                DemoConstants.WIFI_OFF, DemoConstants.CHECK_WIFI) )
                        .then( String.format("wm.executor.pick(\"%s\", wm.service); ", DemoConstants.PICK_LOCAL_SERVICE) )
        );

        for(Rule rule : ruleListGS){
            rulesGS.register(rule);
        }
    }

    public void fireRulesGS(){
        //create a default rules engine and fire rules on known facts
        rulesEngineGS.fire(rulesGS, factsAS);
    }

    // private ServiceExecutor serviceExecutor;
    @Override
    public void onEvent(Blackboard blackboard, BlackboardEvent blackboardEvent) throws Throwable {

    }

    @Override
    protected void startUp() {
        super.startUp();
    }

    @Override
    public void shutDown() {
        super.shutDown();
    }

    public class CompositeService {
        private List<Node> nodes = new ArrayList();
        private int idx = 0;

        private CompositeService(CompositeServiceBuilder builder){
            this.nodes = builder.nodes;
        }

        public List<Node> getNodes() {
            return nodes;
        }

        public String getNext() {
            if (idx < nodes.size()) {
                return nodes.get(idx++).getName();
            }
            return "No more services to process!";
        }
    }

    public class CompositeServiceBuilder{
        private List<Node> nodes = new ArrayList();

        private CompositeServiceBuilder(String request){
            and( request );
        }

        public CompositeServiceBuilder and(String request) {
            nodes.add(new Node(request, Node.NodeType.PLAIN));
            return this;
        }

        public CompositeService build(){
            return new CompositeService(this);
        }
    }

}
