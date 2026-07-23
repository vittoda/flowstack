package com.flowstack.flow;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class StepGroup {

    private HashMap<String, Step> _mSteps = new HashMap<>();

    public StepGroup() {
    }


    public Step getStep(String name) {
        return _mSteps.get(name);
    }

    public void addStep(Step step) {
        _mSteps.put(step.name, step);
    }

    public ArrayList<Step> getAllLastSteps() {
        ArrayList<Step> lastSteps= new ArrayList<>();
        for(Step step : _mSteps.values()) {
            if(step.nextSteps == null || step.nextSteps.length == 0) {
                lastSteps.add(step);
            }
        }

        return lastSteps;
    }

    public Step addSteps(ArrayNode steps, String agentDefaultModelName) {
        int len = steps.size();
        Step firstStep = null;
        String[] stepNames = new String[steps.size()];
        for (int i = 0; i < len; i++) {
            JsonNode step = steps.get(i);
            String name = step.get("name").asText();
            stepNames[i] = name;
            JsonNode desc = step.get("description");
            String description = desc ==null || desc.isNull() ? "" :  desc.asText();
            String instruction = step.get("instruction").asText();
            String type = step.has("type") ? step.get("type").asText() : Step.STEP_TYPE_LLM;

            String nextSteps[] = null;
            if (step.has("nextStep")) {
                ArrayNode nextStepNamesObject = (ArrayNode)step.get("nextStep");
                nextSteps = nextStepNamesObject.size() > 0 ?  new String[nextStepNamesObject.size()+1] : new String[0]; //+1 for error step
                for (int j = 0; j < nextStepNamesObject.size(); j++) {
                        nextSteps[j] = nextStepNamesObject.get(j).asText();
                }

                if(nextStepNamesObject.size() > 0) {
                    nextSteps[nextSteps.length-1] = "__error__";
                }
            }
            else {
                nextSteps = new String[0];
                //nextSteps[0] = "__error__";
            }

            String modelName = agentDefaultModelName;
            if (step.has("model")) {
                modelName = step.get("model").asText();
            }

            ArrayNode toolsObject = step.has("tools") ? (ArrayNode)step.get("tools") : null;
            String[] tools = null;
            if (toolsObject != null) {
                tools = new String[toolsObject.size()];
                for (int j = 0; j < toolsObject.size(); j++) {
                    tools[j] = toolsObject.get(j).asText();
                }
            }

            boolean jsonResponse = false;
            if(step.has("jsonResponse")) {
                jsonResponse = step.get("jsonResponse").asBoolean();
            }

            boolean hitlNeeded = false;
            if(step.has("hitlNeeded")) {
                hitlNeeded = step.get("hitlNeeded").asBoolean();
            }

            HITLMessage hitlConfig = null;
            if(hitlNeeded) {
                //Get and parse the content
                JsonNode hitlMessageNode = step.get("hitlMessage");
                hitlConfig = HITLMessage.loadFromJSON(hitlMessageNode);
            }

            String agent = null;
            if(step.has("agent")) {
                agent = step.get("agent").asText();
            }

            Step st = new Step(name, description, instruction, null, type, modelName, agent, nextSteps, tools, jsonResponse, 
                hitlNeeded, hitlConfig);
            _mSteps.put(name, st);
            if(i == 0) {
                firstStep = st;
            }
        }

        //Add error step.
        Step errorStep = Step.getErrorStrep(stepNames, agentDefaultModelName);
        _mSteps.put("__error__", errorStep);

        return firstStep;

    }

    public void addEndFlowStep(String modelName, ArrayList<Step> lastSteps) {
        Step endFlowStep = new Step("flow_EndFlow",
            "This is the last step for a flow, which will get the result.",
            "This is the last step in the flow. You will need to prepare a result and call the agent_EndFlow, with result of the entore flow. The result will be used by caller for subsequent actions.",
            null,
            Step.STEP_TYPE_LLM,modelName,  null,new String[0],
            new String[] {"agent_endFlow"}, true, false, null);

        //We are not direcly calling the getLastSteps here intentionally. Hvaing no last step is an error. So,
        //this should be handled at model respnse handler. 

        for(Step l : lastSteps) {
            l.nextSteps = new String[]{"flow_EndFlow"};
        }
        _mSteps.put("flow_EndFlow", endFlowStep);
    }



}
