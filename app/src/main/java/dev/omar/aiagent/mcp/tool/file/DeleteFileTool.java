package dev.omar.aiagent.mcp.tool.file;

import org.json.JSONArray;
import org.json.JSONObject;

import dev.omar.aiagent.mcp.service.file.FileOperationsService;

public class DeleteFileTool extends BaseFileTool {
    public DeleteFileTool(FileOperationsService fileService) {
        super(fileService);
    }

    @Override
    public String getName() { return "delete_file_or_folder"; }

    @Override
    public String getDescription() { return "حذف ملف أو مجلد بالكامل بناءً على المسار الموفر."; }

    @Override
    public JSONObject getParametersSchema() {
        try {
            JSONObject props = new JSONObject();
            JSONObject path = new JSONObject();
            path.put("type", "STRING");
            path.put("description", "المسار المطلق للملف أو المجلد المراد حذفه");
            props.put("target_path", path);

            JSONObject schema = new JSONObject();
            schema.put("type", "OBJECT");
            schema.put("properties", props);
            schema.put("required", new JSONArray().put("target_path"));
            return schema;
        } catch (Exception e) { return new JSONObject(); }
    }

    @Override
    public String execute(JSONObject args) {
        return fileService.deleteFileOrFolder(args.optString("target_path"));
    }
}
