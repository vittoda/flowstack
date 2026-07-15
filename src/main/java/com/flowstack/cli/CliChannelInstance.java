package com.flowstack.cli;

import java.util.Date;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flowstack.JsonUtils;
import com.flowstack.agent.AgentCliCommMessageHandler;
import com.flowstack.channels.base.CommChannelException;
import com.flowstack.channels.base.CommChannelInstance;
import com.flowstack.channels.base.MessageContext;
import com.flowstack.channels.base.OnMessageHandler;
import com.flowstack.channels.base.OutputMessage;
import com.flowstack.ws.WSClientSessionCache;

public class CliChannelInstance implements CommChannelInstance {

    private HashMap<String, AgentCliCommMessageHandler> _mMessageHandlers = new HashMap<>();

    public void messageReceived(String clientId, String agentId, String message, ObjectNode options) {
        CliInputMessage ic = new CliInputMessage(message, clientId, options);
        AgentCliCommMessageHandler handler = _mMessageHandlers.get(agentId);
        if (handler != null) {
            OutputMessage om = handler.onMessageReceived(ic);
            if(om != null) {
                WSClientSessionCache.get(clientId).sendMessageToClient(om.getText());
            }
        }
    }

    @Override
    public void sendMessage(OutputMessage msg) throws CommChannelException {
        // This will be sent as event. Because any response to cli should be sent on the
        // same flow.
        ObjectNode on = null;
        String m = msg.getText();
        if (m.charAt(0) != '{') {
            on = JsonUtils.MAPPER.createObjectNode();
            on.put("event", true);
            on.put("message", m);
        } else {
            try {
                on = (ObjectNode) JsonUtils.MAPPER.readTree(m);
                on.put("event", true);
            } catch (JsonProcessingException e) {
                on = JsonUtils.MAPPER.createObjectNode();
                on.put("event", true);
                on.put("message", m);
            }
        }
        on.put("timestamp", new Date().getTime());
        CliMessageContext ctx = (CliMessageContext) msg.getContext();
        WSClientSessionCache.get(ctx.clientId).sendMessageToClient(on.toString());
    }

    @Override
    public void registerOnMessageHandler(OnMessageHandler handler) {
        if (handler instanceof AgentCliCommMessageHandler) {
            AgentCliCommMessageHandler ah = (AgentCliCommMessageHandler) handler;
            _mMessageHandlers.put(ah.agent.id, ah);
            return;
        }
        throw new UnsupportedOperationException(
                "CLI channel allows only AgentCliCommMessageHandler instances to be registered");
    }

    @Override
    public void initialize() throws CommChannelException {
    }


    @Override
    public void getConfirmationResponse(MessageContext context, String message, String responseKey, String requestId) throws CommChannelException  {
        CliMessageContext ctx = (CliMessageContext) context;
        ObjectNode messageObject = JsonUtils.MAPPER.createObjectNode();
        messageObject.put("type" , "userConfirmation");
        messageObject.put("message", message);
        messageObject.put("responseKey", responseKey);
        messageObject.put("requestId", requestId);

        WSClientSessionCache.get(ctx.clientId).sendMessageToClient(messageObject.toString());
    }

    

}
