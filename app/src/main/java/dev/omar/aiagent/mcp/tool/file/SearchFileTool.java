package dev.omar.aiagent.mcp.tool.file;

import org.json.JSONArray;
import org.json.JSONObject;

import dev.omar.aiagent.mcp.service.file.FileOperationsService;

public class SearchFileTool extends BaseFileTool {
    public SearchFileTool(FileOperationsService fileService) {
        super(fileService);
    }

    @Override
    public String getName() {
        return "search_files";
    }

    @Override
    public String getDescription() {
        return "البحث عن الملفات داخل مجلد محدد بأسماء أو امتدادات معينة (مثال: البحث عن جميع ملفات pdf أو ملف باسم معين).";
    }

    @Override
    public JSONObject getParametersSchema() {
        try {
            JSONObject props = new JSONObject();

            JSONObject dirPath = new JSONObject();
            dirPath.put("type", "STRING");
            dirPath.put("description", "مسار المجلد المُراد البحث داخله (مثال: /sdcard/Download)");
            props.put("directory_path", dirPath);

            JSONObject query = new JSONObject();
            query.put("type", "STRING");
            query.put("description", "جزء من اسم الملف المراد البحث عنه (اختياري)");
            props.put("query", query);

            JSONObject extension = new JSONObject();
            extension.put("type", "STRING");
            extension.put("description", "امتداد الملف للفلترة مثل: pdf, json, java, txt (اختياري)");
            props.put("extension", extension);

            JSONObject maxResults = new JSONObject();
            maxResults.put("type", "INTEGER");
            maxResults.put("description", "الحد الأقصى لعدد النتائج المرجعة (افتراضياً 50)");
            props.put("max_results", maxResults);

            JSONObject schema = new JSONObject();
            schema.put("type", "OBJECT");
            schema.put("properties", props);
            schema.put("required", new JSONArray().put("directory_path"));
            return schema;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    @Override
    public String execute(JSONObject args) {
        String dirPath = args.optString("directory_path");
        String query = args.optString("query", null);
        String extension = args.optString("extension", null);
        int maxResults = args.optInt("max_results", 50);

        return fileService.searchFiles(dirPath, query, extension, maxResults);
    }
}
