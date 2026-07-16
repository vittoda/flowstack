package com.flowstack.models;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flowstack.JsonUtils;
import com.flowstack.Keys;
import com.flowstack.flow.FlowMemory;
import com.flowstack.metrics.MetricsDB;
import com.flowstack.metrics.MetricsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenAI extends ModelConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAI.class);

    private static HttpClient _mClient = null;

    private static final int THROTTLE_ERROR_RETRY_COUNT = 0;

    private String _mModelName = null;
    private double _mTemperature = 0.2;

    private boolean _mLogRequests = false;

    public OpenAI(String modelName) {
        _mModelName = modelName;
        _mLogRequests = System.getProperty("openAI.model.logRequests", "false").equals("true");
    }

    protected String getCred() {
        return Keys.getCred(getCredKey());
    }

    protected String getCredKey() {
        return "flowstack.openai";
    }

    protected boolean logRequests() {
        return _mLogRequests;
    }

    protected String getURL() {
        return "https://api.openai.com/v1/chat/completions";
    }

    @Override
    public ModelResponse sendRequest(FlowMemory memory,
            List<String> additionalSystemMessages,
            List<String> userMessages, List<ToolMessage> tools, boolean jsonResponse) throws ModelException

    {
        String key = getCred();
        if (key == null) {
            throw new ModelException(
                    "Access key not found. Get the access key and add it at ~/.fskeys. Use the key  '" + getCredKey()
                            + "'");
        }

        ObjectNode request = JsonUtils.MAPPER.createObjectNode();
        request.put("model", _mModelName);

        // Let us add system messages.
        ArrayNode messages = JsonUtils.MAPPER.createArrayNode();
        request.set("messages", messages);

        // Add memory
        List<ModelMessage> modelMessages = memory.getAllMessages();
        for (ModelMessage mm : modelMessages) {
            if (mm instanceof ModelSystemMessage) {
                ModelSystemMessage ms = (ModelSystemMessage) mm;
                ObjectNode msg = JsonUtils.MAPPER.createObjectNode();
                msg.put("role", "system");
                msg.put("content", ms.content);
                messages.add(msg);
            } else if (mm instanceof ModelAssistantMessage) {
                ModelAssistantMessage ma = (ModelAssistantMessage) mm;
                ObjectNode msg = JsonUtils.MAPPER.createObjectNode();
                msg.put("role", "assistant");
                if (ma.toolCalls != null) {
                    msg.set("tool_calls", ma.toolCalls);
                } else {
                    msg.put("content", ma.content);
                }
                messages.add(msg);
            } else if (mm instanceof ToolResponseMessage) {
                ToolResponseMessage m = (ToolResponseMessage) mm;
                ObjectNode msg = JsonUtils.MAPPER.createObjectNode();
                msg.put("role", "tool");
                msg.put("tool_call_id", m.toolCallId);
                msg.put("name", m.name);
                msg.put("content", m.result);
                messages.add(msg);
            } else if (mm instanceof ModelUserMessage) {
                ModelUserMessage ms = (ModelUserMessage) mm;
                for (String um : ms.content) {
                    ObjectNode msg = JsonUtils.MAPPER.createObjectNode();
                    msg.put("role", "user");
                    msg.put("content", um);
                    messages.add(msg);
                }
            }
        }

        if (additionalSystemMessages != null) {
            for (String m : additionalSystemMessages) {
                ObjectNode msg = JsonUtils.MAPPER.createObjectNode();
                msg.put("role", "system");
                msg.put("content", m);
                messages.add(msg);
            }
        }

        // User messages
        if (userMessages != null) {
            for (String m : userMessages) {
                ObjectNode msg = JsonUtils.MAPPER.createObjectNode();
                msg.put("role", "user");
                msg.put("content", m);
                messages.add(msg);
            }
        }

        // Add tools.
        if (tools != null && tools.size() > 0) {
            ArrayNode toolsArray = JsonUtils.MAPPER.createArrayNode();
            for (ToolMessage t : tools) {
                ObjectNode function = JsonUtils.MAPPER.createObjectNode();
                function.put("type", "function");
                function.set("function", t.toolDefinition);
                toolsArray.add(function);
            }
            request.set("tools", toolsArray);
        }

        request.put("temperature", _mTemperature);
        // request.put("max_tokens", _mMaxTokens);
        if (jsonResponse) {
            ObjectNode responseType = JsonUtils.MAPPER.createObjectNode();
            responseType.put("type", "json_object");
            request.set("response_format", responseType);
        }

        if (_mClient == null) {
            _mClient = HttpClient.newHttpClient();
        }
        if (_mLogRequests) {
            LOGGER.info("================================= REQUEST ======================");
            LOGGER.info(request.toPrettyString());
            LOGGER.info("================================================================");
        }
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(getURL()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getCred())
                .POST(HttpRequest.BodyPublishers.ofString(request.toString()))
                .build();

        int retryInterval = 5000; // 5 seconds
        int timerMultiplier = 1;
        try {
            Date requestStartTime = new Date();
            HttpResponse<String> response = _mClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString());
            Date endTime = new Date();
            int statusCode = response.statusCode();
            try {
                MetricsDB.addRequestMetricForModel(_mModelName, requestStartTime, endTime, statusCode);
            } catch (MetricsException e) {
                LOGGER.warn("Error saving token metric. " , e.getMessage());
            }

            if (statusCode == 429 || statusCode == 503) {
                int retryCount = 0;
                while (retryCount < THROTTLE_ERROR_RETRY_COUNT && (statusCode == 429 || statusCode == 503)) {
                    Thread.sleep(retryInterval);
                    requestStartTime = new Date();
                    response = _mClient.send(
                            httpRequest,
                            HttpResponse.BodyHandlers.ofString());
                    endTime = new Date();
                    statusCode = response.statusCode();
                    try {
                        MetricsDB.addRequestMetricForModel(_mModelName, requestStartTime, endTime, statusCode);
                    } catch (MetricsException e) {
                        LOGGER.warn("Error saving token metric. " , e.getMessage());
                    }
                    retryCount++;
                    timerMultiplier++;
                    retryInterval = 5000 * timerMultiplier;
                }
            }

            if (statusCode == 403 || statusCode == 401) {
                throw new ModelNonRecoverableException("Model requests unthentication error. Non-recoverable.");
            }
            if (statusCode == 429) {
                throw new ModelNonRecoverableException("Model requests are throttled. Non-recoverable.");
            }
            if (statusCode != 200) {
                throw new ModelException(
                        "API responded with status code '" + statusCode + "'.\n Message : " + response.body());
            }

            // Request success. Now add additional system messages and user messages to
            // memory.
            if (additionalSystemMessages != null && additionalSystemMessages.size() > 0) {
                for (String s : additionalSystemMessages) {
                    memory.addContent(new ModelSystemMessage(s));
                }
            }

            if (userMessages != null && userMessages.size() > 0) {
                memory.addContent(new ModelUserMessage(userMessages));
            }

            String responseBody = response.body();
            return handleResponse(memory, (ObjectNode) JsonUtils.MAPPER.readTree(responseBody), jsonResponse);
        } catch (IOException | InterruptedException e) {
            throw new ModelException(e);
        }

    }

    private ModelResponse handleResponse(FlowMemory memory, ObjectNode response, boolean jsonRespnse)
            throws ModelException {

        if (response.has("usage")) {
            ObjectNode usageMetadata = (ObjectNode) response.get("usage");
            int outputTokenSize = usageMetadata.get("completion_tokens").asInt();
            int inputTokenSize = usageMetadata.get("prompt_tokens").asInt();

            try {
                MetricsDB.addTokenMetric(_mModelName, inputTokenSize, outputTokenSize);
            } catch (Exception e) {
                LOGGER.warn("Error saving token metric. " , e.getMessage());
            }
        }
        if (!response.has("choices")) {
            return null;
        }

        if (_mLogRequests) {
            LOGGER.info("================================ RESPONSE ======================");
            LOGGER.info(response.toPrettyString());
            LOGGER.info("================================================================");
        }


        ArrayNode choices = (ArrayNode) response.get("choices");
        ObjectNode choice = (ObjectNode) choices.get(0); // Asume I will get at least one.
        ObjectNode message = (ObjectNode) choice.get("message");
        // Tool calls gets the preference

        if (message.has("tool_calls") && (!message.get("tool_calls").isNull())) {
            ArrayNode tool_calls = (ArrayNode) message.get("tool_calls");

            // Push it to the memory.
            memory.addContent(new ModelAssistantMessage(null, tool_calls));

            ObjectNode tool = (ObjectNode) tool_calls.get(0);
            ObjectNode function = (ObjectNode) tool.get("function");
            String name = function.get("name").asText();
            String id = tool.get("id").asText();
            JsonNode argumentsNode = function.get("arguments");

            ObjectNode arguments;
            if (argumentsNode.isObject()) {
                arguments = (ObjectNode) argumentsNode;
            } else {
                try {
                    arguments = (ObjectNode) JsonUtils.MAPPER.readTree(argumentsNode.asText());
                } catch (JsonProcessingException e) {
                    throw new ModelException(e);
                }
            }
            return new ModelToolResponse(message, name, id, arguments);
        } else if (message.has("content") && (!message.get("content").isNull())) {
            String m = message.get("content").asText();
            memory.addContent(new ModelAssistantMessage(m, null));
            if (jsonRespnse) {
                try {
                    ObjectNode contentJSON = (ObjectNode) JsonUtils.MAPPER.readTree(m);
                    return new ModelJSONResponse(message, contentJSON);
                } catch (JsonProcessingException e) {
                    throw new ModelException(e);
                }
            } 

            return new ModelTextResponse(message, m);

        } else {
            memory.addContent(new ModelAssistantMessage(message.toString(), null));
            return new ModelResponse(message);
        }
    }

}
