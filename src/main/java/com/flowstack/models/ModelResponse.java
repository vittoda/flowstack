package com.flowstack.models;

import com.fasterxml.jackson.databind.node.ObjectNode;

/*
This is just a wrapper class. 
 */
public class ModelResponse {

    public ObjectNode assistantMessage = null;

    public ModelResponse(ObjectNode assistantMessage) {
        this.assistantMessage = assistantMessage;
    }
    
}
