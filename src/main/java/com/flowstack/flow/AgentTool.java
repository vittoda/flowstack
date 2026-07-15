package com.flowstack.flow;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flowstack.JsonUtils;
import com.flowstack.mcp.ToolCallResponse;


public class AgentTool {

    private static ObjectNode _mAllFunctionsDef = null;

    public ToolCallResponse runTool(String toolName, ObjectNode arguments) {
        return null;
    }

    public static ObjectNode getDefinitionForFunctionCalling(String toolName) {
        if (_mAllFunctionsDef == null) {
            try {
                InputStream is = AgentTool.class
                        .getClassLoader()
                        .getResourceAsStream("agentTools.json");

                String jsonText = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                _mAllFunctionsDef =  (ObjectNode) JsonUtils.MAPPER.readTree(jsonText);

            } catch (Exception e) {
                e.printStackTrace();
                // Ignore. This should not happen.
            }
        }

        return (ObjectNode)_mAllFunctionsDef.get(toolName);

    }

    public static List<String> getAllToolNames() {

        List<String> ret = new LinkedList<>();

        ret.add("agent_runStep");
        ret.add("agent_runErrorStep");
        ret.add("agent_sendMessage");
         ret.add("agent_endFlow");

        return ret;
    }


}
