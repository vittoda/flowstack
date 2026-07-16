package com.flowstack.flow;

import com.flowstack.models.ModelJSONResponse;
import com.flowstack.models.ModelResponse;
import com.flowstack.models.ModelThinkingResponse;
import com.flowstack.models.ModelToolResponse;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flowstack.JsonUtils;
import com.flowstack.mcp.MCPRegistry;
import com.flowstack.mcp.ToolCallResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelResponseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelResponseHandler.class);

    public void process(StepRunInstance stepRunInstance, FlowRunner flowRunner,
            ModelResponse response) throws FlowException {

        if (response == null) {
            LOGGER.warn("Response is null for session '{}', step '{}'", flowRunner.getSessionId(), stepRunInstance.getStepDefinition().name);
            flowRunner.currentStepFailed(StepRunInstance.FAIL_REASON_INVALID_RESPONSE, "No response from the model");
            return;
        }

        if (response instanceof ModelThinkingResponse) {
            flowRunner.runCurrentStepAgain();
            return;
        }

        if (response instanceof ModelJSONResponse) {
            ModelJSONResponse jsonRespnse = (ModelJSONResponse) response;
            ObjectNode content = jsonRespnse.responseData;
            // Probably steps.
            if (content.has("steps")) {
                StepsValidator sv = new StepsValidator();
                ArrayNode steps = (ArrayNode) content.get("steps");
                if (steps.size() == 0) {// Not sure how this will happen. But if it happens.
                    throw new FlowException("Model responsed with steps. But steps list is empty");
                }
                // TODO: The tools below should be specific to the flow runner ot step group.
                ArrayNode validationErrors = sv.validate(steps, MCPRegistry.getAllToolNames());
                if (validationErrors.size() > 0) {
                    LOGGER.error("Response validation failed. ", steps.toPrettyString());
                    flowRunner.currentStepFailed(StepRunInstance.FAIL_REASON_INVALID_RESPONSE,
                            validationErrors.toString());
                    return;
                }

                // We will be adding as one for current
                StepGroup sg = new StepGroup();
                Step firstStep = sg.addSteps(steps, flowRunner.getModelName());
                flowRunner.clearAndRunStepGroup(sg, firstStep);
                return;
            }
            String nextStep = null;
            if (content.has("nextStep")) {
                nextStep = content.get("nextStep").asText();
                flowRunner.runStep(nextStep, null);
            } else {
                flowRunner.currentStepCompleted(true);
            }

        } else if (response instanceof ModelToolResponse) {
            ModelToolResponse toolResponse = (ModelToolResponse) response;
            stepRunInstance.addLogInfo("selectedTool", toolResponse.toolName);
            if (toolResponse.toolName.equals("agent_runStep")) {
                // We need special handling for internal tools.
                String stepName = toolResponse.arguments.get("stepName").asText();
                flowRunner.runStep(stepName, null);
                ObjectNode toolResult = JsonUtils.MAPPER.createObjectNode();
                toolResult.put("status", "Success");
                toolResult.put("toolResult", "Step '"+stepName+"' triggered");
                flowRunner.addToolResult(toolResponse.toolName, toolResponse.toolId, toolResult);
                flowRunner.currentStepCompleted(true);
                return;
            } else if (toolResponse.toolName.equals("agent_runErrorStep")) {
                // We need special handling for internal tools.
                String stepName = "__error__";
                flowRunner.runStep(stepName, toolResponse.arguments.get("errorDetails").asText());
                ObjectNode toolResult = JsonUtils.MAPPER.createObjectNode();
                toolResult.put("status", "Success");
                toolResult.put("toolResult", "Error step triggered");
                flowRunner.addToolResult(toolResponse.toolName, toolResponse.toolId, toolResult);
                return;
            } else if (toolResponse.toolName.equals("agent_sendMessage")) {
                LOGGER.info("Sending message on the channel. Message : {}", toolResponse.arguments.toPrettyString());
                ObjectNode toolResult = JsonUtils.MAPPER.createObjectNode();
                ObjectNode r = flowRunner.sendResponse(toolResponse.arguments.get("message").asText());
                toolResult.put("status","Success");
                toolResult.set("toolResult",r);
                flowRunner.addToolResult(toolResponse.toolName, toolResponse.toolId, toolResult);
                flowRunner.currentStepCompleted(true);
                return;
            } else if (toolResponse.toolName.equals("agent_endFlow")) {
                String result = toolResponse.arguments.get("result").asText();
                flowRunner.setFlowResult(result);
                ObjectNode toolResult = JsonUtils.MAPPER.createObjectNode();
                toolResult.put("status", "Success");
                toolResult.put("flowResult", result);
                flowRunner.addToolResult(toolResponse.toolName, toolResponse.toolId, toolResult);
                flowRunner.currentStepCompleted(true);
                return;
            }
            ToolRunner runner = new ToolRunner(toolResponse.toolName, toolResponse.arguments);

            long startTime = System.currentTimeMillis();
            ToolCallResponse toolResult = runner.run(flowRunner.getAgentInstance());
            long endTime = System.currentTimeMillis();
            stepRunInstance.addLogInfo("toolRunDuration", (endTime - startTime));
            flowRunner.addToolResult(toolResponse.toolName, toolResponse.toolId, toolResult.getResultJSON());
            // Take the next step from current tool run step, and proceed.
            flowRunner.currentStepCompleted(true);
        } else {
            LOGGER.warn("Invalid response. '{}'. Content '{}]", response.getClass().getName() ,
                    response.assistantMessage.toPrettyString());
            flowRunner.currentStepCompleted(true);
        }
    }

}
