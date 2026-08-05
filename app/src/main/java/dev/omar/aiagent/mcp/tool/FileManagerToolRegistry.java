package dev.omar.aiagent.mcp.tool;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

import dev.omar.aiagent.mcp.service.file.FileOperationsService;
import dev.omar.aiagent.mcp.tool.file.AppendToFileTool;
import dev.omar.aiagent.mcp.tool.file.CopyFileTool;
import dev.omar.aiagent.mcp.tool.file.CreateFileTool;
import dev.omar.aiagent.mcp.tool.file.DeleteFileTool;
import dev.omar.aiagent.mcp.tool.file.GetFileInfoTool;
import dev.omar.aiagent.mcp.tool.file.GetListFilesTool;
import dev.omar.aiagent.mcp.tool.file.MoveFileTool;
import dev.omar.aiagent.mcp.tool.file.ReadFileTool;
import dev.omar.aiagent.mcp.tool.file.RenameFileTool;
import dev.omar.aiagent.mcp.tool.file.SearchFileTool;

public class FileManagerToolRegistry {

    private final Map<String, McpTool> toolsMap = new HashMap<>();
    private final FileOperationsService fileService;

    public FileManagerToolRegistry(FileOperationsService fileService) {
        this.fileService = fileService;
        registerDefaultTools();
    }

    private void registerDefaultTools() {
        registerTool(new CopyFileTool(fileService));
        registerTool(new MoveFileTool(fileService));
        registerTool(new GetFileInfoTool(fileService));
        registerTool(new SearchFileTool(fileService));
        registerTool(new DeleteFileTool(fileService));
        registerTool(new AppendToFileTool(fileService));
        registerTool(new CreateFileTool(fileService));
        registerTool(new ReadFileTool(fileService));
        registerTool(new GetListFilesTool(fileService));
        registerTool(new RenameFileTool(fileService));
    }

    public void registerTool(McpTool tool) {
        toolsMap.put(tool.getName(), tool);
    }

    public McpTool getTool(String name) {
        return toolsMap.get(name);
    }
    /**
     * بناء صيغة الأدوات لملاءمة طلب Gemini API
     */
    public JSONArray getToolsDeclarationAsJson() {
        JSONArray functionDeclarations = new JSONArray();
        try {
            for (McpTool tool : toolsMap.values()) {
                JSONObject decl = new JSONObject();
                decl.put("name", tool.getName());
                decl.put("description", tool.getDescription());
                decl.put("parameters", tool.getParametersSchema());
                functionDeclarations.put(decl);
            }
        } catch (Exception ignored) {}

        JSONArray tools = new JSONArray();
        try {
            JSONObject container = new JSONObject();
            container.put("function_declarations", functionDeclarations);
            tools.put(container);
        } catch (Exception ignored) {}

        return tools;
    }
}
