package com.flowstack.flow;

import com.fasterxml.jackson.databind.JsonNode;

public class StepResult {
    
    JsonNode assistaneMessage = null;
    public StepResult(JsonNode assistaneMessage) {
        this.assistaneMessage = assistaneMessage;
    }
}
