package com.flowstack.models;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class ToolMessage {

    public ObjectNode toolDefinition = null;

    public ToolMessage(ObjectNode tool) {
        this.toolDefinition = tool;
    }
    
}
