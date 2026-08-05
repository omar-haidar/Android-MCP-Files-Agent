package dev.omar.aiagent.mcp.tool.file;

import org.json.JSONArray;
import org.json.JSONObject;

import dev.omar.aiagent.mcp.service.file.FileOperationsService;

public class AppendToFileTool extends BaseFileTool {
    public AppendToFileTool(FileOperationsService fileService) {
        super(fileService);
    }

    @Override
    public String getName() {
        return "append_to_file";
    }

    @Override
    public String getDescription() {
        return "إضافة أو إلحاق نص جديد في نهاية ملف موجود مسبقاً دون مسح أو تعديل بياناته القديمة.";
    }

    @Override
    public JSONObject getParametersSchema() {
        try {
            JSONObject props = new JSONObject();

            JSONObject path = new JSONObject();
            path.put("type", "STRING");
            path.put("description", "المسار المطلق للملف المراد الإلحاق به (مثل /sdcard/log.txt)");
            props.put("file_path", path);

            JSONObject content = new JSONObject();
            content.put("type", "STRING");
            content.put("description", "النص أو البيانات المراد إضافتها في نهاية الملف");
            props.put("content", content);

            JSONObject schema = new JSONObject();
            schema.put("type", "OBJECT");
            schema.put("properties", props);
            schema.put("required", new JSONArray().put("file_path").put("content"));
            return schema;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    @Override
    public String execute(JSONObject args) {
        String path = args.optString("file_path");
        String content = args.optString("content");

        return fileService.appendToFileContent(path, content);
    }
}