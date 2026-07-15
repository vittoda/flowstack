package com.flowstack.channels;

import java.util.HashMap;
import java.util.ServiceLoader;

import com.flowstack.channels.base.CommChannelBase;
import com.flowstack.channels.base.CommChannelInstance;


public class ChannelRegistry {

    private static final HashMap<String,CommChannelBase> REGISTERED_CHANNELS = new HashMap<>();

    public static void loadChannels() {
        // ServiceLoader looks for files in META-INF/services
        ServiceLoader<CommChannelBase> loader = ServiceLoader.load(CommChannelBase.class);
        System.out.println("Loading Channels");
        for (CommChannelBase channel : loader) {
            String key = channel.getKey();
            REGISTERED_CHANNELS.put(key,channel);
        }
    }

    public static CommChannelInstance getInstanceFor(String key) {
        CommChannelBase channel = REGISTERED_CHANNELS.get(key);
        if(channel == null) {
            return null;
        }

        return channel.createInstance();
    }

    public static String getNameFor(String key) {
        return REGISTERED_CHANNELS.get(key).getName();
    }

}