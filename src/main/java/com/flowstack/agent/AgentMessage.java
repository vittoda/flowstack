package com.flowstack.agent;

import java.util.List;

import com.flowstack.flow.FlowRunner;

public class AgentMessage {

    String prompt = null;
    List<String> additionalContext = null;
    FlowRunner triggerringFlow = null;

    public AgentMessage(String prompt,  List<String> additionalContext, FlowRunner triggerringFlow) {
        this.prompt = prompt;
        this.triggerringFlow = triggerringFlow;
        this.additionalContext = additionalContext;
    }
    
}
