package com.flowstack.api.channel;

import com.flowstack.channels.base.CommChannelException;
import com.flowstack.channels.base.CommChannelInstance;
import com.flowstack.channels.base.MessageContext;
import com.flowstack.channels.base.OnMessageHandler;
import com.flowstack.channels.base.OutputMessage;

public class RestChannelInstance implements CommChannelInstance {

    private OnMessageHandler _onMessageHandler = null;

    @Override
    public void sendMessage(OutputMessage msg) throws CommChannelException {
        throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
    }

    @Override
    public void registerOnMessageHandler(OnMessageHandler handler) {
        _onMessageHandler = handler;
    }

    @Override
    public void initialize() throws CommChannelException {
        //There is nothing to initialize
    }

    @Override
    public void getConfirmationResponse(MessageContext context, String message, String respnseKey, String requestId)
            throws CommChannelException {
        throw new UnsupportedOperationException("Unimplemented method 'getConfirmationResponse'");
    }

    public OutputMessage onEventMessageReceived(String message) {
        return _onMessageHandler.onMessageReceived(new RestInputMessage(message));
    }
    
}
