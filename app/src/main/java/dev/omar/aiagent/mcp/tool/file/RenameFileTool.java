package dev.omar.aiagent.mcp.tool.file;

import org.json.JSONArray;
import org.json.JSONObject;

import dev.omar.aiagent.mcp.service.file.FileOperationsService;

public class RenameFileTool extends BaseFileTool {
    public RenameFileTool(FileOperationsService fileService) {
        super(fileService);
    }

    @Override
    public String getName() { return "rename_file_or_folder"; }

    @Override
    public String getDescription() { return "تغيير اسم ملف أو مجلد إلى اسم جديد."; }

    @Override
    public JSONObject getParametersSchema() {
        try {
            JSONObject props = new JSONObject();

            JSONObject currentPath = new JSONObject();
            currentPath.put("type", "STRING");
            currentPath.put("description", "المسار الحالي للملف أو المجلد");
            props.put("current_path", currentPath);

            JSONObject newName = new JSONObject();
            newName.put("type", "STRING");
            newName.put("description", "الاسم الجديد فقط بدون المسار كامل");
            props.put("new_name", newName);

            JSONObject schema = new JSONObject();
            schema.put("type", "OBJECT");
            schema.put("properties", props);
            schema.put("required", new JSONArray().put("current_path").put("new_name"));
            return schema;
        } catch (Exception e) { return new JSONObject(); }
    }

    @Override
    public String execute(JSONObject args) {
        return fileService.renameFileOrFolder(
                args.optString("current_path"),
                args.optString("new_name")
        );
    }
}
