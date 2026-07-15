package com.flowstack.models;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class ModelJSONResponse extends ModelResponse {

    public ObjectNode responseData = null;

    public ModelJSONResponse(ObjectNode assistantMessage, ObjectNode obj) {
        super(assistantMessage);
        this.responseData = obj;
    }
    
}
