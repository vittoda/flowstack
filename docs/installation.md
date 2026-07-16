# Installation

## Pre-requisites
* **Java 21** : Required for FlowStack, MCP Servers, and Channel.
* **Node.js (v18+)** : Required for the FlowStack UI (built with React).
* **Git** : Required to clone the repository.


## MCP Servers
All the MCP servers are part of single repository. Individual MCP servers are available in their respective folder from root directory.

### Step 1. Clone the github repo
Clone the [flowstack_mcp](https://github.com/vittoda/flowstack_mcp) repo from github.

`git clone git@github.com:vittoda/flowstack_mcp.git`

### Step 2. Build *mcp_base*

**mcp_base** is a common dependency for all the MCP servers. Build the module and publish it to local maven repository.

```
cd flowstack_mcp/mcp_base
./gradlew clean build publishToMavenLocal
```

### Step 3. Build *google_common*

**google_common** is used by MCP servers that connects to Google services

```
cd ../google_common
./gradlew clean build publishToMavenLocal
```


### Step 4. Build MCP Servers

Building and generating the executable JAR file is the same for each MCP server. Here is an example of how to build the AWS S3 MCP server:

```
cd ../aws-s3
./gradlew clean build shadowJar
```
## Channels

As of now only slack channel is available. Channels and its dependencies are available in a single repository.

### Step 1. Clone the github repo
Clone the [flowstack_channels](https://github.com/vittoda/flowstack_channels) repo from github.

`git clone git@github.com:vittoda/flowstack_channels.git`

### Step 2. Build *channels_base*

**channels_base** is a common dependency for channels that provides the common functionalities.

```
cd flowstack_channels/channels_base
./gradlew clean build publishToMavenLocal
```

### Step 3. Build *slack* Channel
As we have one channel implementation at the moment, let us build the same.
```
cd flowstack_channels/slack
./gradlew clean build publishToMavenLocal
```

## FlowStack

### Step 1. Clone the github repo
Clone the [flowstack](https://github.com/vittoda/flowstack) repo from github.

`git clone git@github.com:vittoda/flowstack.git`

### Step 2. Build the FlowStack server
Now it is time to build the FlowStack server. At this moment we will assume you have done the required configuration or use the configuration already avaiable.

```
cd flowstack
./gradlew clean build bootJar
```

## FlowStack UI

### Step 1. Clone the github repo
Clone the [flowstack_ui](https://github.com/vittoda/flowstack_ui) repo from github.

`git clone git@github.com:vittoda/flowstack_ui.git`

### Step 2. Setup the React JS project
FlwoStack UI is built using react-js. Once the repo is cloned, run the following command.

```
cd flowstack_ui
npm install
```
