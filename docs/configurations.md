# Configurations

## MCP Server configuration.

MCP servers configuration file defines list of MCP servers available for the FlowStack instance. Here is the structure of configuration file

```
{
    mcpServers : [ 
        <MCP server configuration>
    ]
}
```

Each of the MCP server definition follows the following structure

```javascript
{
    "name": string,
    "category" : "DB" | "System" | "Saas",
    "command" : string,
    "connection": {
        "type": "stdio"
    }
}
```