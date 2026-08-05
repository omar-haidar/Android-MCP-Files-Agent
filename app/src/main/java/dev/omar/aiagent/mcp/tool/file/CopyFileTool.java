package dev.omar.aiagent.mcp.tool.file;

import org.json.JSONArray;
import org.json.JSONObject;

import dev.omar.aiagent.mcp.service.file.FileOperationsService;
import dev.omar.aiagent.mcp.tool.McpTool;

public class CopyFileTool extends BaseFileTool {
    public CopyFileTool(FileOperationsService fileService) {
        super(fileService);
    }

    @Override
    public String getName() {
        return "copy_file";
    }

    @Override
    public String getDescription() {
        return "نسخ ملف من مسار محدد إلى مسار أو مجلد آخر.";
    }

    @Override
    public JSONObject getParametersSchema() {
        try {
            JSONObject props = new JSONObject();

            JSONObject src = new JSONObject();
            src.put("type", "STRING");
            src.put("description", "مسار الملف المصدر (مثل /sdcard/Download/test.txt)");
            props.put("source_path", src);

            JSONObject dest = new JSONObject();
            dest.put("type", "STRING");
            dest.put("description", "مسار الملف الوجهة أو المجلد المستهدف (مثل /sdcard/Documents)");
            props.put("destination_path", dest);

            JSONObject overwrite = new JSONObject();
            overwrite.put("type", "BOOLEAN");
            overwrite.put("description", "هل يتم استبدال الملف في حال وجوده بالوجهة؟ (افتراضياً false)");
            props.put("overwrite", overwrite);

            JSONObject schema = new JSONObject();
            schema.put("type", "OBJECT");
            schema.put("properties", props);
            schema.put("required", new JSONArray().put("source_path").put("destination_path"));
            return schema;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    @Override
    public String execute(JSONObject args) {
        String src = args.optString("source_path");
        String dest = args.optString("destination_path");
        boolean overwrite = args.optBoolean("overwrite", false);
        return fileService.copyFile(src, dest, overwrite);
    }
}