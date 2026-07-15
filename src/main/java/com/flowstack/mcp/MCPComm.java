package com.flowstack.mcp;

import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class MCPComm {
    
    public abstract ObjectNode sendRequest(ObjectNode request) throws MCPException;
    public abstract void close() throws MCPException;
}
