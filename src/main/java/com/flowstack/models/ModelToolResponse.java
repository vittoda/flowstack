package com.flowstack.models;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class ModelToolResponse extends ModelResponse {

    public ObjectNode arguments = null;
    public String toolName = null;
    public String toolId = null;


    public ModelToolResponse(ObjectNode assistantMessage, String toolName, String toolId, ObjectNode arguments) {
        super(assistantMessage);
        this.toolName = toolName;
        this.arguments = arguments;
        this.toolId = toolId;
    }
    
}
