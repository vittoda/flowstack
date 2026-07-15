package com.flowstack.cli;

import com.flowstack.channels.base.MessageContext;

public class CliMessageContext extends MessageContext {

    public String clientId = null;

    public CliMessageContext(String clientId) {
        this.clientId = clientId;
    }

}