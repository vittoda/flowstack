package com.flowstack.ws;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flowstack.JsonUtils;

public class ClientSession {

    String clientId = null;
    WebSocketSession websocketSession = null;

    private HashMap<String, String> _mSessionVariables = new HashMap<>();

    public ClientSession(String clientId, WebSocketSession websocketSession) {
        this.clientId = clientId;
        this.websocketSession = websocketSession;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setVar(String name, String value) {
        _mSessionVariables.put(name, value);
    }

    public String getVar(String name) {
        return _mSessionVariables.get(name);
    }

    public void clearVar(String name) {
        _mSessionVariables.remove(name);
    }

    public ObjectNode getAllVars() {
        ObjectNode items = JsonUtils.MAPPER.createObjectNode();
        for(String key : _mSessionVariables.keySet()) {
            items.put(key, _mSessionVariables.get(key));
        }

        return items;
    }

    public void sendMessageToClient(String message) {
        if (this.websocketSession != null && this.websocketSession.isOpen()) {
            try {
                this.websocketSession.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                System.err.println("Error sending message to client " + clientId + ": " + e.getMessage());
            }
        } else {
            System.out.println("Client " + clientId + " is not connected.");
        }
    }
    
}
