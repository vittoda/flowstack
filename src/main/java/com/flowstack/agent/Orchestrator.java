package com.flowstack.agent;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Orchestrator {

    public static Orchestrator INSTANCE = new Orchestrator();

    private HashMap<String, LinkedBlockingQueue<AgentMessage>> _mAgentQueues = new HashMap<>();

    private Orchestrator() {

    }

    public void registerAgent(String agentKey) {
        _mAgentQueues.put(agentKey, new LinkedBlockingQueue<AgentMessage>());
    }

    public void addMessage(String agentKey, AgentMessage am) {
        System.err.println("Trying to add message for agent '"+agentKey+"'. It "+(_mAgentQueues.containsKey(agentKey) ? "exists" : "does not exist."));
        _mAgentQueues.get(agentKey).add(am);
    }

    public AgentMessage poll(String agentKey) throws OrchestratorException {
        try {
            return _mAgentQueues.get(agentKey).take();
        } catch (InterruptedException e) {
            throw new OrchestratorException(e);
        }
    }

}
