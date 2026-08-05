package dev.omar.aiagent.mcp.tool.file;

import dev.omar.aiagent.mcp.service.file.FileOperationsService;
import dev.omar.aiagent.mcp.tool.McpTool;

public abstract class BaseFileTool implements McpTool {
    protected final FileOperationsService fileService;

    public BaseFileTool(FileOperationsService fileService) {
        this.fileService = fileService;
    }

    public final FileOperationsService getFileService() {
        return fileService;
    }
}
