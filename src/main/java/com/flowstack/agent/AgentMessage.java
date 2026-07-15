package com.flowstack.agent;

import com.flowstack.flow.FlowRunner;

public class AgentMessage {

    String prompt = null;
    FlowRunner triggerringFlow = null;

    public AgentMessage(String prompt, FlowRunner triggerringFlow) {
        this.prompt = prompt;
        this.triggerringFlow = triggerringFlow;
    }
    
}
