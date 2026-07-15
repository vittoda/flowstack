package com.flowstack.models;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class ModelTextResponse extends ModelResponse {

    public String responseData = null;

    public ModelTextResponse(ObjectNode assistantMessage, String text) {
        super(assistantMessage);
        this.responseData = text;
    }
}