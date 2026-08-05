package dev.omar.aiagent.mcp.tool.file;

import org.json.JSONArray;
import org.json.JSONObject;

import dev.omar.aiagent.mcp.service.file.FileOperationsService;

public class CreateFileTool extends BaseFileTool {
    public CreateFileTool(FileOperationsService fileService) {
        super(fileService);
    }

    @Override
    public String getName() {
        return "create_file";
    }

    @Override
    public String getDescription() {
        return "إنشاء ملف نصي جديد (مثل txt, json, java, md) وتحديد محتواه بترميز UTF-8.";
    }

    @Override
    public JSONObject getParametersSchema() {
        try {
            JSONObject props = new JSONObject();

            JSONObject path = new JSONObject();
            path.put("type", "STRING");
            path.put("description", "المسار المطلق كاملاً شامل اسم الملف والامتداد (مثال: /sdcard/config.json)");
            props.put("file_path", path);

            JSONObject content = new JSONObject();
            content.put("type", "STRING");
            content.put("description", "المحتوى النصي المراد كتابته داخل الملف");
            props.put("content", content);

            JSONObject overwrite = new JSONObject();
            overwrite.put("type", "BOOLEAN");
            overwrite.put("description", "هل يتم استبدال الملف إن كان موجوداً من قبل (افتراضياً false)");
            props.put("overwrite", overwrite);

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
        boolean overwrite = args.optBoolean("overwrite", false);

        return fileService.createFileWithContent(path, content, overwrite);
    }
}
