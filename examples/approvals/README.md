# Approval workflow

This example will demonstrate following capabilities of FlowStack server.

1. Work with multiple MCP servers
2. Multiple workflow in the same agent
3. Multiple input channels (CLI and Gmail)

This agent manages the approval process for a team event using the following workflow:

1. **Event Submission:** The user submits a prompt containing the event details (date, time, and location).
2. **Approval Request:** The agent sends an approval request email to the reviewer.
3. **Approver Response:** The reviewer approves or rejects the request directly via email.
4. **Calendar & Database Update:** Once the agent receives the approval email, it adds the event to the calendar and updates the event's lifecycle status in a SQLite database.

Following MCP servers are used
* SQLite
* LocationIQ
* GMail
* Google Calender

## Instructions
* Ensure that the MCP servers are built
* Ensure you have credentials for Gmail, Google Calender generated and placed appropriately. Follow the instructions at this location, 