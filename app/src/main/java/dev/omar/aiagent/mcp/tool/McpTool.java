package dev.omar.aiagent.mcp.tool;
import org.json.JSONObject;

public interface McpTool {
    String getName();
    String getDescription();
    JSONObject getParametersSchema();
    String execute(JSONObject args);
}