package com.flowstack.mcp;

import java.util.UUID;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flowstack.JsonUtils;

public class MCPServerInstance {

    private MCPServer _mServerDef = null;
    private MCPComm _mConnection = null;

    public MCPServerInstance(MCPServer serverDef, MCPComm conn) {
        _mServerDef = serverDef;
        _mConnection = conn;
    }

    public MCPServer getServerDef() {
        return _mServerDef;
    }

    public void initialize() throws MCPException {

        // Prepare the request
        ObjectNode request = JsonUtils.MAPPER.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", UUID.randomUUID().toString());
        request.put("method", "initialize");

        ObjectNode result = _mConnection.sendRequest(request);
        result = (ObjectNode)result.get("result");
    }

    public ToolCallResponse runTool(String toolName, ObjectNode arguments) throws MCPException {
        return _runCommand(toolName, arguments);
    }

    private ToolCallResponse _runCommand(String toolName, ObjectNode arguments) throws MCPException {
        MCPTool tool = _mServerDef.getToolByName(toolName);
        ObjectNode request = JsonUtils.MAPPER.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", UUID.randomUUID().toString());
        request.put("method", "tools/call");
        ObjectNode params = JsonUtils.MAPPER.createObjectNode();
        request.set("params", params);
        params.put("name", tool.name);
        params.set("arguments", arguments);

        ObjectNode result = _mConnection.sendRequest(request);
        ToolCallResponse tcr = ToolCallResponse.fromResponse(result);
        return tcr;

    }

}
