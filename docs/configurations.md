# FlowStack Configurations

This document covers configurations for FlowStack server. Schema in the following sections will use `Typescript` format to define the type.

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

### Details
| Field Name | Type | Mandatory | Description |
| :--- | :---: | :---: | :--- |
| `name` | String | Yes | A unique identifier for the server. Agents will use this value to identify the MCP server it needs to use |
| `category` | String | Yes | The functional classification. At the moment used for UI rendering. Supported values are `DB`, `System` and `Saas` |
| `command` | String | Yes | Command that will be executed by the FlowStack server to start the MCP server. You can use system property `mcp.base`, as part of the command|
| `connection` | Object | Yes | At the moment only *stdio* mode is available for MCP servers. So, the *type* attribute will have `stdio` as the value

Here is a sample for SQLite MCP server

```
{
    "name": "sqlite",
    "category" : "System",
    "command" : "/usr/bin/java -DdataPath=/tmp -jar ${mcp.base}/sqlite/build/libs/sqlite-1.0.1-all.jar ",
    "connection": {
        "type": "stdio"
    }
}
```

## Agent configuration.
Agent configuration defines agent instances, which are combination of prompts, MCP servers, channels. Agent configuraiton will have similar top level structure as MCP servers.

```
{
    agents : [ 
        <Agent definition>
    ]
}
```

Each of the agent definition has the following schema

```
{
    "name": string,
    "id": string,
    "description": string,
    "context": {file:string} | string,
    "mcpServers": string[],
    "channels": string[],
    "variables": Variable[],
    "hitlConfig" : HITLConfig[]
}
```

### Agent Definition
| Field Name | Type | Mandatory | Description |
| :--- | :---: | :---: | :--- |
| `name` | String | Yes | A unique display name for the Agent. |
| `id` | String | Yes | Unique identifier for the agent. Within FlowStack or for multi-agent configuration, this will be the identifier used. Reccomended characeters are A-Z,a-z,0-9,_. |
| `description` | String | Yes | A free form text describing the agent. This information will be used by other agents to determine high level capabilities of this agent. |
| `context` | Object | Yes | Domain context for the agent, which provides system context for the LLM for this agent. Value can be a context. If the context is too large, save it in a file and specify the `file` attribute for context. |
| `mcpServers` | String[] | Yes | An array of MCP server identifiers that this agent will use. |
| `agents` | String[] | No | An array of agent identifiers this agent can communicate to in a multi-agent scneario. |
| `channels` | String[] | No | An array of channels the agent will listen to and respond to. At the moment only `slack` is supported as channel. |
| `variables` | Variable[] | No | List of variables that the agent prompts will use, which will be replaced with values during runtime. See the [Variables section](#variables) |
| `hitlConfig` | Object | No |Human in the loop configuration. See [HITLConfig Section](#hitlconfig-human-in-the-loop) |


### Variables
Variables can be included in system context or user prompts for the agent. They are replaced at runtime before sending the prompt to LLM.

```
{
    "name": string,
    "label": string,
    "description": string,
}
```

| Field Name | Type | Mandatory | Description |
| :--- | :---: | :---: | :--- |
| `name` | String | Yes | Name of the variable. Reccomended characeters are A-Z,a-z,0-9,_.|
| `label` | String | Yes | A display name for the variable. Used primarily in UI|
| `description` | String | Yes | Detailed description of the variable. Used in UI|

### HITLConfig (Human In The Loop)
Human in the loop configuration provides definitions for setup to handle human in the loop events from the agent. Here is the schema for the definition. Basically HITLConfig provides list of targets.

```
{
    "targets" : HITLTarget[]
}
```

### HITLTarget
```
{
    "type" : "webhook" | "event";
    "instance" : string;
}
```
| Field Name | Type | Mandatory | Description |
| :--- | :---: | :---: | :--- |
| `type` | String | Yes | Supports the following options.<br> . `event` : An event will be triggered on the subscribed channels for this target type<br> . `webhook` : A POST request will be triggered on the provided webhook URL. |
| `instance` | String | Only for `webhook` type | URL of the webhook|

Here is a sample agent definition for an agent with Human In The Loop configuration

```
{
    "name": "SQLite",
    "id": "sqlite",
    "description": "SQLite Integration",
    "context": {
        "file": "sqliteContext.txt"
    },
    "hitlConfig" :{
        "targets" : [
            {
                "type" : "webhook",
                "instance" : "http://localhost:8090/hil"
            },
            {
                "type" : "event"
            }
        ]
    },
    "mcpServers": ["sqlite"]
}
```

Following samle agent configuration uses variables.

```
{
    "name": "File system and GMail",
    "id": "fs_and_Gmail",
    "description": "File system and Gmail integration",
    "context": {
        "file": "fileSystemAndGmailSystemPrompt.txt"
    },
    "mcpServers": ["filesystem","gmail"]
    "variables": [
        {
            "name": "fullName",
            "label": "Full Name",
            "description": "Full name of the recipient"
        },
        {
            "name": "subject",
            "label": "Email Subject",
            "description": "Subject for the email"
        },
        {
            "name": "email",
            "label": "Email address",
            "description": "Email address recipient"
        }
    ]
}
```