package com.flowstack.models;

import java.util.List;

import com.flowstack.flow.FlowMemory;


public abstract class ModelConnection {

     public abstract ModelResponse sendRequest(FlowMemory memory, 
          List<String> additionalSystemMessages, 
          List<String> userMessages, List<ToolMessage> tools, boolean jsonRespnse) throws ModelException;

    
}
