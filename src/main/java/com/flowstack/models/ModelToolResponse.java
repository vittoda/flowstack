package com.flowstack.models;

import com.fasterxml.jackson.databind.JsonNode;

public class ModelToolResponse extends ModelResponse {

    public JsonNode arguments = null;
    public String toolName = null;
    public String toolId = null;


    public ModelToolResponse(JsonNode assistantMessage, String toolName, String toolId, JsonNode arguments) {
        super(assistantMessage);
        this.toolName = toolName;
        this.arguments = arguments;
        this.toolId = toolId;
    }
    
}
