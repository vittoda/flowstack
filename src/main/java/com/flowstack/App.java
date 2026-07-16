package com.flowstack;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.flowstack.agent.AgentRegistry;
import com.flowstack.channels.ChannelRegistry;
import com.flowstack.cli.AgentCliCommand;
import com.flowstack.cli.CliCommandRegistry;
import com.flowstack.cli.HelpCommand;
import com.flowstack.cli.ModelCommand;
import com.flowstack.cli.SessionsCliCommand;
import com.flowstack.flow.FlowExecutionQueue;
import com.flowstack.mcp.MCPRegistry;
import com.flowstack.metrics.MetricsDB;
import com.flowstack.models.ModelConnectionRegistry;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        try {

            String mcpBase = System.getProperty("mcp.base", null);
            if(mcpBase == null) {
                mcpBase = System.getProperty("user.home")+"/projects/agent/mcp";
                 System.setProperty("mcp.base", mcpBase);
            }
           

            String mcpConfigFile = System.getProperty("fs.mcpConfigFile");
            if(mcpConfigFile == null) {
                mcpConfigFile = "."+File.separator+"mcpServers.json";
            }

            if(!Files.exists(Paths.get(mcpConfigFile))) {
                System.err.println("[ERROR] MCP config file is not defined. Use system property 'fs.mcpConfigFile' to set the MCP config file, or, save the config file as 'mcpServers.json' in current folder.");
                return;
            }

            String agentsConfigFile = System.getProperty("fs.agentsConfigFile");
            if(agentsConfigFile == null) {
                agentsConfigFile = "."+File.separator+"agents.json";
            }

            if(!Files.exists(Paths.get(agentsConfigFile))) {
                System.err.println("[ERROR] Agent config file is not defined. Use system property 'fs.agentsConfigFile' to set the Agents config file, or, save the config file as 'agents.json' in current folder.");
                return;
            }


            // Load Keys
            Keys.loadKeys();

            ModelConnectionRegistry.initialize();
            MetricsDB.initialize();

            // Load the MCP servers and discover the tools.
            System.out.println("Loading MCP config file '"+mcpConfigFile+"'");
            MCPRegistry.initialize(mcpConfigFile);

            FlowExecutionQueue.INSTANCE.startConsumer();
            ChannelRegistry.loadChannels();
            SpringApplication.run(App.class, args);

            System.out.println("Loading Agent config file '"+agentsConfigFile+"'");
            AgentRegistry.loadFromFile(agentsConfigFile);

            // Register commands
            CliCommandRegistry.addCommand(new AgentCliCommand());
            CliCommandRegistry.addCommand(new SessionsCliCommand());
            CliCommandRegistry.addCommand(new HelpCommand());
            CliCommandRegistry.addCommand(new ModelCommand());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
