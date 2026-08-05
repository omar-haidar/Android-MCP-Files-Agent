package dev.omar.aiagent.mcp.tool.file;

import org.json.JSONArray;
import org.json.JSONObject;

import dev.omar.aiagent.mcp.service.file.FileOperationsService;

public class ReadFileTool extends BaseFileTool {
    public ReadFileTool(FileOperationsService fileService) {
        super(fileService);
    }

    @Override
    public String getName() {
        return "read_file_content";
    }

    @Override
    public String getDescription() {
        return "قراءة وعرض النص الداخلي لملف معين بشرط ألا يتجاوز حجمه 1.5 ميغابايت.";
    }

    @Override
    public JSONObject getParametersSchema() {
        try {
            JSONObject props = new JSONObject();
            JSONObject path = new JSONObject();
            path.put("type", "STRING");
            path.put("description", "المسار المطلق للملف المراد قراءة محتواه");
            props.put("file_path", path);

            JSONObject schema = new JSONObject();
            schema.put("type", "OBJECT");
            schema.put("properties", props);
            schema.put("required", new JSONArray().put("file_path"));
            return schema;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    @Override
    public String execute(JSONObject args) {
        return fileService.readFileContent(args.optString("file_path"));
    }
}
