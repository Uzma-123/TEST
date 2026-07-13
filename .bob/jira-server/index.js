#!/usr/bin/env node
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";

const JIRA_URL = process.env.JIRA_INSTANCE_URL;
const EMAIL = process.env.JIRA_USER_EMAIL;
const API_KEY = process.env.JIRA_API_KEY;
const AUTH = Buffer.from(`${EMAIL}:${API_KEY}`).toString("base64");

const headers = {
  Authorization: `Basic ${AUTH}`,
  "Content-Type": "application/json",
  Accept: "application/json",
};

async function jiraFetch(path, options = {}) {
  const res = await fetch(`${JIRA_URL}/rest/api/2${path}`, {
    headers,
    ...options,
  });
  const text = await res.text();
  if (!res.ok) throw new Error(`Jira API Error: ${res.status} ${res.statusText} — ${text}`);
  return text ? JSON.parse(text) : {};
}

const server = new McpServer({ name: "jira-full", version: "1.0.0" });

// --- get_issue ---
server.registerTool(
  "get_issue",
  {
    description: "Retrieve details about a Jira issue by its key (e.g. SAM-2).",
    inputSchema: {
      issueIdOrKey: z.string().describe("Issue key or ID, e.g. SAM-2"),
    },
  },
  async ({ issueIdOrKey }) => {
    const data = await jiraFetch(`/issue/${issueIdOrKey}`);
    return { content: [{ type: "text", text: JSON.stringify(data, null, 2) }] };
  }
);

// --- jql_search ---
server.registerTool(
  "jql_search",
  {
    description: "Search Jira issues using JQL.",
    inputSchema: {
      jql: z.string().describe("JQL query string"),
      maxResults: z.number().optional().default(10),
    },
  },
  async ({ jql, maxResults }) => {
    const data = await jiraFetch(
      `/search?jql=${encodeURIComponent(jql)}&maxResults=${maxResults}`
    );
    return { content: [{ type: "text", text: JSON.stringify(data, null, 2) }] };
  }
);

// --- get_transitions ---
server.registerTool(
  "get_transitions",
  {
    description: "Get available status transitions for a Jira issue.",
    inputSchema: { issueIdOrKey: z.string().describe("Issue key, e.g. SAM-2") },
  },
  async ({ issueIdOrKey }) => {
    const data = await jiraFetch(`/issue/${issueIdOrKey}/transitions`);
    return { content: [{ type: "text", text: JSON.stringify(data, null, 2) }] };
  }
);

// --- transition_issue ---
server.registerTool(
  "transition_issue",
  {
    description:
      "Transition a Jira issue to a new status using a transition ID. Use get_transitions first to find valid transition IDs.",
    inputSchema: {
      issueIdOrKey: z.string().describe("Issue key, e.g. SAM-2"),
      transitionId: z.string().describe("Transition ID from get_transitions"),
    },
  },
  async ({ issueIdOrKey, transitionId }) => {
    await jiraFetch(`/issue/${issueIdOrKey}/transitions`, {
      method: "POST",
      body: JSON.stringify({ transition: { id: transitionId } }),
    });
    return {
      content: [{ type: "text", text: `Issue ${issueIdOrKey} transitioned successfully.` }],
    };
  }
);

// --- update_issue ---
server.registerTool(
  "update_issue",
  {
    description: "Update fields of a Jira issue (e.g. summary, description, priority).",
    inputSchema: {
      issueIdOrKey: z.string().describe("Issue key, e.g. SAM-2"),
      fields: z.record(z.any()).describe("Fields to update as a JSON object"),
    },
  },
  async ({ issueIdOrKey, fields }) => {
    await jiraFetch(`/issue/${issueIdOrKey}`, {
      method: "PUT",
      body: JSON.stringify({ fields }),
    });
    return {
      content: [{ type: "text", text: `Issue ${issueIdOrKey} updated successfully.` }],
    };
  }
);

const transport = new StdioServerTransport();
await server.connect(transport);
