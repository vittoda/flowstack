package com.flowstack.cli;

import com.flowstack.channels.base.CommChannelBase;
import com.flowstack.channels.base.CommChannelInstance;

public class CliChannel implements CommChannelBase {

    public static final CliChannel INSTANCE = new CliChannel();
    

    private static CommChannelInstance CLI_CHANNEL_INSTANCE = null;


    private CliChannel() {

    }

    @Override
    public String getName() {
        return "CLI";
    }

    @Override
    public String getKey() {
        return "cli";
    }

    @Override
    public CommChannelInstance createInstance() {
        if (CLI_CHANNEL_INSTANCE == null) {
            synchronized (this) {
                if (CLI_CHANNEL_INSTANCE == null) {
                    CLI_CHANNEL_INSTANCE = new CliChannelInstance();
                }
            }
        }
        return CLI_CHANNEL_INSTANCE;
    }

    

}
