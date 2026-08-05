package dev.omar.aiagent.mcp.tool.file;

import org.json.JSONArray;
import org.json.JSONObject;

import dev.omar.aiagent.mcp.service.file.FileOperationsService;

public class GetListFilesTool extends BaseFileTool {
    public GetListFilesTool(FileOperationsService fileService) {
        super(fileService);
    }

    @Override
    public String getName() { return "list_files"; }

    @Override
    public String getDescription() { return "عرض قائمة الملفات والمجلدات الموجودة في مسار معين."; }

    @Override
    public JSONObject getParametersSchema() {
        try {
            JSONObject props = new JSONObject();
            JSONObject path = new JSONObject();
            path.put("type", "STRING");
            path.put("description", "المسار المطلق للمجلد المراد عرض محتوياته");
            props.put("folder_path", path);

            JSONObject schema = new JSONObject();
            schema.put("type", "OBJECT");
            schema.put("properties", props);
            schema.put("required", new JSONArray().put("folder_path"));
            return schema;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    @Override
    public String execute(JSONObject args) {
        return fileService.listFiles(args.optString("folder_path"));
    }
}
